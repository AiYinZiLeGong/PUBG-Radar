# You need to modify this or you will be banned by BattleEye
![Imgur](https://i.imgur.com/9yh84NO.png)
![Boat](https://github.com/NoParamedic/Memes/blob/master/src/main/resources/images/boat.png?raw=true)
![Buggy](https://github.com/NoParamedic/Memes/blob/master/src/main/resources/images/buggy.png?raw=true)
![Enemy](https://github.com/NoParamedic/Memes/blob/master/src/main/resources/images/arrow.png?raw=true)
![Player](https://github.com/NoParamedic/Memes/blob/master/src/main/resources/images/player.png?raw=true)
![Nade](https://github.com/NoParamedic/Memes/blob/master/src/main/resources/images/grenade.png?raw=true)
![Jetski](https://github.com/NoParamedic/Memes/blob/master/src/main/resources/images/jetski.png?raw=true)
![Parachute](https://github.com/NoParamedic/Memes/blob/master/src/main/resources/images/parachute.png?raw=true)

#### Item Filter:

* HOME -> Show / Hide Compass
* NUMPAD_0 -> Filter Throwables
* NUMPAD_1 -> Filter Attachments
* NUMPAD_2 -> Filter Scopes 
* NUMPAD_3 -> Filter Ammo 
* NUMPAD_4 -> Filter Weapons
* NUMPAD_5 -> Filter Level 2 Gear          
* NUMPAD_6 -> Filter Meds
           
#### Icon Toggles

* F5 -> Toggle Vehicle Names 
* F6 -> Toggle Vehicles
* F11 -> Toggle View Line

#### Toggle Player Information

* F7 -> Combat Mode (Hides Items, Corpses)
* F8 -> Player Information Panel 1-4

1. Distance, Name, Kills, Health, Weapons
2. Distance, Name, Kills, Health
3. Distance, Health, Weapons
4. Distance, Health

#### Zooms:
* NUMPAD_7 -> Scouting
* NUMPAD_8 -> Scout/Loot
* NUMPAD_9 -> Looting
* F9 ->  Camera Zoom ++
* F10 -> Camera Zoom --


# Changes
* Get self player's location by parsing `CharMoveComp` RPC. So the player's direction is corrected now. 
* Get item's relative locaiton by `DroppedItemInteractionComponent`. So the item location is corrected now.
* Change `readRotator()` to `readRotationShort()` fixes empty player state.
* `DroppedItem` is shown.
* Fully parse `ATslCharacter`. So player health is shown.
* Correct player color when driving.
* `Equipped weapons` are shown.
* Clear `DroppedItem` and `DroppedItemGroup` when `picking up`.
