package com.sims3.extended.objects

import com.sims3.extended.core.Sim
import com.sims3.extended.core.WorldManager

/**
 * Objets intelligents qui publient des actions
 */
class SmartObject(
    val objectName: String,
    val objectType: String,
    var x: Float,
    var z: Float,
    var sizeX: Float = 1f,
    var sizeY: Float = 1f,
    var sizeZ: Float = 1f,
    var colorR: Float = 0.5f,
    var colorG: Float = 0.5f,
    var colorB: Float = 0.5f
) {
    private val actions: List<Map<String, Any>>

    init {
        actions = when (objectType) {
            "bed" -> listOf(
                mapOf("name" to "Dormir", "need_effects" to mapOf("energy" to 40f, "comfort" to 20f), "duration" to 30),
                mapOf("name" to "S'allonger", "need_effects" to mapOf("energy" to 10f, "comfort" to 15f), "duration" to 10)
            )
            "fridge" -> listOf(
                mapOf("name" to "Manger", "need_effects" to mapOf("hunger" to 35f), "duration" to 8),
                mapOf("name" to "Collation", "need_effects" to mapOf("hunger" to 15f), "duration" to 4)
            )
            "sofa" -> listOf(
                mapOf("name" to "S'asseoir", "need_effects" to mapOf("comfort" to 20f, "fun" to 5f), "duration" to 10),
                mapOf("name" to "Regarder la télé", "need_effects" to mapOf("fun" to 25f, "comfort" to 10f), "duration" to 15)
            )
            "toilet" -> listOf(
                mapOf("name" to "Toilettes", "need_effects" to mapOf("bladder" to 50f), "duration" to 5)
            )
            "shower" -> listOf(
                mapOf("name" to "Douche", "need_effects" to mapOf("hygiene" to 45f, "comfort" to 10f), "duration" to 10)
            )
            "tv" -> listOf(
                mapOf("name" to "Télé", "need_effects" to mapOf("fun" to 30f), "duration" to 20)
            )
            else -> listOf(
                mapOf("name" to "Interagir", "need_effects" to mapOf("fun" to 5f), "duration" to 5)
            )
        }
        WorldManager.registerObject(this)
    }

    fun getAdvertisedActions(sim: Sim): List<Map<String, Any>> {
        return actions.map { action ->
            action.toMutableMap().apply {
                this["targetX"] = x
                this["targetZ"] = z
            }
        }
    }
}
