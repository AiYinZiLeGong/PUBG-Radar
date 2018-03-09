package pubg.radar.struct.cmd

import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemToItem
import pubg.radar.struct.*

object DroppedItemCMD {
  
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      //      println("${actor.netGUID} $waitingHandle")
      when (waitingHandle) {
        16 -> {
          val (itemguid, item) = readObject()
          droppedItemToItem[actor.netGUID] = itemguid
//          println("$actor hasItem $itemguid,$item")
        }
        else -> return false
      }
      return true
    }
  }
}