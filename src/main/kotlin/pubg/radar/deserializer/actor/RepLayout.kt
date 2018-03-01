package pubg.radar.deserializer.actor

import pubg.radar.info
import pubg.radar.struct.Actor
import pubg.radar.struct.Bunch
import pubg.radar.struct.cmd.CMD.processors

fun repl_layout_bunch(bunch: Bunch, actor: Actor) {
    val cmdProcessor = processors[actor.Type] ?: return
    val bDoChecksum = bunch.readBit()
    do {
        val waitingHandle = bunch.readIntPacked()
        info { ",<$waitingHandle>" }
    } while (waitingHandle > 0 && cmdProcessor(actor, bunch, waitingHandle) && bunch.notEnd())
}