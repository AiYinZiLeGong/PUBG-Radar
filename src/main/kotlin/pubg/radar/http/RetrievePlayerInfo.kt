package pubg.radar.http

import okhttp3.*
import pubg.radar.*
import pubg.radar.http.PlayerProfile.Companion.search
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
  companion object: GameListener {
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
    
    val completedPlayerInfo = ConcurrentHashMap<String, PlayerInfo>()
    val pendingPlayerInfo = ConcurrentHashMap<String, Int>()
    private val baseCount = ConcurrentHashMap<String, Int>()
    val client = OkHttpClient()
    val scheduled = AtomicBoolean(false)
    val running = AtomicBoolean(true)
    
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
            val playerInfo = search(name)
            if (playerInfo == null) {
              baseCount.compute(name) { _, count ->
                count!! - 1
              }
              Thread.sleep(2000)
            } else {
              completedPlayerInfo[name] = playerInfo
              pendingPlayerInfo.remove(name)
            }
          }
        }
    }
    
    fun ee(c: Int, a: Int = base): String {
      val first = if (c < a) ""
      else
        ee(c / a, a)
      
      val c = c % a
      return first + if (c > 35)
        (c + 29).toChar()
      else
        c.toString(36)
    }
    
    val base = 62
    
    fun parseData(p: String, k: List<String>): String {
      var c = k.size
      val d = HashMap<String, String>()
      while (c-- > 0)
        d[ee(c, base)] = if (k[c].isBlank()) ee(c) else k[c]
      return p.replace(Regex("\\b\\w+\\b")) {
        d[it.value] ?: ""
      }
    }
    
    val roundMostKillRegex = Regex("\"records_roundmostkills\":\\s*\"([0-9]+)\"")
    val winRegex = Regex("\"records_wins\":\\s*\"([0-9]+)\"")
    val totalPlayedRegex = Regex("\"records_roundsplayed\":\\s*\"([0-9]+)\"")
    val killDeathRatioRegex = Regex("\"records_killdeathratio\":\\s*\"([0-9.]+)\"")
    val headshotKillRatioRegex = Regex("\"records_headshotkillratio\":\\s*\"([0-9.]+)\"")
    
    fun search(name: String): PlayerInfo? {
      val url = "http://radar.ali213.net/pubg10/ajax?nickname=$name"
      val request = Request.Builder().url(url).build()
      client.newCall(request).execute().use {
        val result = it.body()?.string()
        if (result != null) {
          try {
            val idx = result.indices
            val indices = IntArray(6)
            var found = 0
            for (i in idx.endInclusive - 1 downTo idx.start)
              if (result[i] == '\'') {
                indices[found++] = i
                if (found > indices.lastIndex)
                  break
              }
            val keys = result.substring(indices[3] + 1, indices[2]).split("|")
            val data = result.substring(indices[5] + 1, indices[4])
            val jsonData = parseData(data, keys)
            val exclude = jsonData.lastIndexOf(';')
            val first = jsonData.indexOf('{')
            val json = jsonData.substring(first, exclude)
            return PlayerInfo(roundMostKillRegex.find(json)!!.groups[1]!!.value.toInt(),
                              winRegex.find(json)!!.groups[1]!!.value.toInt(),
                              totalPlayedRegex.find(json)!!.groups[1]!!.value.toInt(),
                              killDeathRatioRegex.find(json)!!.groups[1]!!.value.toFloat(),
                              headshotKillRatioRegex.find(json)!!.groups[1]!!.value.toFloat())
          } catch (e: Exception) {
//            e.printStackTrace()
          }
          
        }
      }
      return null
    }
    
  }
}