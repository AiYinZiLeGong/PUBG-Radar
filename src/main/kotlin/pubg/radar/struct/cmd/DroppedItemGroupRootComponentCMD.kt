package pubg.radar.struct.cmd

import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemCompToItem
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemGroup
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubg.radar.struct.*

object DroppedItemGroupRootComponentCMD {
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      when (waitingHandle) {
        4 -> {
          val arraySize = readUInt16()
          val comps = droppedItemGroup[actor.netGUID] ?: ArrayList(arraySize)
          val new = comps.isEmpty()
          var index = readIntPacked()
          val toRemove = HashSet<NetworkGUID>()
          val toAdd = HashSet<NetworkGUID>()
          while (index != 0) {
            val i = index - 1
            val (netguid, obj) = readObject()
            if (new)
              comps.add(netguid)
            else {
              //remove index
              toRemove.add(comps[i])
              comps[i] = netguid
              toAdd.add(netguid)
            }
//            println("$netguid,$obj")
            index = readIntPacked()
          }
          for (i in comps.lastIndex downTo arraySize)
            toRemove.add(comps.removeAt(i))
          toRemove.removeAll(toAdd)
          droppedItemGroup[actor.netGUID] = comps
          for (removedComp in toRemove)
            droppedItemLocation.remove(droppedItemCompToItem[removedComp] ?: continue)
        }
        else -> return false
      }
    }
    return true
  }
}