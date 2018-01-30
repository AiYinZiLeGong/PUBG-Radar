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
  PlayerState;
  
  companion object {
    fun fromArchetype(archetype: String) = when {
      archetype.contains("Default__TSLGameState") -> GameState
      archetype.contains("Default__Player") -> Player
      archetype.contains("DroppedItemGroup") -> DroopedItemGroup
      archetype.contains("Aircraft") -> Plane
      archetype.contains("Parachute") -> Parachute
      archetype.contains("SideCar", true) -> ThreeSeatCar
      archetype.contains("bike", true) -> TwoSeatCar
      archetype.contains(Regex("(dacia|uaz|pickup|buggy)", RegexOption.IGNORE_CASE)) -> FourSeatCar
      archetype.contains("bus", true) -> SixSeatCar
      archetype.contains("van", true) -> SixSeatCar
      archetype.contains("AquaRail", true) -> TwoSeatBoat
      archetype.contains("boat", true) -> SixSeatBoat
      archetype.contains("Carapackage", true) -> AirDrop
      archetype.contains(Regex("(SmokeBomb|Molotov|Grenade|FlashBang|BigBomb)", RegexOption.IGNORE_CASE)) -> Grenade
      archetype.contains("Default__TslPlayerState") -> PlayerState
      else -> Other
    }
  }
}

class Actor(val netGUID: NetworkGUID, val archetypeGUID: NetworkGUID, val archetype: NetGuidCacheObject, val ChIndex: Int) {
  val Type: Archetype = fromArchetype(archetype.pathName)
  
  var location = Vector3.Zero
  var rotation = Vector3.Zero
  var velocity = Vector3.Zero
  
  var owner: NetworkGUID? = null
  var attachTo: NetworkGUID? = null
  var beAttached = false
  var isStatic = false
  
  override fun toString(): String {
    val ow = if (owner != null) owner else ""
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