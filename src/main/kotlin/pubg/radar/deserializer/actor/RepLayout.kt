package pubg.radar.deserializer.actor

import pubg.radar.info
import pubg.radar.struct.*
import pubg.radar.struct.cmd.CMD.processors

fun repl_layout_bunch(bunch: Bunch, repObj: NetGuidCacheObject?, actor: Actor) {
  val cmdProcessor = processors[repObj?.pathName ?: return] ?: return
  val bDoChecksum = bunch.readBit()
  val data = HashMap<String, Any?>()
  do {
    val waitingHandle = bunch.readIntPacked()
    info { ",<$waitingHandle>" }
  } while (waitingHandle > 0 && cmdProcessor(actor, bunch,repObj, waitingHandle, data) && bunch.notEnd())
}