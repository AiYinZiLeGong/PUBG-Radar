package wumo.pubg.struct.cmd

import pubg.radar.GameListener
import pubg.radar.bugln
import pubg.radar.register
import pubg.radar.struct.Actor
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetGuidCacheObject
import pubg.radar.struct.cmd.CMD.propertyString
import pubg.radar.struct.cmd.CMD.propertyVector100
import java.util.concurrent.ConcurrentHashMap

object TeamCMD : GameListener {
    val team = ConcurrentHashMap<String, String>()

    init {
        register(this)
    }

    override fun onGameOver() {
        team.clear()
    }

    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        with(bunch) {
            //      println("${actor.netGUID} $waitingHandle")
            when (waitingHandle) {
                5 -> {
                    val (netGUID, obj) = readObject()
                    actor.owner = if (netGUID.isValid()) netGUID else null
                    bugln { " owner: [$netGUID] $obj ---------> beOwned:$actor" }
                }
                16 -> {
                    val playerLocation = propertyVector100()
                }
                17 -> {
                    val playerRotation = readRotationShort()
                }
                18 -> {
                    val playerName = propertyString()
                    team[playerName] = playerName
                }
                else -> return false
            }
            return true
        }
    }
}