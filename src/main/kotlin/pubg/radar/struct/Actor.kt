package pubg.radar.struct

import com.badlogic.gdx.math.Vector3
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.Archetype.Companion.fromArchetype
import java.util.concurrent.ConcurrentHashMap

enum class Archetype { //order matters, it affects the order of drawing
    Other,
    GameState,
    Plane,
    Parachute,
    Player,
    DroopedItemGroup,
    Grenade,
    TwoSeatBoat,
    FourSeatDU,
    FourSeatP,
    SixSeatBoat,
    TwoSeatBike,
    TwoSeatCar,
    ThreeSeatCar,
    SixSeatCar,
    AirDrop,
    PlayerState,
    Team,
    DeathDropItemPackage,
    DroppedItem,
    WeaponProcessor,
    Weapon;

    companion object {
        fun fromArchetype(archetype: String): Archetype = when {
            archetype.contains("Default__TSLGameState") -> GameState
            archetype.contains("Aircraft") -> Plane
            archetype.contains("Parachute") -> Parachute
            archetype.contains("Default__Player") -> Player
            archetype.contains("DroppedItemGroup") -> DroopedItemGroup
            archetype.contains("bike", true) -> TwoSeatBike
            archetype.contains("Sidecart", true) -> ThreeSeatCar
            archetype.contains("buggy", true) -> TwoSeatCar
            archetype.contains("dacia", true) -> FourSeatDU
            archetype.contains("uaz", true) -> FourSeatDU
            archetype.contains("pickup", true) -> FourSeatP
            archetype.contains("bus", true) -> SixSeatCar
            archetype.contains("van", true) -> SixSeatCar
            archetype.contains("AquaRail", true) -> TwoSeatBoat
            archetype.contains("boat", true) -> SixSeatBoat
            archetype.contains("Carapackage", true) -> AirDrop
            archetype.contains(Regex("(SmokeBomb|Molotov|Grenade|FlashBang|BigBomb)", RegexOption.IGNORE_CASE)) -> Grenade
            archetype.contains("Default__TslPlayerState") -> PlayerState
            archetype.contains("Default__Team", true) -> Team
            archetype.contains("DeathDropItemPackage", true) -> DeathDropItemPackage
            archetype.contains("DroppedItem") -> DroppedItem
            archetype.contains("Default__WeaponProcessor") -> WeaponProcessor
            archetype.contains("Weap") -> Weapon
            else -> Other
        }
    }
}

class Actor(val netGUID: NetworkGUID, private val archetypeGUID: NetworkGUID, val archetype: NetGuidCacheObject, private val ChIndex: Int) {
    private val archetype1: Archetype = fromArchetype(archetype.pathName)
    val Type: Archetype
        get() = archetype1
    var location = Vector3.Zero!!
    var rotation = Vector3.Zero!!
    var velocity = Vector3.Zero!!
    var owner: NetworkGUID? = null
    var attachParent: NetworkGUID? = null
    var attachChildren = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
    var isStatic = false

    override fun toString(): String {
        val ow: Any = this.owner ?: ""
        return "Actor(netGUID=$netGUID,location=$location,archetypeGUID=$archetypeGUID,\n" +
                "archetype=$archetype, ChIndex=$ChIndex, Type=$Type,  rotation=$rotation, velocity=$velocity,owner=$ow"
    }

    val isAPawn = when (Type) {
        Parachute,
        TwoSeatBoat,
        SixSeatBoat,
        TwoSeatBike,
        TwoSeatCar,
        ThreeSeatCar,
        FourSeatDU,
        FourSeatP,
        SixSeatCar,
        Plane,
        Player -> true
        else -> false
    }
    val isACharacter = Type == Player
    val isVehicle = Type.ordinal >= TwoSeatBoat.ordinal && Type.ordinal <= SixSeatCar.ordinal
}