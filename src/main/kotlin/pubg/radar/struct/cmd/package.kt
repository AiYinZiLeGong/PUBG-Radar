package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import pubg.radar.struct.Actor
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetGuidCacheObject
import wumo.pubg.struct.cmd.TeamCMD
import java.util.*

typealias cmdProcessor = (Actor, Bunch, NetGuidCacheObject?, Int, HashMap<String, Any?>) -> Boolean

enum class REPCMD {
    DynamicArray,  //0 Dynamic array
    Return,  //1 Return from array, or end of stream
    Property,  //2 Generic property

    PropertyBool,//3
    PropertyFloat,//4
    PropertyInt,//5
    PropertyByte,//6
    PropertyName,//7
    PropertyObject,//8
    PropertyUInt32,//9
    PropertyVector,//10
    PropertyRotator,//11
    PropertyPlane,//12
    PropertyVector100,//13
    PropertyNetId,//14
    RepMovement,//15
    PropertyVectorNormal,//16
    PropertyVector10,//17
    PropertyVectorQ,//18
    PropertyString,//19
    PropertyUInt64,//20
}

object CMD {
    fun Bunch.propertyBool() = readBit()
    fun Bunch.propertyFloat() = readFloat()
    fun Bunch.propertyInt() = readInt32()
    fun Bunch.propertyByte() = readByte()
    fun Bunch.propertyName() = readName()
    fun Bunch.propertyObject() = readObject()
    fun Bunch.propertyUInt32() = readUInt32()
    fun Bunch.propertyVector() = Vector3(readFloat(), readFloat(), readFloat())
    fun Bunch.propertyRotator() = Vector3(readFloat(), readFloat(), readFloat())
    fun Bunch.propertyPlane() = Quaternion(propertyVector(), readFloat())
    fun Bunch.propertyVector100() = readVector(100, 30)
    fun Bunch.propertyNetId() = if (readInt32() > 0) readString() else ""
    fun Bunch.repMovement(actor: Actor) {
        val bSimulatedPhysicSleep = readBit()
        val bRepPhysics = readBit()
        actor.location = if (actor.isAPawn)
            readVector(100, 30)
        else
            readVector(1, 24)

        actor.rotation = if (actor.isACharacter)
            readRotationShort()
        else
            readRotation()

        actor.velocity = readVector(1, 24)
        if (bRepPhysics)
            readVector(1, 24)
    }
    fun Bunch.propertyString() = readString()
    fun Bunch.propertyVectorNormal() = readFixedVector(1, 16)
    fun Bunch.propertyVector10() = readVector(10, 24)
    fun Bunch.propertyVectorQ() = readVector(1, 20)
    fun Bunch.propertyUInt64() = readInt64()

    val processors = mapOf<String, cmdProcessor>(
            GameState.name to GameStateCMD::process,
            Other.name to APawnCMD::process,
            DroopedItemGroup.name to APawnCMD::process,
            Grenade.name to APawnCMD::process,
            TwoSeatBoat.name to APawnCMD::process,
            SixSeatBoat.name to APawnCMD::process,
            TwoSeatBike.name to APawnCMD::process,
            TwoSeatCar.name to APawnCMD::process,
            ThreeSeatCar.name to APawnCMD::process,
            FourSeatP.name to APawnCMD::process,
            FourSeatDU.name to APawnCMD::process,
            SixSeatCar.name to APawnCMD::process,
            Plane.name to APawnCMD::process,
            Player.name to ActorCMD::process,
            Parachute.name to APawnCMD::process,
            AirDrop.name to APawnCMD::process,
            PlayerState.name to PlayerStateCMD::process,
            DroppedItem.name to DroppedItemCMD::process,
            Team.name to TeamCMD::process,
            "DroppedItemInteractionComponent" to DroppedItemInteractionComponentCMD::process,
            "DroppedItemGroupRootComponent" to DroppedItemGroupRootComponentCMD::process,
            WeaponProcessor.name to WeaponProcessorCMD::process

    )
}