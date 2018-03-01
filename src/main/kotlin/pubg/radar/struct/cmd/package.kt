package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Vector3
import pubg.radar.struct.Actor
import pubg.radar.struct.Archetype
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.Bunch
import wumo.pubg.struct.cmd.TeamCMD
import java.util.*

typealias cmdProcessor = (Actor, Bunch, Int) -> Boolean

object CMD {
    fun Bunch.propertyBool() = readBit()
    fun Bunch.propertyFloat() = readFloat()
    fun Bunch.propertyInt() = readInt32()
    fun Bunch.propertyByte() = readByte()
    fun Bunch.propertyName() = readName()
    fun Bunch.propertyObject() = readObject()
    fun Bunch.propertyVector() = Vector3(readFloat(), readFloat(), readFloat())
    fun Bunch.propertyRotator() = Vector3(readFloat(), readFloat(), readFloat())
    fun Bunch.propertyVector10() = readVector(10, 24)
    fun Bunch.propertyVector100() = readVector(100, 30)
    fun Bunch.propertyNetId() = if (readInt32() > 0) readString() else ""
    fun Bunch.repMovement(actor: Actor) {
        val bSimulatedPhysicSleep = readBit()
        val bRepPhysics = readBit()
        actor.location = if (actor.isAPawn)
            readVector(100, 30)
        else readVector(1, 24)

        actor.rotation = if (actor.isACharacter)
            readRotationShort()
        else readRotation()

        actor.velocity = readVector(1, 24)
        if (bRepPhysics)
            readVector(1, 24)
    }

    fun Bunch.propertyString() = readString()

    val processors: Map<Archetype, cmdProcessor>

    init {
        processors = EnumMap<Archetype, cmdProcessor>(
                mapOf(
                        GameState to GameStateCMD::process,
                        Other to APawnCMD::process,
                        DroopedItemGroup to APawnCMD::process,
                        Grenade to APawnCMD::process,
                        TwoSeatBoat to APawnCMD::process,
                        SixSeatBoat to APawnCMD::process,
                        TwoSeatCar to APawnCMD::process,
                        ThreeSeatCar to APawnCMD::process,
                        FourSeatCar to APawnCMD::process,
                        SixSeatCar to APawnCMD::process,
                        Plane to APawnCMD::process,
                        Player to ActorCMD::process,
                        Parachute to APawnCMD::process,
                        AirDrop to APawnCMD::process,
                        PlayerState to PlayerStateCMD::process,
                        Team to TeamCMD::process
                ))
    }
}
