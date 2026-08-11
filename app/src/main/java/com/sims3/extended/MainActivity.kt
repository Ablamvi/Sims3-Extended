package com.sims3.extended

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sims3.extended.core.Sim
import com.sims3.extended.core.TimeManager
import com.sims3.extended.objects.SmartObject
import com.sims3.extended.render.GameView

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var txtNeeds: TextView
    private lateinit var txtTime: TextView
    private lateinit var txtMood: TextView

    private lateinit var player: Sim
    private val handler = Handler(Looper.getMainLooper())
    private val uiUpdater = object : Runnable {
        override fun run() {
            updateUI()
            handler.postDelayed(this, 200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)
        txtNeeds = findViewById(R.id.txtNeeds)
        txtTime = findViewById(R.id.txtTime)
        txtMood = findViewById(R.id.txtMood)

        setupWorld()
        handler.post(uiUpdater)
    }

    private fun setupWorld() {
        // Meubles
        SmartObject("Lit", "bed", -4f, -3f, 2f, 0.6f, 1.2f, 0.6f, 0.3f, 0.3f)
        SmartObject("Frigo", "fridge", 3f, -4f, 0.8f, 1.8f, 0.7f, 0.8f, 0.8f, 0.85f)
        SmartObject("Canapé", "sofa", 0f, 2f, 2.2f, 0.7f, 0.9f, 0.2f, 0.3f, 0.6f)
        SmartObject("WC", "toilet", -6f, 1f, 0.5f, 0.7f, 0.6f, 0.9f, 0.9f, 0.95f)
        SmartObject("Douche", "shower", -6f, 3f, 0.9f, 1.8f, 0.9f, 0.5f, 0.7f, 0.9f)
        SmartObject("Télé", "tv", 0f, 4.5f, 1.4f, 0.9f, 0.2f, 0.15f, 0.15f, 0.15f)

        // Sim joueur
        player = Sim("Alex", 0f, 0f).apply {
            isPlayerControlled = true
            colorR = 0.3f
            colorG = 0.5f
            colorB = 0.95f
        }
        gameView.playerSim = player

        TimeManager.timeScale = 2f
    }

    private fun updateUI() {
        val sb = StringBuilder()
        val order = listOf("hunger", "energy", "bladder", "hygiene", "social", "fun", "comfort", "environment")
        for (need in order) {
            val v = player.getNeed(need).toInt()
            val bar = when {
                v < 25 -> "!"
                v < 50 -> "~"
                else -> " "
            }
            sb.append("%s %-12s %3d\n".format(bar, need.replaceFirstChar { it.uppercase() }, v))
        }
        txtNeeds.text = sb.toString().trimEnd()
        txtTime.text = "${TimeManager.getTimeString()} - Jour ${TimeManager.day} (${TimeManager.season})"
        txtMood.text = "Humeur : ${player.getMood()}" +
            if (player.currentAction.isNotEmpty()) " | ${player.currentAction}" else ""
    }

    override fun onDestroy() {
        handler.removeCallbacks(uiUpdater)
        super.onDestroy()
    }
}
