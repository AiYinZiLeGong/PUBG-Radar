# PUBG-Radar
PUBG-Radar by sniffering udp packet

Written in Kotlin

# Changes
1. Get self player's location by parsing `CharMoveComp` RPC. So the player direction is corrected now. 
2. Get item's relative locaiton by `DroppedItemInteractionComponent`. So the item location is corrected now.

# Build
Using [maven](https://maven.apache.org/)

# Note
You need to modify the code before playing game, or you will be banned by BE.
