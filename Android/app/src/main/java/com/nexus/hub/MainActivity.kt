package com.nexus.hub

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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

class MainActivity : Activity() {
    private external fun nativeEngineVersion(): String
    private external fun nativeRunNXS(source: String): String

    private var currentProject: DocumentFile? = null
    private var projectTreeUri: Uri? = null
    private var projectIsTreeRoot: Boolean = false
    private var currentScript: DocumentFile? = null
    private var scriptEditor: EditText? = null
    private var outputView: TextView? = null
    private var pendingAction = ACTION_NONE

    companion object {
        private const val REQUEST_PROJECT_FOLDER = 4101
        private const val ACTION_NONE = 0
        private const val ACTION_CREATE = 1
        private const val ACTION_OPEN = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(9, 13, 22)
        window.navigationBarColor = Color.rgb(9, 13, 22)
        showHome()
    }

    private fun showHome() {
        val content = page()
        title(content, "NEXUS")
        subtitle(content, "ENGINE EDITOR  •  EARLY ACCESS")
        space(content, 28)
        heading(content, "Create something new.")
        body(content, "Create projects, edit NXS scripts and run them with the native NEXUS runtime.")
        space(content, 26)
        action(content, "＋   CREATE PROJECT") { askProjectName() }
        space(content, 12)
        action(content, "OPEN PROJECT") { chooseFolder(ACTION_OPEN) }
        space(content, 12)
        action(content, "NXS DOCUMENTATION") { showDocumentation() }
        space(content, 32)
        val nativeStatus = try {
            System.loadLibrary("nexus_android")
            nativeEngineVersion() + "\nNative C++ bridge: connected"
        } catch (error: UnsatisfiedLinkError) {
            "NEXUS ENGINE 0.1.0\nNative C++ bridge: unavailable"
        }
        body(content, nativeStatus)
        setContentView(content)
    }

    private fun askProjectName() {
        val input = EditText(this).apply {
            hint = "MyFirstGame"
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("New NEXUS project")
            .setMessage("Choose a project name.")
            .setView(input)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("CONTINUE") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isBlank()) {
                    toast("Enter a project name first.")
                } else {
                    projectNamePending = name
                    chooseFolder(ACTION_CREATE)
                }
            }.show()
    }

    private var projectNamePending = ""

    private fun chooseFolder(action: Int) {
        pendingAction = action
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_PROJECT_FOLDER)
    }

    @Deprecated("Uses the Android Storage Access Framework folder picker for broad device compatibility.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PROJECT_FOLDER || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val flags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try { contentResolver.takePersistableUriPermission(uri, flags) } catch (_: SecurityException) {}
        val selected = DocumentFile.fromTreeUri(this, uri) ?: return
        projectTreeUri = uri
        projectIsTreeRoot = pendingAction == ACTION_OPEN
        when (pendingAction) {
            ACTION_CREATE -> createProject(selected, projectNamePending)
            ACTION_OPEN -> openProject(selected)
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
        val manifest = project.findFile("project.nproject")
            ?: project.createFile("application/json", "project.nproject")
        manifest?.let {
            writeDocument(it, "{\n  \"name\": \"$safeName\",\n  \"engine\": \"0.1.0\",\n  \"main\": \"Scripts/main.nxs\"\n}\n")
        }
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
        writeDocument(main, starter)
        currentProject = project
        openScript(main)
        toast("Project created.")
    }

    private fun openProject(folder: DocumentFile) {
        val manifest = folder.findFile("project.nproject")
        if (manifest == null) {
            toast("This folder does not contain project.nproject. Select the project folder.")
            return
        }
        currentProject = folder
        val scripts = folder.findFile("Scripts")
        val files = scripts?.listFiles()?.filter {
            it.isFile && it.name?.endsWith(".nxs", ignoreCase = true) == true
        } ?: emptyList()
        if (files.isEmpty()) {
            toast("No .nxs scripts found in the Scripts folder.")
            showProjectHome()
            return
        }
        if (files.size == 1) {
            openScript(files[0])
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Choose an NXS script")
            .setItems(files.map { it.name ?: "Unnamed.nxs" }.toTypedArray()) { _, which ->
                openScript(files[which])
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun showProjectHome() {
        val project = currentProject ?: return showHome()
        val content = page()
        heading(content, project.name ?: "NEXUS Project")
        body(content, "Project workspace")
        space(content, 18)
        action(content, "OPEN SCENE EDITOR") {
            val treeUri = projectTreeUri
            if (treeUri == null) {
                toast("Please reopen the project to grant folder access.")
            } else {
                startActivity(Intent(this, SceneEditorActivity::class.java).apply {
                    putExtra(SceneEditorActivity.EXTRA_TREE_URI, treeUri.toString())
                    putExtra(SceneEditorActivity.EXTRA_PROJECT_NAME, project.name ?: "")
                    putExtra(SceneEditorActivity.EXTRA_PROJECT_IS_TREE_ROOT, projectIsTreeRoot)
                })
            }
        }
        space(content, 10)
        action(content, "OPEN main.nxs") {
            val main = project.findFile("Scripts")?.findFile("main.nxs")
            if (main != null) openScript(main) else toast("main.nxs was not found.")
        }
        space(content, 10)
        action(content, "OPEN ANOTHER PROJECT") { chooseFolder(ACTION_OPEN) }
        space(content, 10)
        action(content, "HOME") { showHome() }
        setContentView(content)
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
        val projectName = currentProject?.name ?: "NEXUS Project"
        heading(root, projectName)
        subtitle(root, "SCRIPT EDITOR  /  ${file.name ?: "main.nxs"}")
        space(root, 10)

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val save = smallAction("SAVE") { saveScript() }
        val run = smallAction("▶ RUN") { runScript() }
        val back = smallAction("PROJECT") { showProjectHome() }
        toolbar.addView(save, weighted())
        toolbar.addView(run, weighted())
        toolbar.addView(back, weighted())
        root.addView(toolbar)
        space(root, 10)

        val editor = EditText(this).apply {
            setText(source)
            setTextColor(Color.rgb(220, 230, 242))
            setHintTextColor(Color.rgb(120, 135, 155))
            setBackgroundColor(Color.rgb(17, 25, 39))
            typeface = Typeface.MONOSPACE
            textSize = 13f
            gravity = Gravity.TOP or Gravity.START
            setPadding(dp(12), dp(12), dp(12), dp(12))
            minLines = 15
            isVerticalScrollBarEnabled = true
            setHorizontallyScrolling(true)
        }
        scriptEditor = editor
        root.addView(editor, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        space(root, 12)
        subtitle(root, "OUTPUT")
        outputView = TextView(this).apply {
            text = "Ready. Press RUN to execute this NXS script."
            setTextColor(Color.rgb(99, 230, 190))
            typeface = Typeface.MONOSPACE
            textSize = 12f
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setBackgroundColor(Color.rgb(17, 25, 39))
            minHeight = dp(76)
        }
        root.addView(outputView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
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
        } catch (error: UnsatisfiedLinkError) {
            "ERROR: Native NXS runtime is unavailable."
        }
        outputView?.text = result
    }

    private fun writeDocument(file: DocumentFile, contents: String): Boolean {
        return try {
            contentResolver.openOutputStream(file.uri, "wt")?.bufferedWriter()?.use {
                it.write(contents)
            } ?: throw IllegalStateException("Storage provider did not open the file.")
            true
        } catch (error: Exception) {
            toast("Could not save file: ${error.message}")
            false
        }
    }

    private fun showDocumentation() {
        AlertDialog.Builder(this)
            .setTitle("NXS 0.1")
            .setMessage(
                "Supported now:\n" +
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

    private fun page(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(16))
            setBackgroundColor(Color.rgb(9, 13, 22))
        }
        return root
    }

    private fun title(parent: LinearLayout, value: String) {
        parent.addView(TextView(this).apply {
            text = value
            textSize = 34f
            setTextColor(Color.rgb(99, 230, 190))
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            gravity = Gravity.CENTER
        }, matchWrap())
    }

    private fun subtitle(parent: LinearLayout, value: String) {
        parent.addView(TextView(this).apply {
            text = value
            textSize = 11f
            letterSpacing = 0.12f
            setTextColor(Color.rgb(145, 160, 184))
        }, matchWrap())
    }

    private fun heading(parent: LinearLayout, value: String) {
        parent.addView(TextView(this).apply {
            text = value
            textSize = 23f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }, matchWrap())
    }

    private fun body(parent: LinearLayout, value: String) {
        parent.addView(TextView(this).apply {
            text = value
            textSize = 13f
            setTextColor(Color.rgb(166, 178, 198))
        }, matchWrap())
    }

    private fun action(parent: LinearLayout, label: String, callback: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = 13f
            isAllCaps = false
            setTextColor(Color.rgb(9, 13, 22))
            setBackgroundColor(Color.rgb(99, 230, 190))
            minHeight = dp(52)
            setOnClickListener { callback() }
        }
        parent.addView(button, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
        ))
    }

    private fun smallAction(label: String, callback: () -> Unit) = Button(this).apply {
        text = label
        textSize = 11f
        isAllCaps = false
        setOnClickListener { callback() }
    }

    private fun weighted() = LinearLayout.LayoutParams(0, dp(46), 1f)
    private fun space(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }
    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
