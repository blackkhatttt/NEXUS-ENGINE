package com.nexus.hub

import android.app.Activity
import android.app.AlertDialog
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SceneEditorActivity : Activity() {
    companion object {
        const val EXTRA_TREE_URI = "nexus.project.tree_uri"
        const val EXTRA_PROJECT_NAME = "nexus.project.name"
        const val EXTRA_PROJECT_IS_TREE_ROOT = "nexus.project.is_tree_root"
        val CONTROL_COMPONENTS = listOf("Button", "Joystick", "Arrows", "Panel")
    }

    private data class SceneNode(
        val id: String = UUID.randomUUID().toString(),
        var name: String = "Node2D",
        var type: String = "Node2D",
        var x: Float = 0f,
        var y: Float = 0f,
        var rotation: Float = 0f,
        var scaleX: Float = 1f,
        var scaleY: Float = 1f,
        val controlComponents: MutableList<String> = mutableListOf(),
        val children: MutableList<SceneNode> = mutableListOf()
    )

    private lateinit var project: DocumentFile
    private lateinit var sceneFile: DocumentFile
    private lateinit var sceneRoot: SceneNode
    private var selectedId: String = ""
    private lateinit var treePanel: LinearLayout
    private lateinit var inspectorPanel: LinearLayout
    private lateinit var statusLabel: TextView
    private lateinit var viewportView: SceneViewportView
    private var nameField: EditText? = null
    private var xField: EditText? = null
    private var yField: EditText? = null
    private var rotationField: EditText? = null
    private var scaleXField: EditText? = null
    private var scaleYField: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(9, 13, 22)
        window.navigationBarColor = Color.rgb(9, 13, 22)
        if (!resolveProject()) {
            finish()
            return
        }
        val scenes = project.findFile("Scenes") ?: project.createDirectory("Scenes")
        if (scenes == null) {
            toast("Could not access the Scenes folder.")
            finish()
            return
        }
        sceneFile = scenes.findFile("Main.nscene") ?: scenes.createFile("text/plain", "Main.nscene")
            ?: run {
                toast("Could not create Main.nscene.")
                finish()
                return
            }
        loadScene()
        buildEditor()
    }

    private fun resolveProject(): Boolean {
        val treeText = intent.getStringExtra(EXTRA_TREE_URI) ?: return false
        val treeUri = Uri.parse(treeText)
        val treeRoot = DocumentFile.fromTreeUri(this, treeUri) ?: return false
        val isRoot = intent.getBooleanExtra(EXTRA_PROJECT_IS_TREE_ROOT, false)
        val projectName = intent.getStringExtra(EXTRA_PROJECT_NAME).orEmpty()
        project = if (isRoot) treeRoot else treeRoot.findFile(projectName) ?: return false
        return project.isDirectory
    }

    private fun loadScene() {
        val source = try {
            contentResolver.openInputStream(sceneFile.uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
        } catch (_: Exception) { "" }
        sceneRoot = try {
            if (source.isBlank()) defaultScene() else {
                val document = JSONObject(source)
                nodeFromJson(document.getJSONObject("root"))
            }
        } catch (_: Exception) {
            toast("Scene file was invalid. A new scene was created.")
            defaultScene()
        }
        selectedId = sceneRoot.id
    }

    private fun defaultScene() = SceneNode(name = "Main", type = "Node2D")

    private fun nodeFromJson(json: JSONObject): SceneNode {
        val node = SceneNode(
            id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
            name = json.optString("name", "Node2D"),
            type = json.optString("type", "Node2D"),
            x = json.optDouble("x", 0.0).toFloat(),
            y = json.optDouble("y", 0.0).toFloat(),
            rotation = json.optDouble("rotation", 0.0).toFloat(),
            scaleX = json.optDouble("scale_x", 1.0).toFloat(),
            scaleY = json.optDouble("scale_y", 1.0).toFloat()
        )
        val components = json.optJSONArray("control_components") ?: JSONArray()
        for (index in 0 until components.length()) {
            val component = components.optString(index)
            if (component in CONTROL_COMPONENTS && !node.controlComponents.contains(component)) {
                node.controlComponents.add(component)
            }
        }
        val children = json.optJSONArray("children") ?: JSONArray()
        for (index in 0 until children.length()) {
            val child = children.optJSONObject(index) ?: continue
            node.children.add(nodeFromJson(child))
        }
        return node
    }

    private fun nodeToJson(node: SceneNode): JSONObject {
        val children = JSONArray()
        node.children.forEach { children.put(nodeToJson(it)) }
        return JSONObject()
            .put("id", node.id)
            .put("name", node.name)
            .put("type", node.type)
            .put("x", node.x.toDouble())
            .put("y", node.y.toDouble())
            .put("rotation", node.rotation.toDouble())
            .put("scale_x", node.scaleX.toDouble())
            .put("scale_y", node.scaleY.toDouble())
            .put("control_components", JSONArray().also { array ->
                node.controlComponents.forEach { array.put(it) }
            })
            .put("children", children)
    }

    private fun buildEditor() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundColor(Color.rgb(8, 13, 23))
        }
        page.addView(TextView(this).apply {
            text = "NEXUS  /  SCENE WORKSPACE"
            textSize = 10f
            letterSpacing = 0.12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(99, 230, 190))
        }, matchWrap())
        page.addView(TextView(this).apply {
            text = "${project.name ?: "PROJECT"}     •     Scenes/Main.nscene"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(232, 239, 248))
        }, matchWrap())

        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        toolbar.addView(toolButton("＋ CHILD") { addChildDialog() }, weighted())
        toolbar.addView(toolButton("RENAME") { renameSelectedDialog() }, weighted())
        toolbar.addView(toolButton("DELETE") { deleteSelected() }, weighted())
        toolbar.addView(toolButton("SAVE") { saveScene() }, weighted())
        page.addView(toolbar, matchWrap())

        val viewportCard = panel()
        viewportCard.addView(sectionTitle("2D VIEWPORT"))
        viewportView = SceneViewportView(this).apply {
            onNodeSelected = { id ->
                selectedId = id
                refreshPanels()
            }
            onNodeMoved = { id, dx, dy ->
                val node = findNode(sceneRoot, id)
                if (node != null) {
                    node.x += dx
                    node.y += dy
                    refreshViewport()
                }
            }
        }
        viewportCard.addView(viewportView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(250)
        ))
        page.addView(viewportCard, matchWrap())

        val scroll = ScrollView(this)
        val panels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val treeCard = panel()
        treeCard.addView(sectionTitle("SCENE TREE"))
        treePanel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        treeCard.addView(treePanel, matchWrap())
        panels.addView(treeCard, matchWrap())

        val inspectorCard = panel()
        inspectorCard.addView(sectionTitle("INSPECTOR"))
        inspectorPanel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inspectorCard.addView(inspectorPanel, matchWrap())
        panels.addView(inspectorCard, matchWrap())
        scroll.addView(panels)
        page.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        statusLabel = TextView(this).apply {
            text = "Scene ready • Select a node to inspect it"
            textSize = 11f
            setTextColor(Color.rgb(99, 230, 190))
            setPadding(dp(4), dp(8), dp(4), dp(4))
        }
        page.addView(statusLabel, matchWrap())
        page.addView(toolButton("BACK TO PROJECT") { finish() }, matchWrap())
        setContentView(page)
        refreshPanels()
    }

    private fun refreshPanels() {
        treePanel.removeAllViews()
        addTreeRow(sceneRoot, 0)
        inspectorPanel.removeAllViews()
        refreshViewport()
        val selected = findNode(sceneRoot, selectedId) ?: sceneRoot
        inspectorPanel.addView(TextView(this).apply {
            text = "TYPE   ${selected.type}"
            textSize = 12f
            setTextColor(Color.rgb(99, 230, 190))
            setPadding(0, dp(4), 0, dp(8))
        }, matchWrap())
        nameField = field("Name", selected.name)
        xField = field("Position X", selected.x.toString())
        yField = field("Position Y", selected.y.toString())
        rotationField = field("Rotation", selected.rotation.toString())
        scaleXField = field("Scale X", selected.scaleX.toString())
        scaleYField = field("Scale Y", selected.scaleY.toString())
        inspectorPanel.addView(toolButton("APPLY PROPERTIES") { applyInspector() }, matchWrap())
        if (selected.type == "Control") {
            inspectorPanel.addView(sectionTitle("CONTROL COMPONENTS"))
            inspectorPanel.addView(TextView(this).apply {
                text = if (selected.controlComponents.isEmpty()) "No components attached"
                    else selected.controlComponents.joinToString("  •  ")
                textSize = 12f
                setTextColor(Color.rgb(220, 230, 242))
                setPadding(0, dp(4), 0, dp(8))
            }, matchWrap())
            inspectorPanel.addView(toolButton("＋ ADD COMPONENT") {
                addControlComponentDialog(selected)
            }, matchWrap())
            if (selected.controlComponents.isNotEmpty()) {
                inspectorPanel.addView(toolButton("REMOVE COMPONENT") {
                    removeControlComponentDialog(selected)
                }, matchWrap())
            }
        }
    }

    private fun refreshViewport() {
        if (!::viewportView.isInitialized) return
        val items = mutableListOf<SceneViewportNode>()
        fun collect(node: SceneNode, parentX: Float, parentY: Float) {
            val worldX = parentX + node.x
            val worldY = parentY + node.y
            items.add(SceneViewportNode(node.id, node.name, worldX, worldY, node.children.isNotEmpty()))
            node.children.forEach { collect(it, worldX, worldY) }
        }
        collect(sceneRoot, 0f, 0f)
        viewportView.setScene(items, selectedId)
    }

    private fun addTreeRow(node: SceneNode, depth: Int) {
        val selected = node.id == selectedId
        val row = Button(this).apply {
            text = "${"  ".repeat(depth)}${if (selected) "▸ " else "  "}${node.name}  [${node.type}]"
            textSize = 12f
            isAllCaps = false
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setTextColor(if (selected) Color.rgb(9, 13, 22) else Color.rgb(220, 230, 242))
            setBackgroundColor(if (selected) Color.rgb(99, 230, 190) else Color.rgb(17, 25, 39))
            setOnClickListener {
                selectedId = node.id
                refreshPanels()
            }
        }
        treePanel.addView(row, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(42)
        ).apply { bottomMargin = dp(3) })
        node.children.forEach { addTreeRow(it, depth + 1) }
    }

    private fun field(label: String, value: String): EditText {
        inspectorPanel.addView(TextView(this).apply {
            text = label.uppercase()
            textSize = 10f
            setTextColor(Color.rgb(145, 160, 184))
        }, matchWrap())
        val edit = EditText(this).apply {
            setSingleLine(true)
            setText(value)
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setTextColor(Color.rgb(220, 230, 242))
            setHintTextColor(Color.GRAY)
            setPadding(dp(10), dp(5), dp(10), dp(5))
            setBackgroundColor(Color.rgb(17, 25, 39))
        }
        inspectorPanel.addView(edit, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(44)
        ).apply { bottomMargin = dp(7) })
        return edit
    }

    private fun applyInspector() {
        val node = findNode(sceneRoot, selectedId) ?: return
        val newName = nameField?.text?.toString()?.trim().orEmpty()
        val x = xField?.text?.toString()?.toFloatOrNull()
        val y = yField?.text?.toString()?.toFloatOrNull()
        val rotation = rotationField?.text?.toString()?.toFloatOrNull()
        val scaleX = scaleXField?.text?.toString()?.toFloatOrNull()
        val scaleY = scaleYField?.text?.toString()?.toFloatOrNull()
        if (newName.isBlank() || x == null || y == null || rotation == null || scaleX == null || scaleY == null) {
            toast("Enter a valid name and numeric transform values.")
            return
        }
        node.name = newName
        node.x = x
        node.y = y
        node.rotation = rotation
        node.scaleX = scaleX
        node.scaleY = scaleY
        statusLabel.text = "Updated ${node.name}"
        refreshPanels()
    }

    private fun addChildDialog() {
        val parent = findNode(sceneRoot, selectedId) ?: sceneRoot
        val input = EditText(this).apply { hint = "NewNode"; setSingleLine(true) }
        AlertDialog.Builder(this)
            .setTitle("Add child node")
            .setMessage("A Node2D will be added under ${parent.name}.")
            .setView(input)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("ADD") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isBlank()) {
                    toast("Node name cannot be empty.")
                } else {
                    val options = arrayOf("Node2D", "Control")
                    AlertDialog.Builder(this)
                        .setTitle("Choose node type")
                        .setItems(options) { _, which ->
                            val node = SceneNode(name = name, type = options[which])
                            parent.children.add(node)
                            selectedId = node.id
                            statusLabel.text = "Added ${node.type}: ${node.name}"
                            refreshPanels()
                        }
                        .show()
                }
            }.show()
    }

    private fun addControlComponentDialog(node: SceneNode) {
        val available = CONTROL_COMPONENTS.filterNot { node.controlComponents.contains(it) }
        if (available.isEmpty()) {
            toast("All available control components are already attached.")
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Add control component")
            .setItems(available.toTypedArray()) { _, which ->
                node.controlComponents.add(available[which])
                statusLabel.text = "Added ${available[which]} to ${node.name}"
                refreshPanels()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun removeControlComponentDialog(node: SceneNode) {
        if (node.controlComponents.isEmpty()) return
        val attached = node.controlComponents.toList()
        AlertDialog.Builder(this)
            .setTitle("Remove control component")
            .setItems(attached.toTypedArray()) { _, which ->
                node.controlComponents.remove(attached[which])
                statusLabel.text = "Removed ${attached[which]}"
                refreshPanels()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun renameSelectedDialog() {
        val node = findNode(sceneRoot, selectedId) ?: return
        val input = EditText(this).apply { setText(node.name); setSingleLine(true) }
        AlertDialog.Builder(this)
            .setTitle("Rename node")
            .setView(input)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("RENAME") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotBlank()) {
                    node.name = value
                    statusLabel.text = "Renamed node to $value"
                    refreshPanels()
                } else toast("Node name cannot be empty.")
            }.show()
    }

    private fun deleteSelected() {
        if (selectedId == sceneRoot.id) {
            toast("The root node cannot be deleted.")
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Delete selected node?")
            .setMessage("The selected node and all its children will be removed.")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("DELETE") { _, _ ->
                if (removeNode(sceneRoot, selectedId)) {
                    selectedId = sceneRoot.id
                    statusLabel.text = "Node deleted"
                    refreshPanels()
                }
            }.show()
    }

    private fun findNode(node: SceneNode, id: String): SceneNode? {
        if (node.id == id) return node
        node.children.forEach { child ->
            val found = findNode(child, id)
            if (found != null) return found
        }
        return null
    }

    private fun removeNode(parent: SceneNode, id: String): Boolean {
        val direct = parent.children.indexOfFirst { it.id == id }
        if (direct >= 0) {
            parent.children.removeAt(direct)
            return true
        }
        parent.children.forEach { if (removeNode(it, id)) return true }
        return false
    }

    private fun saveScene() {
        val document = JSONObject()
            .put("format", "NEXUS_SCENE")
            .put("version", 1)
            .put("root", nodeToJson(sceneRoot))
        try {
            contentResolver.openOutputStream(sceneFile.uri, "wt")?.bufferedWriter()?.use {
                it.write(document.toString(2))
            } ?: throw IllegalStateException("Storage provider did not open the scene file.")
            statusLabel.text = "Saved Scenes/Main.nscene"
            toast("Scene saved.")
        } catch (error: Exception) {
            toast("Could not save scene: ${error.message}")
        }
    }

    private fun panel() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(10), dp(10), dp(10), dp(10))
        background = GradientDrawable().apply { setColor(Color.rgb(17, 26, 41)); cornerRadius = dp(14).toFloat() }
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(10)
        layoutParams = params
    }

    private fun sectionTitle(value: String) = TextView(this).apply {
        text = value
        textSize = 11f
        letterSpacing = 0.1f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.rgb(145, 160, 184))
        setPadding(0, 0, 0, dp(8))
    }

    private fun toolButton(label: String, callback: () -> Unit) = Button(this).apply {
        text = label
        textSize = 10f
        isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(if (label == "SAVE" || label == "APPLY PROPERTIES") Color.rgb(8, 13, 23) else Color.rgb(232, 239, 248))
        background = GradientDrawable().apply {
            setColor(if (label == "SAVE" || label == "APPLY PROPERTIES") Color.rgb(99, 230, 190) else Color.rgb(31, 44, 63))
            cornerRadius = dp(9).toFloat()
        }
        setOnClickListener { callback() }
    }

    private fun weighted() = LinearLayout.LayoutParams(0, dp(44), 1f)
    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
