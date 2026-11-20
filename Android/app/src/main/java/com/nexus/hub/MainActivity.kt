package com.nexus.hub

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import org.json.JSONObject

class MainActivity : Activity() {
    private external fun nativeEngineVersion(): String
    private external fun nativeRunNXS(source: String): String

    private var currentProject: DocumentFile? = null
    private var projectTreeUri: Uri? = null
    private var projectIsTreeRoot = false
    private var currentScript: DocumentFile? = null
    private var scriptEditor: EditText? = null
    private var outputView: TextView? = null
    private var pendingAction = ACTION_NONE
    private var projectNamePending = ""

    companion object {
        private const val REQUEST_PROJECT_FOLDER = 4101
        private const val ACTION_NONE = 0
        private const val ACTION_CREATE = 1
        private const val ACTION_OPEN = 2
        private const val PREFS = "nexus_editor"
        private const val RECENT_TREE = "recent_tree_uri"
        private const val RECENT_NAME = "recent_project_name"
        private const val RECENT_IS_ROOT = "recent_is_root"
        private val BG = Color.rgb(8, 13, 23)
        private val PANEL = Color.rgb(17, 26, 41)
        private val MINT = Color.rgb(99, 230, 190)
        private val MUTED = Color.rgb(145, 160, 184)
        private val WHITE = Color.rgb(232, 239, 248)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = BG
        window.navigationBarColor = BG
        showHome()
    }

    private fun showHome() {
        val root = page()
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(24))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        brand.addView(TextView(this).apply {
            text = "N"
            textSize = 27f
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            setTextColor(BG)
            gravity = Gravity.CENTER
            background = rounded(MINT, 13)
        }, LinearLayout.LayoutParams(dp(48), dp(48)))
        val brandText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        brandText.addView(label("NEXUS ENGINE", 17f, WHITE, true))
        brandText.addView(label("GAME DEVELOPMENT ENVIRONMENT", 9f, MUTED, true))
        brand.addView(brandText)
        content.addView(brand)
        gap(content, 30)

        val hero = card()
        hero.addView(label("WELCOME TO NEXUS", 10f, MINT, true))
        gap(hero, 10)
        hero.addView(label("Build worlds.\nWrite your logic.", 29f, WHITE, true))
        gap(hero, 10)
        hero.addView(label(
            "Create projects, edit 2D scenes and write scripts with NXS.",
            14f, MUTED, false
        ))
        gap(hero, 18)
        hero.addView(label("EDITOR PREVIEW  •  VERSION 0.5", 10f, MUTED, true))
        content.addView(hero)
        gap(content, 22)

        content.addView(label("START", 11f, MUTED, true))
        gap(content, 9)
        actionCard(content, "＋", "Create new project",
            "Start with a clean NEXUS project.", true) { askProjectName() }
        gap(content, 10)
        actionCard(content, "▣", "Open project",
            "Scan a folder and detect NEXUS projects automatically.", false) {
            chooseFolder(ACTION_OPEN)
        }
        gap(content, 22)

        val recent = recentProjectInfo()
        if (recent != null) {
            content.addView(label("RECENT PROJECT", 11f, MUTED, true))
            gap(content, 9)
            actionCard(content, "↻", recent.second,
                "Reopen your last project.", false) { reopenRecentProject() }
            gap(content, 20)
        }

        val status = nativeStatus()
        val statusCard = card()
        statusCard.orientation = LinearLayout.HORIZONTAL
        statusCard.gravity = Gravity.CENTER_VERTICAL
        statusCard.addView(View(this).apply {
            background = rounded(MINT, 20)
        }, LinearLayout.LayoutParams(dp(8), dp(8)))
        val statusText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        statusText.addView(label("ENGINE STATUS", 9f, MUTED, true))
        statusText.addView(label(status, 12f, WHITE, false))
        statusCard.addView(statusText)
        content.addView(statusCard)
        gap(content, 12)
        actionCard(content, "?", "NXS documentation",
            "See the language features supported by the runtime.", false) {
            showDocumentation()
        }
        setContentView(root)
    }

    private fun nativeStatus(): String = try {
        System.loadLibrary("nexus_android")
        "${nativeEngineVersion()}  •  Native bridge connected"
    } catch (_: UnsatisfiedLinkError) {
        "Native C++ bridge unavailable"
    }

    private fun askProjectName() {
        val input = EditText(this).apply {
            hint = "MyFirstGame"
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("Create project")
            .setMessage("Choose a name for your new game project.")
            .setView(input)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("CONTINUE") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isBlank()) toast("Enter a project name first.")
                else {
                    projectNamePending = name
                    chooseFolder(ACTION_CREATE)
                }
            }.show()
    }

    private fun chooseFolder(action: Int) {
        pendingAction = action
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_PROJECT_FOLDER)
    }

    @Deprecated("Uses Android Storage Access Framework for broad device compatibility.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PROJECT_FOLDER || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val flags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try { contentResolver.takePersistableUriPermission(uri, flags) } catch (_: SecurityException) {}
        val selected = DocumentFile.fromTreeUri(this, uri) ?: return
        projectTreeUri = uri
        when (pendingAction) {
            ACTION_CREATE -> {
                projectIsTreeRoot = false
                createProject(selected, projectNamePending)
            }
            ACTION_OPEN -> discoverProjects(selected, uri)
        }
        pendingAction = ACTION_NONE
    }

    private fun createProject(parent: DocumentFile, name: String) {
        val safeName = name.replace(Regex("[^A-Za-z0-9 _-]"), "").trim()
        if (safeName.isBlank()) {
            toast("That project name is not valid.")
            return
        }
        val project = parent.findFile(safeName) ?: parent.createDirectory(safeName)
        if (project == null) {
            toast("Could not create the project folder.")
            return
        }
        val scripts = project.findFile("Scripts") ?: project.createDirectory("Scripts")
        project.findFile("Scenes") ?: project.createDirectory("Scenes")
        project.findFile("Assets") ?: project.createDirectory("Assets")
        if (scripts == null) {
            toast("Could not create the Scripts folder.")
            return
        }

        val manifestJson = JSONObject()
            .put("format", "NEXUS_PROJECT")
            .put("version", 1)
            .put("name", safeName)
            .put("engine", "0.5.0")
            .put("main_scene", "Scenes/Main.nscene")
            .put("main_script", "Scripts/main.nxs")
        val manifest = project.findFile("project.nscene")
            ?: project.createFile("application/json", "project.nscene")
        if (manifest == null || !writeDocument(manifest, manifestJson.toString(2))) {
            toast("Could not create project.nscene.")
            return
        }
        // Keep the previous marker for projects and tools created with Editor 0.2–0.4.
        val legacy = project.findFile("project.nproject")
            ?: project.createFile("application/json", "project.nproject")
        legacy?.let { writeDocument(it, manifestJson.toString(2)) }

        val main = scripts.findFile("main.nxs") ?: scripts.createFile("text/plain", "main.nxs")
        if (main == null) {
            toast("Project created, but main.nxs could not be created.")
            return
        }
        val starter = """game "$safeName" start {
    # Your first NXS script
    var message Text = "Hello from NEXUS!"
    var score Number = 0
    var speed NumberD = 4.5
    var running FalseTrue = true

    print(message)
    print("Score: " + score)
    print("Speed: " + speed)
    print("Running: " + running)
}
"""
        if (!writeDocument(main, starter)) return
        currentProject = project
        projectIsTreeRoot = false
        rememberProject()
        showProjectHome()
        toast("Project created.")
    }

    private fun hasProjectMarker(folder: DocumentFile): Boolean =
        folder.findFile("project.nscene")?.isFile == true ||
            folder.findFile("project.nproject")?.isFile == true

    private fun discoverProjects(selected: DocumentFile, treeUri: Uri) {
        val found = mutableListOf<Pair<DocumentFile, Boolean>>()
        if (hasProjectMarker(selected)) found.add(selected to true)
        selected.listFiles().filter { it.isDirectory && hasProjectMarker(it) }
            .forEach { child -> if (found.none { it.first.uri == child.uri }) found.add(child to false) }

        when (found.size) {
            0 -> AlertDialog.Builder(this)
                .setTitle("No NEXUS projects found")
                .setMessage("This folder does not contain project.nscene or project.nproject. Choose a project folder or a parent folder containing projects.")
                .setPositiveButton("SCAN ANOTHER FOLDER") { _, _ -> chooseFolder(ACTION_OPEN) }
                .setNegativeButton("CLOSE", null)
                .show()
            1 -> {
                projectTreeUri = treeUri
                projectIsTreeRoot = found[0].second
                openProject(found[0].first)
            }
            else -> {
                val projects = found.map { it.first.name ?: "Unnamed project" }
                AlertDialog.Builder(this)
                    .setTitle("NEXUS projects detected")
                    .setMessage("${found.size} projects found in this folder.")
                    .setItems(projects.toTypedArray()) { _, which ->
                        projectTreeUri = treeUri
                        projectIsTreeRoot = found[which].second
                        openProject(found[which].first)
                    }
                    .setNegativeButton("CANCEL", null)
                    .show()
            }
        }
    }

    private fun openProject(folder: DocumentFile) {
        if (!hasProjectMarker(folder)) {
            toast("Project marker not found. Expected project.nscene.")
            return
        }
        currentProject = folder
        rememberProject()
        val scripts = folder.findFile("Scripts")
        val files = scripts?.listFiles()?.filter {
            it.isFile && it.name?.endsWith(".nxs", ignoreCase = true) == true
        } ?: emptyList()
        if (files.size == 1) {
            showProjectHome()
        } else if (files.size > 1) {
            AlertDialog.Builder(this)
                .setTitle("Choose an NXS script")
                .setItems(files.map { it.name ?: "Unnamed.nxs" }.toTypedArray()) { _, which ->
                    openScript(files[which])
                }
                .setNegativeButton("CANCEL") { _, _ -> showProjectHome() }
                .show()
        } else {
            showProjectHome()
        }
    }

    private fun rememberProject() {
        val project = currentProject ?: return
        val uri = projectTreeUri ?: return
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
            .putString(RECENT_TREE, uri.toString())
            .putString(RECENT_NAME, project.name ?: "")
            .putBoolean(RECENT_IS_ROOT, projectIsTreeRoot)
            .apply()
    }

    private fun recentProjectInfo(): Pair<String, String>? {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val uri = prefs.getString(RECENT_TREE, null) ?: return null
        val name = prefs.getString(RECENT_NAME, null) ?: return null
        return uri to name
    }

    private fun reopenRecentProject() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val uriText = prefs.getString(RECENT_TREE, null) ?: return
        val name = prefs.getString(RECENT_NAME, null).orEmpty()
        val isRoot = prefs.getBoolean(RECENT_IS_ROOT, false)
        try {
            val uri = Uri.parse(uriText)
            val tree = DocumentFile.fromTreeUri(this, uri)
                ?: throw IllegalStateException("Folder access is no longer available.")
            val project = if (isRoot) tree else tree.findFile(name)
                ?: throw IllegalStateException("Project folder was moved or deleted.")
            projectTreeUri = uri
            projectIsTreeRoot = isRoot
            openProject(project)
        } catch (error: Exception) {
            toast("Could not reopen recent project: ${error.message}")
            chooseFolder(ACTION_OPEN)
        }
    }

    private fun showProjectHome() {
        val project = currentProject ?: return showHome()
        val root = page()
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(22))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        toolbarTitle(content, "PROJECT WORKSPACE", project.name ?: "NEXUS PROJECT")
        gap(content, 18)
        val info = card()
        info.addView(label("PROJECT DETECTED", 10f, MINT, true))
        gap(info, 8)
        info.addView(label(project.name ?: "NEXUS Project", 22f, WHITE, true))
        gap(info, 5)
        info.addView(label(
            if (project.findFile("project.nscene") != null) "project.nscene  •  NEXUS project format"
            else "project.nproject  •  legacy project format",
            11f, MUTED, false
        ))
        content.addView(info)
        gap(content, 20)

        content.addView(label("WORKSPACE", 11f, MUTED, true))
        gap(content, 9)
        actionCard(content, "▦", "Open 2D scene editor",
            "Edit your scene tree, properties and 2D layout.", true) {
            val treeUri = projectTreeUri
            if (treeUri == null) toast("Reopen the project to grant folder access.")
            else startActivity(Intent(this, SceneEditorActivity::class.java).apply {
                putExtra(SceneEditorActivity.EXTRA_TREE_URI, treeUri.toString())
                putExtra(SceneEditorActivity.EXTRA_PROJECT_NAME, project.name ?: "")
                putExtra(SceneEditorActivity.EXTRA_PROJECT_IS_TREE_ROOT, projectIsTreeRoot)
            })
        }
        gap(content, 10)
        actionCard(content, "</>", "Open NXS script",
            "Edit and run scripts using the native NXS runtime.", false) {
            val scripts = project.findFile("Scripts")
            val files = scripts?.listFiles()?.filter {
                it.isFile && it.name?.endsWith(".nxs", ignoreCase = true) == true
            } ?: emptyList()
            if (files.isEmpty()) toast("No NXS scripts found in Scripts/.")
            else if (files.size == 1) openScript(files[0])
            else AlertDialog.Builder(this).setTitle("Choose script")
                .setItems(files.map { it.name ?: "Script.nxs" }.toTypedArray()) { _, which ->
                    openScript(files[which])
                }.show()
        }
        gap(content, 10)
        actionCard(content, "↗", "Open another project",
            "Find projects in another folder.", false) { chooseFolder(ACTION_OPEN) }
        gap(content, 10)
        actionCard(content, "⌂", "Back to NEXUS home",
            "Return to the project launcher.", false) { showHome() }
        setContentView(root)
    }

    private fun openScript(file: DocumentFile) {
        val source = try {
            contentResolver.openInputStream(file.uri)?.bufferedReader()?.use { it.readText() } ?: ""
        } catch (error: Exception) {
            toast("Could not read this script: ${error.message}")
            return
        }
        currentScript = file
        val root = page()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        top.addView(label(currentProject?.name ?: "NEXUS PROJECT", 16f, WHITE, true),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(textAction("SAVE", MINT) { saveScript() })
        top.addView(textAction("RUN", MINT) { runScript() })
        top.addView(textAction("BACK", PANEL) { showProjectHome() })
        content.addView(top)
        gap(content, 8)
        content.addView(label("NXS SCRIPT  /  ${file.name ?: "main.nxs"}", 10f, MUTED, true))
        gap(content, 10)
        val editor = EditText(this).apply {
            setText(source)
            setTextColor(WHITE)
            setHintTextColor(MUTED)
            setBackground(rounded(PANEL, 12))
            typeface = Typeface.MONOSPACE
            textSize = 13f
            gravity = Gravity.TOP or Gravity.START
            setPadding(dp(12), dp(12), dp(12), dp(12))
            minLines = 15
            isVerticalScrollBarEnabled = true
            setHorizontallyScrolling(true)
        }
        scriptEditor = editor
        content.addView(editor, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        gap(content, 10)
        content.addView(label("OUTPUT", 10f, MUTED, true))
        outputView = TextView(this).apply {
            text = "Ready. Press RUN to execute this NXS script."
            setTextColor(MINT)
            typeface = Typeface.MONOSPACE
            textSize = 12f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(PANEL, 10)
            minHeight = dp(76)
        }
        content.addView(outputView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        root.addView(content, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ))
        setContentView(root)
    }

    private fun saveScript() {
        val file = currentScript ?: return
        val source = scriptEditor?.text?.toString() ?: return
        if (writeDocument(file, source)) {
            outputView?.text = "Saved: ${file.name}"
            toast("Script saved.")
        }
    }

    private fun runScript() {
        val source = scriptEditor?.text?.toString() ?: return
        saveScript()
        val result = try {
            System.loadLibrary("nexus_android")
            nativeRunNXS(source)
        } catch (_: UnsatisfiedLinkError) {
            "ERROR: Native NXS runtime is unavailable."
        }
        outputView?.text = result
    }

    private fun writeDocument(file: DocumentFile, contents: String): Boolean = try {
        contentResolver.openOutputStream(file.uri, "wt")?.bufferedWriter()?.use { it.write(contents) }
            ?: throw IllegalStateException("Storage provider did not open the file.")
        true
    } catch (error: Exception) {
        toast("Could not save file: ${error.message}")
        false
    }

    private fun showDocumentation() {
        AlertDialog.Builder(this)
            .setTitle("NXS language")
            .setMessage(
                "Supported by the current runtime:\n\n" +
                "• game \"PROJECT\" start { ... }\n" +
                "• var name Text = \"text\"\n" +
                "• Number, NumberD and FalseTrue\n" +
                "• print(expression)\n" +
                "• assignment and +\n" +
                "• # line comments\n\n" +
                "Functions, conditions, loops and scene-node commands are planned for later versions."
            )
            .setPositiveButton("GOT IT", null)
            .show()
    }

    private fun page() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(BG)
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = rounded(PANEL, 16)
    }

    private fun actionCard(
        parent: LinearLayout, symbol: String, title: String, description: String,
        primary: Boolean, callback: () -> Unit
    ) {
        val surface = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(14), dp(14), dp(14))
            background = rounded(if (primary) Color.rgb(23, 54, 54) else PANEL, 14)
            isClickable = true
            isFocusable = true
            setOnClickListener { callback() }
        }
        val icon = TextView(this).apply {
            text = symbol
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(if (primary) MINT else WHITE)
            background = rounded(if (primary) Color.rgb(12, 77, 68) else Color.rgb(31, 44, 63), 11)
        }
        surface.addView(icon, LinearLayout.LayoutParams(dp(44), dp(44)))
        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), 0, dp(6), 0)
        }
        texts.addView(label(title, 15f, WHITE, true))
        gap(texts, 4)
        texts.addView(label(description, 11f, MUTED, false))
        surface.addView(texts, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        surface.addView(label("›", 24f, if (primary) MINT else MUTED, false))
        parent.addView(surface, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun toolbarTitle(parent: LinearLayout, eyebrow: String, heading: String) {
        parent.addView(label("NEXUS  /  $eyebrow", 10f, MINT, true))
        gap(parent, 6)
        parent.addView(label(heading, 25f, WHITE, true))
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun textAction(value: String, color: Int, callback: () -> Unit) =
        TextView(this).apply {
            text = value
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(color)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(10), dp(8), dp(10))
            setOnClickListener { callback() }
        }

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun gap(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
