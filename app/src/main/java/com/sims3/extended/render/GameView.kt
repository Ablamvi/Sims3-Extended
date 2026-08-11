package com.sims3.extended.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.sims3.extended.core.Sim
import com.sims3.extended.core.TimeManager
import com.sims3.extended.core.WorldManager
import com.sims3.extended.objects.SmartObject

/**
 * Vue de jeu simple (2.5D isométrique / top-down stylisé)
 * Fiable, pas de dépendance native OpenGL pour le prototype
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var playerSim: Sim? = null
    private var cameraX = 0f
    private var cameraZ = 0f
    private val scale = 40f  // pixels par unité monde

    private val groundPaint = Paint().apply {
        color = Color.rgb(55, 90, 40)
        style = Paint.Style.FILL
    }
    private val gridPaint = Paint().apply {
        color = Color.rgb(70, 110, 50)
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val simPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val objPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val shadowPaint = Paint().apply {
        color = Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private var lastTime = System.nanoTime()

    init {
        // Boucle de rendu
        post(object : Runnable {
            override fun run() {
                val now = System.nanoTime()
                val delta = ((now - lastTime) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
                lastTime = now
                tick(delta)
                invalidate()
                postOnAnimation(this)
            }
        })
    }

    private fun tick(delta: Float) {
        TimeManager.update(delta)
        playerSim?.update(delta)
        WorldManager.sims.forEach { if (it != playerSim) it.update(delta) }

        // Caméra suit le joueur
        playerSim?.let {
            cameraX += (it.x - cameraX) * 3f * delta
            cameraZ += (it.z - cameraZ) * 3f * delta
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f

        // Sol
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), groundPaint)

        // Grille
        val gridRange = 15
        for (i in -gridRange..gridRange) {
            val wx1 = worldToScreenX(i.toFloat(), -gridRange.toFloat(), cx)
            val wz1 = worldToScreenY(i.toFloat(), -gridRange.toFloat(), cy)
            val wx2 = worldToScreenX(i.toFloat(), gridRange.toFloat(), cx)
            val wz2 = worldToScreenY(i.toFloat(), gridRange.toFloat(), cy)
            canvas.drawLine(wx1, wz1, wx2, wz2, gridPaint)

            val wx3 = worldToScreenX(-gridRange.toFloat(), i.toFloat(), cx)
            val wz3 = worldToScreenY(-gridRange.toFloat(), i.toFloat(), cy)
            val wx4 = worldToScreenX(gridRange.toFloat(), i.toFloat(), cx)
            val wz4 = worldToScreenY(gridRange.toFloat(), i.toFloat(), cy)
            canvas.drawLine(wx3, wz3, wx4, wz4, gridPaint)
        }

        // Objets
        for (obj in WorldManager.smartObjects) {
            drawObject(canvas, obj, cx, cy)
        }

        // Sims
        for (sim in WorldManager.sims) {
            drawSim(canvas, sim, cx, cy)
        }
    }

    private fun worldToScreenX(wx: Float, wz: Float, cx: Float): Float {
        return cx + (wx - cameraX) * scale
    }

    private fun worldToScreenY(wx: Float, wz: Float, cy: Float): Float {
        return cy + (wz - cameraZ) * scale
    }

    private fun drawObject(canvas: Canvas, obj: SmartObject, cx: Float, cy: Float) {
        val sx = worldToScreenX(obj.x, obj.z, cx)
        val sy = worldToScreenY(obj.x, obj.z, cy)
        val w = obj.sizeX * scale
        val h = obj.sizeZ * scale

        // Ombre
        canvas.drawOval(sx - w / 2, sy + h / 3, sx + w / 2, sy + h / 3 + 12, shadowPaint)

        objPaint.color = Color.rgb(
            (obj.colorR * 255).toInt().coerceIn(0, 255),
            (obj.colorG * 255).toInt().coerceIn(0, 255),
            (obj.colorB * 255).toInt().coerceIn(0, 255)
        )
        val rect = RectF(sx - w / 2, sy - h / 2 - obj.sizeY * scale * 0.3f, sx + w / 2, sy + h / 2)
        canvas.drawRoundRect(rect, 8f, 8f, objPaint)

        textPaint.textSize = 22f
        canvas.drawText(obj.objectName, sx, sy - h / 2 - obj.sizeY * scale * 0.3f - 8, textPaint)
    }

    private fun drawSim(canvas: Canvas, sim: Sim, cx: Float, cy: Float) {
        val sx = worldToScreenX(sim.x, sim.z, cx)
        val sy = worldToScreenY(sim.x, sim.z, cy)

        canvas.drawOval(sx - 18, sy + 10, sx + 18, sy + 22, shadowPaint)

        simPaint.color = Color.rgb(
            (sim.colorR * 255).toInt().coerceIn(0, 255),
            (sim.colorG * 255).toInt().coerceIn(0, 255),
            (sim.colorB * 255).toInt().coerceIn(0, 255)
        )
        // Corps
        canvas.drawCircle(sx, sy - 20, 22f, simPaint)
        // Tête
        simPaint.color = Color.rgb(255, 220, 180)
        canvas.drawCircle(sx, sy - 48, 16f, simPaint)

        textPaint.textSize = 24f
        canvas.drawText(sim.displayName, sx, sy + 40, textPaint)
        if (sim.currentAction.isNotEmpty()) {
            textPaint.textSize = 18f
            textPaint.color = Color.YELLOW
            canvas.drawText(sim.currentAction, sx, sy + 58, textPaint)
            textPaint.color = Color.WHITE
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val cx = width / 2f
            val cy = height / 2f
            // Convertir écran → monde
            val worldX = cameraX + (event.x - cx) / scale
            val worldZ = cameraZ + (event.y - cy) / scale

            // Cliquer sur un objet proche ?
            val nearby = WorldManager.getObjectsInRadius(worldX, worldZ, 1.5f)
            if (nearby.isNotEmpty()) {
                val obj = nearby.first()
                val actions = obj.getAdvertisedActions(playerSim ?: return true)
                if (actions.isNotEmpty()) {
                    playerSim?.queueAction(actions.first())
                }
            } else {
                // Déplacer le Sim
                playerSim?.moveTo(worldX, worldZ)
            }
            return true
        }
        return super.onTouchEvent(event)
    }
}
