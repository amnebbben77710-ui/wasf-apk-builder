package com.wasf.desertquest

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var gameView: GameView
    private lateinit var hud: TextView
    private lateinit var menuOverlay: LinearLayout

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#0F172A")

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor("#0F172A"))

        gameView = GameView(this)
        root.addView(gameView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        hud = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(dp(16), dp(8), dp(16), dp(8))
            text = "النقاط: 0"
        }
        root.addView(hud, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.TOP or Gravity.START })

        val btnBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(16f).toFloat()
            setColor(Color.parseColor("#059669"))
        }

        val jumpBtn = Button(this).apply {
            text = "⬆ قفز"
            setBackground(btnBackground)
            setTextColor(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(dp(120), dp(80)).apply { gravity = Gravity.BOTTOM or Gravity.END; bottomMargin = dp(20); rightMargin = dp(20) }
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    gameView.jump()
                    hud.text = "النقاط: ${gameView.score}"
                }
                true
            }
        }
        root.addView(jumpBtn)

        menuOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#CC0F172A"))
            setPadding(dp(30), dp(30), dp(30), dp(30))
            val title = TextView(this.context).apply { text = "مغامرة الصحراء"; textSize = 32f; setTextColor(Color.WHITE) }
            val startBtn = Button(this.context).apply {
                text = "ابدأ اللعب"
                setBackground(btnBackground)
                setTextColor(Color.WHITE)
                setOnClickListener { 
                    menuOverlay.visibility = View.GONE
                    gameView.startNewGame()
                }
            }
            addView(title)
            addView(startBtn)
        }
        root.addView(menuOverlay, FrameLayout.LayoutParams(dp(300), dp(250)).apply { gravity = Gravity.CENTER })

        setContentView(root)
    }
    
    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}