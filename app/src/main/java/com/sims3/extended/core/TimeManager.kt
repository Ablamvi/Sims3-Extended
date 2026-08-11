package com.sims3.extended.core

/**
 * Horloge interne : 1 seconde réelle ≈ 1 minute de jeu
 */
object TimeManager {
    const val REAL_TO_GAME_RATIO = 60f

    var gameMinutes: Int = 8 * 60  // 08:00
        private set
    var day: Int = 1
        private set
    var season: String = "Printemps"
        private set
    var timeScale: Float = 2f
    var paused: Boolean = false

    private var accumulator = 0f
    private val listeners = mutableListOf<(Int) -> Unit>()
    private val dayListeners = mutableListOf<(Int) -> Unit>()

    fun update(deltaSeconds: Float) {
        if (paused) return
        accumulator += deltaSeconds * timeScale * REAL_TO_GAME_RATIO
        while (accumulator >= 1f) {
            accumulator -= 1f
            advanceOneMinute()
        }
    }

    private fun advanceOneMinute() {
        gameMinutes++
        if (gameMinutes >= 24 * 60) {
            gameMinutes = 0
            day++
            dayListeners.forEach { it(day) }
            checkSeason()
        }
        listeners.forEach { it(gameMinutes) }
    }

    private fun checkSeason() {
        val seasons = listOf("Printemps", "Été", "Automne", "Hiver")
        val idx = ((day - 1) / 28) % 4
        val newSeason = seasons[idx]
        if (newSeason != season) season = newSeason
    }

    fun getTimeString(): String {
        val h = gameMinutes / 60
        val m = gameMinutes % 60
        return "%02d:%02d".format(h, m)
    }

    fun getPeriod(): String {
        val h = gameMinutes / 60
        return when {
            h in 6..11 -> "Matin"
            h in 12..17 -> "Après-midi"
            h in 18..21 -> "Soir"
            else -> "Nuit"
        }
    }

    fun isNight(): Boolean {
        val h = gameMinutes / 60
        return h < 6 || h >= 22
    }

    fun onTick(listener: (Int) -> Unit) { listeners.add(listener) }
    fun onDayChanged(listener: (Int) -> Unit) { dayListeners.add(listener) }
}
