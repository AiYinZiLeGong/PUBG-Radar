package pubg.radar.struct.cmd

import pubg.radar.GameListener
import pubg.radar.deserializer.ROLE_MAX
import pubg.radar.register
import pubg.radar.struct.Actor
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetworkGUID
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyInt
import pubg.radar.struct.cmd.CMD.propertyNetId
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyString
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object PlayerStateCMD : GameListener {
    init {
        register(this)
    }

    override fun onGameOver() {
        playerNames.clear()
        playerNumKills.clear()
        uniqueIds.clear()
        teamNumbers.clear()
        attacks.clear()
    }

    val playerNames = ConcurrentHashMap<NetworkGUID, String>()
    val playerNumKills = ConcurrentHashMap<NetworkGUID, Int>()
    val uniqueIds = ConcurrentHashMap<String, NetworkGUID>()
    val teamNumbers = ConcurrentHashMap<NetworkGUID, Int>()
    val attacks = ConcurrentLinkedQueue<Pair<NetworkGUID, NetworkGUID>>()//A -> B
    var selfID = NetworkGUID(0)

    fun process(actor: Actor, bunch: Bunch, waitingHandle: Int): Boolean {
        with(bunch) {
            //item_dbg {"$actor"}
            //item_dbg {""}
            when (waitingHandle) {
                1 -> {
                    val bHidden = readBit()
//          println("bHidden=$bHidden")
                }
                2 -> {
                    val bReplicateMovement = readBit()
//          println("bHidden=$bReplicateMovement")
                }
                3 -> {
                    val bTearOff = readBit()
//          println("bHidden=$bTearOff")
                }
                4 -> {
                    val role = readInt(ROLE_MAX)
                }
                5 -> {
                    val (ownerGUID, owner) = propertyObject()
                }
                7 -> {
                    val (a, obj) = readObject()
                }
                13 -> {
                    readInt(ROLE_MAX)
                }
                16 -> {
                    val score = propertyFloat()
                }
                17 -> {
                    val ping = propertyByte()
                }
                18 -> {
                    val name = propertyString()
                    playerNames[actor.netGUID] = name
                    //query(name)
//          println("${actor.netGUID} playerID=$name")
                }
                19 -> {
                    val playerID = propertyInt()
//          println("${actor.netGUID} playerID=$playerID")
                }
                20 -> {
                    val bIsSpectator = propertyBool()
//        println("${actor.netGUID} bIsSpectator=$bIsSpectator")
                }
                21 -> {
                    val bOnlySpectator = propertyBool()
//        println("${actor.netGUID} bOnlySpectator=$bOnlySpectator")
                }
                22 -> {
                    val isABot = propertyBool()
//        println("${actor.netGUID} isABot=$isABot")
                }
                23 -> {
                    val bIsInactive = propertyBool()
//        println("${actor.netGUID} bIsInactive=$bIsInactive")
                }
                24 -> {
                    val bFromPreviousLevel = propertyBool()
//        println("${actor.netGUID} bFromPreviousLevel=$bFromPreviousLevel")
                }
                25 -> {
                    val StartTime = propertyInt()
//        println("${actor.netGUID} StartTime=$StartTime")
                }
                26 -> {
                    val uniqueId = propertyNetId()
                    uniqueIds[uniqueId] = actor.netGUID
//        println("${playerNames[actor.netGUID]}${actor.netGUID} uniqueId=$uniqueId")
                }
                27 -> {//indicate player's death
                    val Ranking = propertyInt()
//        println("${playerNames[actor.netGUID]}${actor.netGUID} Ranking=$Ranking")
                }
                28 -> {
                    val AccountId = propertyString()
//        println("${actor.netGUID} AccountId=$AccountId")
                }
                29 -> {
                    val ReportToken = propertyString()
//        println("${actor.netGUID} ReportToken=$ReportToken")
                }
                31 -> {
                    val ObserverAuthorityType = readInt(4)
//        println("${playerNames[actor.netGUID]}${actor.netGUID} ObserverAuthorityType=$ObserverAuthorityType")
                }
                32 -> {
                    val teamNumber = readInt(100)
                    teamNumbers[actor.netGUID] = teamNumber
//          println("${playerNames[actor.netGUID]}${actor.netGUID} TeamNumber=$teamNumber")
                }
                33 -> {
                    val bIsZombie = propertyBool()
//          println("bIsZombie=$bIsZombie")
                }
                34 -> {
                    val scoreByDamage = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} scoreByDamage=$scoreByDamage")
                }
                35 -> {
                    val ScoreByKill = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} ScoreByKill=$ScoreByKill")
                }
                36 -> {
                    val ScoreByRanking = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} ScoreByRanking=$ScoreByRanking")
                }
                37 -> {
                    val ScoreFactor = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} ScoreFactor=$ScoreFactor")
                }
                38 -> {
                    val NumKills = propertyInt()
                    playerNumKills[actor.netGUID] = NumKills
//          println("${playerNames[actor.netGUID]}${actor.netGUID} NumKills=$NumKills")
                }
                39 -> {
                    val TotalMovedDistanceMeter = propertyFloat()
                    selfID = actor.netGUID//only self will get this update
//          val NumKills = propertyInt()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} TotalMovedDistanceMeter=$TotalMovedDistanceMeter")
                }
                40 -> {
                    val TotalGivenDamages = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} TotalGivenDamages=$TotalGivenDamages")
                }
                41 -> {
                    val LongestDistanceKill = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} LongestDistanceKill=$LongestDistanceKill")
                }
                42 -> {
                    val HeadShots = propertyInt()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} HeadShots=$HeadShots")
                }
                44 -> {//bIsInAircraft
                    val bIsInAircraft = propertyBool()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} bIsInAircraft=$bIsInAircraft")
                }
                45 -> {//LastHitTime
                    val lastHitTime = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} lastHitTime=$lastHitTime")
                }
                46 -> {
                    val currentAttackerPlayerNetId = propertyString()
                    attacks.add(Pair(uniqueIds[currentAttackerPlayerNetId]!!, actor.netGUID))
//          println("${playerNames[actor.netGUID]}${actor.netGUID} currentAttackerPlayerNetId=$currentAttackerPlayerNetId")

                }
                else -> return false
            }
        }
        return true
    }
}