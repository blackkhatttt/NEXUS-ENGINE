package com.nexus.hub

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

class GameRuntimeActivity : Activity() {
    companion object {
        const val EXTRA_SCENE_URI = "nexus.runtime.scene_uri"
        const val EXTRA_PROJECT_NAME = "nexus.runtime.project_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterImmersiveMode()

        val sceneText = try {
            val uri = Uri.parse(intent.getStringExtra(EXTRA_SCENE_URI) ?: "")
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: throw IllegalStateException("Scene file could not be opened.")
        } catch (error: Exception) {
            Toast.makeText(this, "Could not load scene: ${error.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val root = try {
            JSONObject(sceneText).getJSONObject("root")
        } catch (_: Exception) {
            Toast.makeText(this, "Invalid NEXUS scene document.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val frame = FrameLayout(this)
        frame.setBackgroundColor(Color.rgb(12, 18, 29))
        frame.addView(GameRuntimeView(this, root), FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        val exit = TextView(this).apply {
            text = "✕  EXIT"
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(9), dp(16), dp(9))
            setBackgroundColor(0xAA101927.toInt())
            setOnClickListener { finish() }
        }
        frame.addView(exit, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END
        ).apply { topMargin = dp(14); marginEnd = dp(14) })

        val title = TextView(this).apply {
            text = intent.getStringExtra(EXTRA_PROJECT_NAME) ?: "NEXUS GAME"
            textSize = 10f
            letterSpacing = 0.12f
            setTextColor(0x99FFFFFF.toInt())
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        frame.addView(title, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START
        ))
        setContentView(frame)
    }

    @Suppress("DEPRECATION")
    private fun enterImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private class RuntimeElement(
        val name: String,
        val type: String,
        val x: Float,
        val y: Float,
        val scaleX: Float,
        val scaleY: Float,
        val components: List<String>,
        val textureUri: String
    )

    private class GameRuntimeView(context: android.content.Context, root: JSONObject) : View(context) {
        private val density = resources.displayMetrics.density
        private val elements = mutableListOf<RuntimeElement>()
        private val textures = mutableMapOf<String, Bitmap>()
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 13f * density
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }
        private var pressedButton = ""
        private var activeControl = ""
        private var joystickX = 0f
        private var joystickY = 0f
        private var arrowDirection = ""

        init {
            collect(root, 0f, 0f, 1f, 1f)
            isFocusable = true
        }

        private fun collect(node: JSONObject, parentX: Float, parentY: Float, parentScaleX: Float, parentScaleY: Float) {
            val x = parentX + node.optDouble("x", 0.0).toFloat() * parentScaleX
            val y = parentY + node.optDouble("y", 0.0).toFloat() * parentScaleY
            val sx = parentScaleX * node.optDouble("scale_x", 1.0).toFloat()
            val sy = parentScaleY * node.optDouble("scale_y", 1.0).toFloat()
            val components = mutableListOf<String>()
            val array = node.optJSONArray("control_components") ?: JSONArray()
            for (i in 0 until array.length()) {
                val value = array.optString(i)
                if (value in listOf("Button", "Joystick", "Arrows", "Panel")) components.add(value)
            }
            elements.add(RuntimeElement(
                node.optString("name", "Node"),
                node.optString("type", "Node2D"),
                x, y, sx, sy, components, node.optString("texture_uri", "")
            ))
            val children = node.optJSONArray("children") ?: JSONArray()
            for (i in 0 until children.length()) {
                children.optJSONObject(i)?.let { collect(it, x, y, sx, sy) }
            }
        }

        private fun textureFor(uriText: String): Bitmap? {
            if (uriText.isBlank()) return null
            textures[uriText]?.let { return it }
            val bitmap = try {
                context.contentResolver.openInputStream(Uri.parse(uriText))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
                null
            }
            if (bitmap != null) textures[uriText] = bitmap
            return bitmap
        }

        private fun centerX(element: RuntimeElement) = width / 2f + element.x * density
        private fun centerY(element: RuntimeElement) = height / 2f - element.y * density
        private fun rectFor(element: RuntimeElement, component: String): RectF {
            val cx = centerX(element)
            val cy = centerY(element)
            val w = when (component) {
                "Button" -> 132f
                "Joystick" -> 112f
                "Arrows" -> 116f
                "Panel" -> 190f
                else -> 64f
            } * density * max(0.4f, element.scaleX)
            val h = when (component) {
                "Button" -> 54f
                "Joystick" -> 112f
                "Arrows" -> 116f
                "Panel" -> 140f
                else -> 48f
            } * density * max(0.4f, element.scaleY)
            return RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(Color.rgb(12, 18, 29))
            val grid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x182F455D
                strokeWidth = density
            }
            val step = 40f * density
            var gx = width / 2f % step
            while (gx < width) { canvas.drawLine(gx, 0f, gx, height.toFloat(), grid); gx += step }
            var gy = height / 2f % step
            while (gy < height) { canvas.drawLine(0f, gy, width.toFloat(), gy, grid); gy += step }

            elements.forEach { element ->
                val cx = centerX(element)
                val cy = centerY(element)
                if (element.type == "Sprite2D") {
                    val bitmap = textureFor(element.textureUri)
                    if (bitmap != null) {
                        val w = bitmap.width * density * max(0.05f, element.scaleX)
                        val h = bitmap.height * density * max(0.05f, element.scaleY)
                        val destination = RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
                        paint.alpha = 255
                        canvas.drawBitmap(bitmap, null, destination, paint)
                    } else {
                        val w = 72f * density * max(0.2f, element.scaleX)
                        val h = 56f * density * max(0.2f, element.scaleY)
                        paint.color = 0xFF9A4D58.toInt()
                        canvas.drawRoundRect(RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f),
                            6f * density, 6f * density, paint)
                        textPaint.textSize = 9f * density
                        canvas.drawText("NO TEXTURE", cx, cy + 3f * density, textPaint)
                    }
                } else if (element.type != "Control") {
                    val w = 64f * density * max(0.2f, element.scaleX)
                    val h = 48f * density * max(0.2f, element.scaleY)
                    paint.color = if (element.name.equals("Player", true)) 0xFF63E6BE.toInt() else 0xFF42627F.toInt()
                    canvas.drawRoundRect(RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f),
                        8f * density, 8f * density, paint)
                    textPaint.textSize = 11f * density
                    canvas.drawText(element.name, cx, cy + 4f * density, textPaint)
                }

                element.components.forEach { component ->
                    val rect = rectFor(element, component)
                    when (component) {
                        "Button" -> {
                            paint.color = if (pressedButton == element.name) 0xFF2B8D78.toInt() else 0xFF176B66.toInt()
                            canvas.drawRoundRect(rect, 12f * density, 12f * density, paint)
                            textPaint.textSize = 14f * density
                            canvas.drawText(if (pressedButton == element.name) "PRESSED" else element.name.ifBlank { "BUTTON" },
                                rect.centerX(), rect.centerY() + 5f * density, textPaint)
                        }
                        "Joystick" -> {
                            paint.color = 0x884C6D89.toInt()
                            canvas.drawCircle(rect.centerX(), rect.centerY(), min(rect.width(), rect.height()) * 0.48f, paint)
                            paint.color = 0xFF63E6BE.toInt()
                            val radius = min(rect.width(), rect.height()) * 0.22f
                            canvas.drawCircle(rect.centerX() + joystickX * radius, rect.centerY() + joystickY * radius,
                                radius * 0.58f, paint)
                        }
                        "Arrows" -> {
                            paint.color = 0xAA344B63.toInt()
                            canvas.drawRoundRect(rect, 14f * density, 14f * density, paint)
                            textPaint.textSize = 20f * density
                            canvas.drawText("▲", rect.centerX(), rect.top + rect.height() * 0.29f, textPaint)
                            canvas.drawText("◀   ▼   ▶", rect.centerX(), rect.centerY() + 7f * density, textPaint)
                            textPaint.textSize = 9f * density
                            canvas.drawText(arrowDirection.ifBlank { "DIRECTION PAD" }, rect.centerX(), rect.bottom - 8f * density, textPaint)
                        }
                        "Panel" -> {
                            paint.color = 0xAA192B40.toInt()
                            canvas.drawRoundRect(rect, 12f * density, 12f * density, paint)
                            textPaint.textSize = 12f * density
                            canvas.drawText(element.name, rect.centerX(), rect.centerY() + 4f * density, textPaint)
                        }
                    }
                }
            }
            textPaint.textSize = 10f * density
            textPaint.color = 0x88FFFFFF.toInt()
            canvas.drawText("NEXUS RUNTIME", width / 2f, height - 18f * density, textPaint)
            textPaint.color = Color.WHITE
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val x = event.x
                    val y = event.y
                    val target = elements.asReversed().firstNotNullOfOrNull { element ->
                        element.components.firstOrNull { component ->
                            component != "Panel" && rectFor(element, component).contains(x, y)
                        }?.let { element to it }
                    }
                    activeControl = target?.first?.name.orEmpty()
                    when (target?.second) {
                        "Button" -> pressedButton = target.first.name
                        "Joystick" -> {
                            val rect = rectFor(target.first, "Joystick")
                            joystickX = ((x - rect.centerX()) / (rect.width() * 0.22f)).coerceIn(-1f, 1f)
                            joystickY = ((y - rect.centerY()) / (rect.height() * 0.22f)).coerceIn(-1f, 1f)
                        }
                        "Arrows" -> {
                            val rect = rectFor(target.first, "Arrows")
                            val nx = (x - rect.centerX()) / rect.width()
                            val ny = (y - rect.centerY()) / rect.height()
                            arrowDirection = when {
                                ny < -0.16f -> "UP"
                                ny > 0.16f -> "DOWN"
                                nx < -0.12f -> "LEFT"
                                nx > 0.12f -> "RIGHT"
                                else -> "CENTER"
                            }
                        }
                    }
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    pressedButton = ""
                    activeControl = ""
                    joystickX = 0f
                    joystickY = 0f
                    arrowDirection = ""
                    invalidate()
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

}
