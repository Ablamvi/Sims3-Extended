package com.sims3.extended.ai

import com.sims3.extended.core.Sim
import com.sims3.extended.core.WorldManager

/**
 * Utility AI – autonomie des Sims
 */
class UtilityAI(private val sim: Sim) {
    var enabled = true
    private var timer = 0f
    private val interval = 2.5f

    fun update(delta: Float) {
        if (!enabled || sim.isPlayerControlled) return
        if (sim.currentAction.isNotEmpty()) return

        timer += delta
        if (timer < interval) return
        timer = 0f

        evaluateAndAct()
    }

    private fun evaluateAndAct() {
        val critical = sim.getMostCriticalNeed()
        var bestScore = -999f
        var bestAction: Map<String, Any>? = null

        val nearby = WorldManager.getObjectsInRadius(sim.x, sim.z, 25f)
        for (obj in nearby) {
            for (action in obj.getAdvertisedActions(sim)) {
                val score = scoreAction(action, critical)
                if (score > bestScore) {
                    bestScore = score
                    bestAction = action
                }
            }
        }

        if (bestAction != null && bestScore > 10f) {
            sim.queueAction(bestAction)
        }
    }

    private fun scoreAction(action: Map<String, Any>, critical: String): Float {
        var score = 0f
        @Suppress("UNCHECKED_CAST")
        val effects = action["need_effects"] as? Map<String, Float> ?: return 0f

        effects[critical]?.let { effect ->
            val current = sim.getNeed(critical)
            score += (100f - current) * 0.5f + effect * 0.8f
        }
        for ((need, effect) in effects) {
            if (need != critical) score += effect * 0.2f
        }
        return score
    }
}
