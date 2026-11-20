package com.nexus.hub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

data class SceneViewportNode(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val hasChildren: Boolean,
    val textureUri: String = "",
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f
)

/** Touch-enabled 2D scene canvas. Positive world Y points upward. */
class SceneViewportView(context: Context) : View(context) {
    var onNodeSelected: ((String) -> Unit)? = null
    var onNodeMoved: ((String, Float, Float) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF202B3B.toInt(); strokeWidth = density }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF3A786F.toInt(); strokeWidth = 1.5f * density }
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF63E6BE.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f * density
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE2EAF4.toInt(); textSize = 11f * density
    }
    private val originPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF63E6BE.toInt(); strokeWidth = 2f * density
    }

    private var nodes: List<SceneViewportNode> = emptyList()
    private val textureCache = mutableMapOf<String, Bitmap?>()
    private var selectedId = ""
    private var zoom = 1f
    private var panX = 0f
    private var panY = 0f
    private var activeNodeId: String? = null
    private var lastX = 0f
    private var lastY = 0f
    private var pinchStartDistance = 0f
    private var pinchStartZoom = 1f
    private var moved = false

    fun setScene(value: List<SceneViewportNode>, selected: String) {
        nodes = value
        selectedId = selected
        invalidate()
    }

    private fun screenX(x: Float) = width / 2f + panX + x * zoom * density
    private fun screenY(y: Float) = height / 2f + panY - y * zoom * density

    private fun textureFor(uriText: String): Bitmap? {
        if (uriText.isBlank()) return null
        if (textureCache.containsKey(uriText)) return textureCache[uriText]
        val bitmap = try {
            context.contentResolver.openInputStream(Uri.parse(uriText))?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) { null }
        textureCache[uriText] = bitmap
        return bitmap
    }

    private fun hitNode(x: Float, y: Float) = nodes.asReversed().firstOrNull { node ->
        abs(x - screenX(node.x)) <= 27f * density &&
            abs(y - screenY(node.y)) <= 22f * density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0xFF080E17.toInt())
        val spacing = max(12f * density, 32f * density * zoom)
        var gx = (width / 2f + panX) % spacing
        while (gx < width) { canvas.drawLine(gx, 0f, gx, height.toFloat(), gridPaint); gx += spacing }
        var gy = (height / 2f + panY) % spacing
        while (gy < height) { canvas.drawLine(0f, gy, width.toFloat(), gy, gridPaint); gy += spacing }
        val ox = screenX(0f)
        val oy = screenY(0f)
        canvas.drawLine(ox, 0f, ox, height.toFloat(), axisPaint)
        canvas.drawLine(0f, oy, width.toFloat(), oy, axisPaint)
        canvas.drawCircle(ox, oy, 4f * density, originPaint)
        canvas.drawText("(0, 0)", ox + 7f * density, oy - 7f * density, textPaint)

        nodes.forEach { node ->
            val cx = screenX(node.x)
            val cy = screenY(node.y)
            val rect = RectF(cx - 22f * density, cy - 15f * density,
                cx + 22f * density, cy + 15f * density)
            val bitmap = textureFor(node.textureUri)
            if (bitmap != null) {
                val w = bitmap.width * density * zoom * node.scaleX.coerceAtLeast(0.05f)
                val h = bitmap.height * density * zoom * node.scaleY.coerceAtLeast(0.05f)
                val destination = RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
                canvas.save()
                canvas.rotate(node.rotation, cx, cy)
                canvas.drawBitmap(bitmap, null, destination, nodePaint)
                canvas.restore()
            } else {
                nodePaint.color = if (node.hasChildren) 0xFF285A58.toInt() else 0xFF29465D.toInt()
                canvas.drawRoundRect(rect, 5f * density, 5f * density, nodePaint)
            }
            if (node.id == selectedId) {
                canvas.drawRoundRect(RectF(rect.left - 3f * density, rect.top - 3f * density,
                    rect.right + 3f * density, rect.bottom + 3f * density),
                    7f * density, 7f * density, selectedPaint)
            }
            val label = if (node.name.length > 16) node.name.take(15) + "…" else node.name
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(label, cx, cy + 4f * density, textPaint)
        }
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = 0xFF91A0B8.toInt()
        textPaint.textSize = 10f * density
        canvas.drawText("DRAG NODE • DRAG EMPTY SPACE TO PAN • PINCH TO ZOOM",
            9f * density, height - 9f * density, textPaint)
        textPaint.color = 0xFFE2EAF4.toInt()
        textPaint.textSize = 11f * density
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x; lastY = event.y; moved = false
                val hit = hitNode(event.x, event.y)
                activeNodeId = hit?.id
                if (hit != null) { selectedId = hit.id; onNodeSelected?.invoke(hit.id) }
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2) {
                    pinchStartDistance = hypot((event.getX(0) - event.getX(1)).toDouble(),
                        (event.getY(0) - event.getY(1)).toDouble()).toFloat().coerceAtLeast(1f)
                    pinchStartZoom = zoom
                    activeNodeId = null
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2) {
                    val distance = hypot((event.getX(0) - event.getX(1)).toDouble(),
                        (event.getY(0) - event.getY(1)).toDouble()).toFloat().coerceAtLeast(1f)
                    zoom = (pinchStartZoom * distance / pinchStartDistance).coerceIn(0.25f, 4f)
                    invalidate()
                    return true
                }
                val dx = event.x - lastX
                val dy = event.y - lastY
                if (abs(dx) + abs(dy) > 1.5f * density) moved = true
                val id = activeNodeId
                if (id != null && moved) onNodeMoved?.invoke(id, dx / (zoom * density), -dy / (zoom * density))
                else if (id == null) { panX += dx; panY += dy; invalidate() }
                lastX = event.x; lastY = event.y
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeNodeId = null
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
