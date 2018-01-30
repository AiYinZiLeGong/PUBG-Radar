package pubg.radar.struct.cmd

import pubg.radar.*
import pubg.radar.http.PlayerProfile.Companion.query
import pubg.radar.struct.*
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyString
import java.util.concurrent.ConcurrentHashMap

object PlayerStateCMD: GameListener {
  init {
    register(this)
  }
  
  override fun onGameOver() {
    playerStateAndNames.clear()
  }
  
  val playerStateAndNames = ConcurrentHashMap<NetworkGUID, String>()
  
  fun process(actor: Actor, bunch: Bunch, waitingHandle: Int): Boolean {
    with(bunch) {
      when (waitingHandle) {
        5 -> {
          val (ownerGUID, owner) = propertyObject()
        }
        16 -> {
          val score = propertyFloat()
        }
        17 -> {
          val ping = propertyByte()
        }
        18 -> {
          val name = propertyString()
          playerStateAndNames[actor.netGUID] = name
          query(name)
//          println("${actor.netGUID} playerID=$name")
        }
//        19 -> {
//          val playerID = propertyInt()
////          println("${actor.netGUID} playerID=$playerID")
//        }
//        20 -> {
//          val bFromPreviousLevel = propertyBool()
////          println("${actor.netGUID} bFromPreviousLevel=$bFromPreviousLevel")
//        }
//        21 -> {
//          val isABot = propertyBool()
////          println("${actor.netGUID} isABot=$isABot")
//        }
//        22 -> {
//          val bIsInactive = propertyBool()
////          println("${actor.netGUID} bIsInactive=$bIsInactive")
//        }
//        23 -> {
//          val bIsSpectator = propertyBool()
////          println("${actor.netGUID} bIsSpectator=$bIsSpectator")
//        }
//        24 -> {
//          val bOnlySpectator = propertyBool()
////          println("${actor.netGUID} bOnlySpectator=$bOnlySpectator")
//        }
//        25 -> {
//          val StartTime = propertyInt()
////          println("${actor.netGUID} StartTime=$StartTime")
//        }
//        26 -> {
//          val uniqueId = propertyNetId()
////          println("${actor.netGUID} uniqueID=$uniqueId")
//        }
//        27 -> {
//          val Ranking = propertyInt()
////          println("${actor.netGUID} Ranking=$Ranking")
//        }
//        28 -> {
//          val AccountId = propertyString()
////          println("${actor.netGUID} AccountId=$AccountId")
//        }
//        29 -> {
//          return false
//        }
//        31 -> {
//          val itemType = propertyByte()
//          println("${actor.netGUID} itemType=$itemType")
//        }
//        32-> {
//          val ItemCount = propertyInt()
//          println("${actor.netGUID} ItemCount=$ItemCount")
//        }
//        34-> {
//          val ObserverAuthorityType = propertyByte()
//          println("${actor.netGUID} ObserverAuthorityType=$ObserverAuthorityType")
//        }
//        35 -> {
//          val TeamNumber = propertyInt()
//          println("${actor.netGUID} TeamNumber=$TeamNumber")
//        }
//        36 -> {
//          val bIsZombie = propertyBool()
//          println("${actor.netGUID} bIsZombie=$bIsZombie")
//        }
//        37 -> {
//          val ScoreByDamage = propertyFloat()
//          println("${actor.netGUID} ScoreByDamage=$ScoreByDamage")
//        }
//        38 -> {
//          val ScoreByKill = propertyFloat()
//          println("${actor.netGUID} ScoreByKill=$ScoreByKill")
//        }
//        39 -> {
//          val ScoreByRanking = propertyFloat()
//          println("${actor.netGUID} ScoreByRanking=$ScoreByRanking")
//        }
//        40 -> {
//          val ScoreFactor = propertyFloat()
//          println("${actor.netGUID} ScoreFactor=$ScoreFactor")
//        }
//        41 -> {
//          val NumKills = propertyInt()
//          println("${actor.netGUID} NumKills=$NumKills")
//        }
//        42 -> {
//          val TotalMovedDistanceMeter = propertyFloat()
//          println("${actor.netGUID} TotalMovedDistanceMeter=$TotalMovedDistanceMeter")
//        }
//        43 -> {
//          val TotalGivenDamages = propertyFloat()
//          println("${actor.netGUID} TotalGivenDamages=$TotalGivenDamages")
//        }
//        44 -> {
//          val LongestDistanceKill = propertyFloat()
//          println("${actor.netGUID} LongestDistanceKill=$LongestDistanceKill")
//        }
//        45 -> {
//          val HeadShots = propertyInt()
//          println("${actor.netGUID} HeadShots=$HeadShots")
//        }
        else -> return false
      }
    }
    return true
  }
}