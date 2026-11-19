package com.nexus.hub

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private external fun nativeEngineVersion(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(9, 13, 22)
        window.navigationBarColor = Color.rgb(9, 13, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(24))
            setBackgroundColor(Color.rgb(9, 13, 22))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        scroll.addView(content)

        content.addView(TextView(this).apply {
            text = "NEXUS"
            textSize = 34f
            setTextColor(Color.rgb(99, 230, 190))
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            gravity = Gravity.CENTER
        }, matchWrap())
        content.addView(TextView(this).apply {
            text = "ENGINE HUB  •  EARLY ACCESS"
            textSize = 12f
            letterSpacing = 0.16f
            setTextColor(Color.rgb(145, 160, 184))
            gravity = Gravity.CENTER
        }, matchWrap())
        addSpace(content, 34)
        content.addView(TextView(this).apply {
            text = "Build something\nthat exists only in your imagination."
            textSize = 25f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            gravity = Gravity.CENTER
        }, matchWrap())
        addSpace(content, 12)
        content.addView(TextView(this).apply {
            text = "Your workspace for creating games with NEXUS ENGINE."
            textSize = 14f
            setTextColor(Color.rgb(166, 178, 198))
            gravity = Gravity.CENTER
        }, matchWrap())
        addSpace(content, 32)

        addAction(content, "＋   CREATE PROJECT") {
            Toast.makeText(this, "Project creation will be available in a future update.", Toast.LENGTH_LONG).show()
        }
        addSpace(content, 12)
        addAction(content, "OPEN PROJECT") {
            Toast.makeText(this, "Project browser is coming in a future update.", Toast.LENGTH_LONG).show()
        }
        addSpace(content, 12)
        addAction(content, "DOCUMENTATION") {
            Toast.makeText(this, "NEXUS ENGINE documentation is under construction.", Toast.LENGTH_LONG).show()
        }
        addSpace(content, 36)

        val nativeStatus = try {
            System.loadLibrary("nexus_android")
            nativeEngineVersion() + "\nNative C++ bridge: connected"
        } catch (error: UnsatisfiedLinkError) {
            "NEXUS ENGINE 0.1.0\nNative C++ bridge: unavailable"
        }
        content.addView(TextView(this).apply {
            text = nativeStatus
            textSize = 12f
            setTextColor(Color.rgb(99, 230, 190))
            gravity = Gravity.CENTER
        }, matchWrap())
        addSpace(content, 24)
        content.addView(TextView(this).apply {
            text = "NEXUS ENGINE  •  VERSION 0.1.0"
            textSize = 10f
            setTextColor(Color.rgb(105, 119, 143))
            gravity = Gravity.CENTER
        }, matchWrap())

        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
    }

    private fun addAction(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.rgb(9, 13, 22))
            setBackgroundColor(Color.rgb(99, 230, 190))
            minHeight = dp(54)
            setOnClickListener { action() }
        }
        parent.addView(button, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(54)
        ))
    }

    private fun addSpace(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
