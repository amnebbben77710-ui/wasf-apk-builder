package com.wasf.desertquest

import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.View
import kotlin.random.Random

class GameView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lastFrameTime = System.nanoTime()
    private var worldInitialized = false

    var gameState = 0 // 0:MENU, 1:PLAYING, 2:PAUSED, 3:GAMEOVER
    var score = 0
    var lives = 3

    private var heroY = 0f
    private var heroVelY = 0f
    private val gravity = 2500f
    private val jumpForce = -1100f
    private var groundY = 0f
    private var obstacleX = 0f
    private var coinX = 0f
    private var coinY = 0f

    private val toneGen = try { ToneGenerator(AudioManager.STREAM_MUSIC, 80) } catch (e: Exception) { null }

    init {
        isFocusable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            groundY = h * 0.8f
            initWorld()
        }
    }

    fun initWorld() {
        heroY = groundY - 100f
        obstacleX = width.toFloat()
        coinX = width.toFloat() + 200f
        coinY = groundY - 200f
        worldInitialized = true
    }

    fun startNewGame() {
        score = 0
        lives = 3
        heroY = groundY - 100f
        obstacleX = width.toFloat()
        gameState = 1
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (width <= 0 || height <= 0 || !worldInitialized) return

        val now = System.nanoTime()
        val dt = ((now - lastFrameTime) / 1_000_000_000f).coerceIn(0.01f, 0.05f)
        lastFrameTime = now

        canvas.drawColor(Color.parseColor("#FDE68A"))
        paint.color = Color.parseColor("#D97706")
        canvas.drawRect(0f, groundY, width.toFloat(), height.toFloat(), paint)

        if (gameState == 1) {
            heroVelY += gravity * dt
            heroY += heroVelY * dt
            if (heroY > groundY - 100f) {
                heroY = groundY - 100f
                heroVelY = 0f
            }

            obstacleX -= 600f * dt
            if (obstacleX < -100f) {
                obstacleX = width.toFloat() + Random.nextInt(100, 500).toFloat()
                score += 5
            }

            coinX -= 500f * dt
            if (coinX < -100f) {
                coinX = width.toFloat() + 800f
                coinY = groundY - 300f
            }

            if (obstacleX < 150f && obstacleX > 50f && heroY > groundY - 150f) {
                lives--
                obstacleX = width.toFloat() + 300f
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                if (lives <= 0) gameState = 3
            }

            if (coinX < 150f && coinX > 50f && heroY > coinY - 100f) {
                score += 20
                coinX = -500f
            }
        }

        paint.color = Color.parseColor("#0F766E")
        canvas.drawRect(50f, heroY, 150f, heroY + 100f, paint)
        paint.color = Color.parseColor("#BE123C")
        canvas.drawRect(obstacleX, groundY - 80f, obstacleX + 80f, groundY, paint)
        paint.color = Color.parseColor("#EAB308")
        canvas.drawCircle(coinX, coinY, 30f, paint)

        postInvalidateOnAnimation()
    }

    fun jump() {
        if (gameState == 1 && heroY >= groundY - 105f) {
            heroVelY = jumpForce
        }
    }
}