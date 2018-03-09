package pubg.radar.deserializer.channel

import pubg.radar.deserializer.CHTYPE_CONTROL
import pubg.radar.deserializer.NMT_Welcome
import pubg.radar.gameOver
import pubg.radar.gameStart
import pubg.radar.isErangel
import pubg.radar.struct.Bunch

class ControlChannel(ChIndex: Int, client: Boolean = true) : Channel(ChIndex, CHTYPE_CONTROL, client) {
    override fun ReceivedBunch(bunch: Bunch) {
        val messageType = bunch.readUInt8()
        when (messageType) {
            NMT_Welcome -> {// server tells client they're ok'ed to load the server's level
                val map = bunch.readString()
                val gameMode = bunch.readString()
                val unknown = bunch.readString()
                isErangel = map.contains("erangel", true)
                gameStart()
                println("Welcome To ${if (isErangel) "Erangel" else "Miramar"}")
            }
            else -> {

            }
        }
    }

    override fun close() {
        println("Game over")
        gameOver()
    }
}