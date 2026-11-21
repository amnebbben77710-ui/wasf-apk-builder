package com.wasf.mygame

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameView = RacingGameView(this)
        setContentView(gameView)
    }
}

class RacingGameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    private var thread: Thread? = null
    private var running = false
    private val paint = Paint()
    private var playerX = 500f
    private var carSpeed = 20f

    init {
        holder.addCallback(this)
    }

    override fun run() {
        while (running) {
            if (holder.surface.isValid) {
                val canvas = holder.lockCanvas()
                draw(canvas)
                holder.unlockCanvasAndPost(canvas)
                update()
            }
        }
    }

    private fun update() {
        playerX += carSpeed
        if (playerX > width - 100 || playerX < 0) carSpeed *= -1
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawColor(Color.parseColor("#0F172A"))
        paint.color = Color.parseColor("#334155")
        canvas.drawRect(playerX, height - 300f, playerX + 100f, height - 100f, paint)
        paint.color = Color.WHITE
        paint.textSize = 60f
        canvas.drawText("سباق الأسفلت 2D", 50f, 100f, paint)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread = Thread(this)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        thread?.join()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            carSpeed *= -1
        }
        return true
    }
}