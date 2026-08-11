package com.sims3.extended.core

import com.sims3.extended.objects.SmartObject

/**
 * Carte globale unifiée + registre central
 */
object WorldManager {
    val sims = mutableListOf<Sim>()
    val smartObjects = mutableListOf<SmartObject>()

    fun registerSim(sim: Sim) {
        if (sim !in sims) {
            sims.add(sim)
        }
    }

    fun unregisterSim(sim: Sim) {
        sims.remove(sim)
    }

    fun registerObject(obj: SmartObject) {
        if (obj !in smartObjects) {
            smartObjects.add(obj)
        }
    }

    fun getObjectsInRadius(x: Float, z: Float, radius: Float): List<SmartObject> {
        return smartObjects.filter {
            val dx = it.x - x
            val dz = it.z - z
            dx * dx + dz * dz <= radius * radius
        }
    }
}
