package pubg.radar.struct

import com.badlogic.gdx.math.Vector3
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.Archetype.Companion.fromArchetype

enum class Archetype { //order matters, it affects the order of drawing
    Other,
    GameState,
    DroopedItemGroup,
    Grenade,
    TwoSeatBoat,
    SixSeatBoat,
    TwoSeatCar,
    ThreeSeatCar,
    FourSeatCar,
    SixSeatCar,
    Plane,
    Player,
    Parachute,
    AirDrop,
    PlayerState,
    Team,
    DeathDropItemPackage;

    companion object {
        fun fromArchetype(archetype: String): Archetype {
            return when {
                archetype.contains("Default__TSLGameState") -> GameState
                archetype.contains("Default__Player") -> Player
                archetype.contains("DroppedItemGroup") -> DroopedItemGroup
                archetype.contains("Aircraft") -> Plane
                archetype.contains("Parachute") -> Parachute
                archetype.contains(Regex("(bike|buggy|Sidecart)", RegexOption.IGNORE_CASE)) -> TwoSeatCar
                archetype.contains(Regex("(dacia|uaz|pickup)", RegexOption.IGNORE_CASE)) -> FourSeatCar
                archetype.contains("bus", true) -> SixSeatCar
                archetype.contains("van", true) -> SixSeatCar
                archetype.contains("AquaRail", true) -> TwoSeatBoat
                archetype.contains("boat", true) -> SixSeatBoat
                archetype.contains("Carapackage", true) -> AirDrop
                archetype.contains(Regex("(SmokeBomb|Molotov|Grenade|FlashBang|BigBomb)", RegexOption.IGNORE_CASE)) -> Grenade
                archetype.contains("Default__TslPlayerState") -> PlayerState
                archetype.contains("Default__Team", true) -> Team
                archetype.contains("DeathDropItemPackage", true) -> DeathDropItemPackage
                else -> Other
            }
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
    var attachTo: NetworkGUID? = null
    var beAttached = false
    var isStatic = false

    override fun toString(): String {
        val ow: Any = this.owner ?: ""
        return "Actor(netGUID=$netGUID,location=$location,archetypeGUID=$archetypeGUID, archetype=$archetype, ChIndex=$ChIndex, Type=$Type,  rotation=$rotation, velocity=$velocity,owner=$ow"
    }

    val isAPawn = when (Type) {
        TwoSeatBoat,
        SixSeatBoat,
        TwoSeatCar,
        ThreeSeatCar,
        FourSeatCar,
        SixSeatCar,
        Plane,
        Player,
        Parachute -> true
        else -> false
    }
    val isACharacter = Type == Player
    val isVehicle = Type.ordinal >= TwoSeatBoat.ordinal && Type.ordinal <= SixSeatCar.ordinal
}