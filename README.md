# PUBG-Radar
PUBG-Radar by sniffering udp packet

Written in Kotlin

# Changes
* Get self player's location by parsing `CharMoveComp` RPC. So the player's direction is corrected now. 
* Get items' relative locaiton by `DroppedItemInteractionComponent`. So the item location is corrected now.
* change `readRotator()` to `readRotationShort()` fixes empty player state.
# Build
Using [maven](https://maven.apache.org/)

# Note
You need to modify the code before playing game, or you will be banned by BE.
