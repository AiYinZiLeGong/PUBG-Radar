# PUBG-Radar
PUBG-Radar by sniffering udp packet

Written in Kotlin

# Changes
* Get self player's location by parsing `CharMoveComp` RPC. So the player's direction is corrected now. 
* Get item's relative locaiton by `DroppedItemInteractionComponent`. So the item location is corrected now.
* Change `readRotator()` to `readRotationShort()` fixes empty player state.
* `DroppedItem` is shown.
* Fully parse `ATslCharacter`. So player health is shown.
* Correct player color when driving.
* `Equipped weapons` are shown.
* Clear `DroppedItem` and `DroppedItemGroup` when `picking up`.

# Build
Using [maven](https://maven.apache.org/)

# Note
You need to modify the code before playing game, or you will be banned by BE.
