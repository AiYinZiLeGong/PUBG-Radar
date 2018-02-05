package pubg.radar.struct.cmd

import pubg.radar.*
import pubg.radar.deserializer.ROLE_MAX
import pubg.radar.deserializer.channel.ActorChannel.Companion.actors
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.visualActors
import pubg.radar.struct.*
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.NetGUIDCache.Companion.guidCache
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyName
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyRotator
import pubg.radar.struct.cmd.CMD.propertyVector100
import pubg.radar.struct.cmd.CMD.repMovement
import java.util.concurrent.ConcurrentHashMap

object ActorCMD: GameListener {
  init {
    register(this)
  }
  
  override fun onGameOver() {
    actorWithPlayerState.clear()
    playerStateToActor.clear()
  }
  
  val actorWithPlayerState = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
  val playerStateToActor = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
  
  fun process(actor: Actor, bunch: Bunch, waitingHandle: Int): Boolean {
    with(bunch) {
      when (waitingHandle) {
        1 -> if (readBit()) {//bHidden
          visualActors.remove(actor.netGUID)
          bugln { ",bHidden id$actor" }
        }
        2 -> if (!readBit()) {// bReplicateMovement
          if (!actor.isVehicle) {
            visualActors.remove(actor.netGUID)
          }
          bugln { ",!bReplicateMovement id$actor " }
        }
        3 -> if (readBit()) {//bTearOff
          visualActors.remove(actor.netGUID)
          bugln { ",bTearOff id$actor" }
        }
        4 -> {
          val role = readInt(ROLE_MAX)
          val b = role
        }
        5 -> {
          val (netGUID, obj) = readObject()
          actor.owner = if (netGUID.isValid()) netGUID else null
          bugln { " owner: [$netGUID] $obj ---------> beOwned:$actor" }
        }
        6 -> {
          repMovement(actor)
          with(actor) {
            when (Type) {
              AirDrop -> airDropLocation[netGUID] = location
              Other -> {
              }
              else -> visualActors[netGUID] = this
            }
          }
        }
        7 -> {
          val (a, obj) = readObject()
          val attachTo = if (a.isValid()) {
            actors[a]?.beAttached = true
            a
          } else null
          if (actor.attachTo != null)
            actors[actor.attachTo!!]?.beAttached = false
          actor.attachTo = attachTo
          bugln { ",attachTo [$actor---------> $a ${guidCache.getObjectFromNetGUID(a)} ${actors[a]}" }
        }
        8 -> {
          val locationOffset = propertyVector100()
          if (actor.Type == DroopedItemGroup) {
            bugln { "${actor.location} locationOffset $locationOffset" }
          }
          bugln { ",attachLocation $actor ----------> $locationOffset" }
        }
        9 -> propertyVector100()
        10 -> propertyRotator()
        11 -> {
          val attachSocket = propertyName()
        }
        12 -> {
          val (attachComponnent, attachName) = bunch.readObject()
        }
        13 -> {
          readInt(ROLE_MAX)
        }
        14 -> propertyBool()
        15 -> propertyObject()
        16 -> {
          val (playerStateGUID, playerState) = propertyObject()
          if (playerStateGUID.isValid()) {
            actorWithPlayerState[actor.netGUID] = playerStateGUID
            playerStateToActor[playerStateGUID] = actor.netGUID
          }
        }
//        17 -> {//RemoteViewPitch 2
//          readUInt16()
//        }
//        18 -> propertyObject()
//        19 -> propertyObject()
//        20 -> propertyName()
//        21 -> propertyVector100()
//        22 -> propertyRotator()
//        23 -> propertyBool()
//        24 -> propertyBool()
//        25 -> propertyBool()
//        26 -> propertyFloat()
//        27 -> propertyFloat()
//        28 -> propertyByte()
//        29 -> propertyBool()
//        30 -> propertyFloat()
//        31 -> propertyInt()
//        32 -> propertyBool()
//        33 -> propertyObject()
//        34 -> propertyFloat()
//        35 -> propertyVector100()
//        36 -> propertyRotator()
//        37 -> propertyObject()
//        38 -> propertyName()
//        39 -> propertyBool()
//        40 -> propertyBool()
//        41 -> {//player
//          println("41")
//          return false
////          val sourcesNum = readUInt8()
////          val bHasAdditiveSources = readBit()
////          val bHasOverrideSources = readBit()
////          val lastPreAdditiveVelocity = propertyVector10()
////          val bIsAdditiveVelocityApplied = readBit()
////          val flags = readUInt8()
////          for (i in 0 until sourcesNum) {
////
////          }
//        }
//        42 -> propertyVector10()
//        43 -> propertyVector10()
//        44 -> {
//          println("44")
//          return false
////          val arrayNum = readUInt16()
////          for (i in 0 until arrayNum) {
////            val handle = readIntPacked()
////            ActorCMD.process(actor, bunch, handle)
////          }
//        }
//        45 -> {
//          println("45")
//          return false
//        }
//        46 -> {
//          propertyInt()
//        }
//        47 -> {
//          propertyFloat()
//        }
//        48 -> {
//          propertyObject()
//        }
//        49 -> {
//          propertyObject()
//        }
//        50 -> {
//          propertyByte()
//        }
//        51 -> {
//          propertyBool()
//        }
//        52 -> {
//          val bIsAimingRemote = propertyBool()
//        }
//        53 -> {
//          propertyBool()
//        }
//        54 -> {
//          propertyBool()
//        }
//        55 -> {
//          propertyObject()
//        }
//        56 -> {
//          val ActualDamage = propertyFloat()
//          println("ActualDamage=$ActualDamage")
//        }
//        57 -> {
//          val damageType = propertyObject()
//          println("damageType=$damageType")
//        }
//        58 -> {
//          val PlayerInstigator = propertyObject()
//          if (PlayerInstigator.first in actors)
//            println("PlayerInstigator=${actors[PlayerInstigator.first]}")
//        }
//        59 -> {
//          val DamageOrigin = propertyVectorQ()
//          println("DamageOrigin=$DamageOrigin")
//        }
//        60 -> {
//          val RelHitLocation = propertyVectorQ()
//          println("RelHitLocation=$RelHitLocation")
//        }
//        61 -> {
//          propertyName()
//        }
//        62 -> {
//          val DamageMaxRadius = propertyFloat()
//        }
//        63 -> {
//          val ShotDirPitch = propertyByte()
//        }
//        64 -> {
//          val ShotDirYaw = propertyByte()
//          println("shot yaw=$ShotDirYaw")
//        }
//        65 -> {
//          val result = propertyBool()
//          val b = result
//        }
//        66 -> {
//          val result = propertyBool()
//          val b = result
//        }
//        67 -> {
//          val bKilled = propertyBool()
//          println("bKilled=$bKilled")
//        }
//        68 -> {
//          val EnsureReplicationByte = propertyByte()
//        }
////        69 -> {
////          val AttackerWeaponName = propertyName()
////          println("AttackerWeaponName=$AttackerWeaponName")
////        }
//        70 -> {
//          val AttackerLocation=propertyVector()
//          println("AttackerLocation=$AttackerLocation")
////          val ReviveCastingTime=propertyFloat()
////          println("ReviveCastingTime=$ReviveCastingTime")
//        }
//        69 -> {
//          val TargetingType = readInt(4)
//          println(TargetingType)
//        }
//
//        90 -> {
//          propertyBool()
//        }
//        91 -> {
//          propertyBool()
//        }
//        92 -> {
//          val health = propertyFloat()
//          println("health=$health")
//        }
//        93 -> {
//          val healthMax = propertyFloat()
//          println("health max=$healthMax")
//        }
//        94 -> {
//          val GroggyHealth = propertyFloat()
//          println("GroggyHealth=$GroggyHealth")
//        }
//        95 -> {
//          val GroggyHealthMax = propertyFloat()
//          println("GroggyHealthMax=$GroggyHealthMax")
//        }
//        80 -> propertyBool()
        else -> return false
      }
      return true
    }
  }
}