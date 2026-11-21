package com.wasf.desertquest
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameView = DesertGameView(this)
        setContentView(gameView)
    }
}
class DesertGameView(context: Context) : SurfaceView(context), Runnable, SurfaceHolder.Callback {
    private var running = false
    private var gameThread: Thread? = null
    private var playerY = 0f
    private var velocityY = 0f
    private val paint = Paint()
    init {
        holder.addCallback(this)
    }
    override fun run() {
        while (running) {
            if (!holder.surface.isValid) continue
            val canvas = holder.lockCanvas()
            canvas?.let {
                drawGame(it)
                holder.unlockCanvasAndPost(it)
            }
            updatePhysics()
        }
    }
    private fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#F59E0B"))
        paint.color = Color.parseColor("#0F766E")
        canvas.drawRect(100f, height - 200f - playerY, 200f, height - 100f - playerY, paint)
    }
    private fun updatePhysics() {
        velocityY += 2f
        playerY += velocityY
        if (playerY < 0f) {
            playerY = 0f
            velocityY = 0f
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            velocityY = -30f
        }
        return true
    }
    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        gameThread = Thread(this)
        gameThread?.start()
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        gameThread?.join()
    }
}