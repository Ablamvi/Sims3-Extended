package com.sims3.extended.core

import com.sims3.extended.objects.SmartObject
import kotlin.math.sqrt

/**
 * Sim Core – besoins, traits, moodlets, compétences, file d'actions
 */
class Sim(
    var displayName: String = "Alex",
    var x: Float = 0f,
    var z: Float = 0f
) {
    // Besoins 0-100
    val needs = mutableMapOf(
        "hunger" to 80f,
        "energy" to 90f,
        "bladder" to 70f,
        "hygiene" to 85f,
        "social" to 60f,
        "fun" to 75f,
        "comfort" to 80f,
        "environment" to 70f
    )

    val decayRates = mutableMapOf(
        "hunger" to 0.15f,
        "energy" to 0.08f,
        "bladder" to 0.12f,
        "hygiene" to 0.06f,
        "social" to 0.10f,
        "fun" to 0.11f,
        "comfort" to 0.04f,
        "environment" to 0.03f
    )

    var traits = mutableListOf("Amical", "Créatif")
    var isPlayerControlled = true
    var currentAction: String = ""
    var colorR = 0.4f
    var colorG = 0.6f
    var colorB = 0.9f

    // Mouvement
    private var targetX = 0f
    private var targetZ = 0f
    private var isMoving = false
    private val moveSpeed = 3.5f

    private val actionQueue = mutableListOf<Map<String, Any>>()
    private var actionTimer = 0f
    private var pendingEffects: Map<String, Float>? = null

    init {
        WorldManager.registerSim(this)
        TimeManager.onTick { onTimeTick() }
    }

    fun onTimeTick() {
        decayNeeds()
        processActionQueue()
    }

    private fun decayNeeds() {
        for ((need, rate) in decayRates) {
            var r = rate
            if ("Gourmand" in traits && need == "hunger") r *= 1.3f
            needs[need] = (needs[need]!! - r).coerceIn(0f, 100f)
        }
    }

    fun getMostCriticalNeed(): String {
        return needs.minByOrNull { it.value }?.key ?: "hunger"
    }

    fun getMood(): String {
        val avg = needs.values.average()
        return when {
            avg >= 80 -> "Heureux"
            avg >= 50 -> "Neutre"
            avg >= 25 -> "Malheureux"
            else -> "Misérable"
        }
    }

    fun getNeed(name: String): Float = needs[name] ?: 50f

    fun moveTo(tx: Float, tz: Float) {
        targetX = tx
        targetZ = tz
        isMoving = true
        currentAction = "Se déplacer"
    }

    fun queueAction(action: Map<String, Any>) {
        actionQueue.add(action)
    }

    fun update(delta: Float) {
        // Mouvement
        if (isMoving) {
            val dx = targetX - x
            val dz = targetZ - z
            val dist = sqrt(dx * dx + dz * dz)
            if (dist < 0.3f) {
                isMoving = false
                x = targetX
                z = targetZ
                finishMove()
            } else {
                val step = moveSpeed * delta
                x += dx / dist * step
                z += dz / dist * step
            }
        }

        // Action en cours (sur place)
        if (actionTimer > 0f) {
            actionTimer -= delta
            if (actionTimer <= 0f) {
                pendingEffects?.forEach { (need, value) ->
                    needs[need] = (needs[need]!! + value).coerceIn(0f, 100f)
                }
                pendingEffects = null
                currentAction = ""
            }
        }
    }

    private fun finishMove() {
        // Si une action attendait l'arrivée
        if (actionQueue.isNotEmpty() && currentAction == "Se déplacer") {
            currentAction = ""
        } else {
            currentAction = ""
        }
    }

    private fun processActionQueue() {
        if (currentAction.isNotEmpty() || isMoving || actionQueue.isEmpty()) return
        val action = actionQueue.removeAt(0)
        startAction(action)
    }

    private fun startAction(action: Map<String, Any>) {
        currentAction = action["name"] as? String ?: "Action"
        val tx = action["targetX"] as? Float
        val tz = action["targetZ"] as? Float
        @Suppress("UNCHECKED_CAST")
        val effects = action["need_effects"] as? Map<String, Float>

        if (tx != null && tz != null) {
            pendingEffects = effects
            // Durée après arrivée
            val duration = (action["duration"] as? Number)?.toFloat() ?: 5f
            actionTimer = duration * 0.15f  // accéléré pour prototype
            moveTo(tx, tz)
        } else {
            pendingEffects = effects
            val duration = (action["duration"] as? Number)?.toFloat() ?: 5f
            actionTimer = duration * 0.15f
        }
    }

    fun interactWithNearest(radius: Float = 3f) {
        val nearby = WorldManager.getObjectsInRadius(x, z, radius)
        if (nearby.isNotEmpty()) {
            val obj = nearby.first()
            val actions = obj.getAdvertisedActions(this)
            if (actions.isNotEmpty()) {
                queueAction(actions.first())
            }
        }
    }
}
