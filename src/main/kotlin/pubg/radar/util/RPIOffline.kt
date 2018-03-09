package pubg.radar.util

import pubg.radar.GameListener
import pubg.radar.register
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

data class PlayerInfo(
        val roundMostKill: Int,
        val win: Int,
        val totalPlayed: Int,
        val killDeathRatio: Float,
        val headshotKillRatio: Float)

class PlayerProfile {
    companion object : GameListener {
        init {
            register(this)
        }

        override fun onGameStart() {
            running.set(true)
            scheduled.set(false)
        }

        override fun onGameOver() {
            running.set(false)
            completedPlayerInfo.clear()
            pendingPlayerInfo.clear()
            baseCount.clear()
        }

        private val completedPlayerInfo = ConcurrentHashMap<String, PlayerInfo>()
        private val pendingPlayerInfo = ConcurrentHashMap<String, Int>()
        private val baseCount = ConcurrentHashMap<String, Int>()
        private val scheduled = AtomicBoolean(false)
        private val running = AtomicBoolean(true)
        private const val base = 62


        fun query(name: String) {
            if (completedPlayerInfo.containsKey(name)) return
            baseCount.putIfAbsent(name, 0)
            pendingPlayerInfo.compute(name) { _, count ->
                (count ?: 0) + 1
            }
            if (scheduled.compareAndSet(false, true))
                thread(isDaemon = true) {
                    while (running.get()) {
                        var next = pendingPlayerInfo.maxBy { it.value + baseCount[it.key]!! }
                        if (next == null) {
                            scheduled.set(false)
                            next = pendingPlayerInfo.maxBy { it.value + baseCount[it.key]!! }
                            if (next == null || !scheduled.compareAndSet(false, true))
                                break
                        }
                        val (name) = next
                        if (completedPlayerInfo.containsKey(name)) {
                            pendingPlayerInfo.remove(name)
                            continue
                        }
                    }
                } else {
                return
            }
        }
    }

    private fun ee(c: Int, a: Int = base): String {
        val first = if (c < a) ""
        else ee(c / a, a)

        val c = c % a
        return first + if (c > 35)
            (c + 29).toChar()
        else c.toString(36)
    }

    private fun parseData(p: String, k: List<String>): String {
        var c = k.size
        val d = HashMap<String, String>()
        while (c-- > 0)
            d[ee(c, base)] = if (k[c].isBlank()) ee(c) else k[c]
        return p.replace(Regex("\\b\\w+\\b")) {
            d[it.value] ?: ""
        }
    }
}