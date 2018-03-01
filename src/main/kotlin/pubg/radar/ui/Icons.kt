package pubg.radar.ui

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

class Icons(sheet: Texture, size: Int) {
    var iconSheet: Texture = sheet
    var icon: TextureRegion
    private var iconSize: Int = size
    private val coordinates: Map<String, Array<Int>> = mapOf(
            // These Match up to the sprite sheet
            // 0 Items
            "Gas" to arrayOf(1, 0),
            "Bandage" to arrayOf(2, 0),
            "FirstAid" to arrayOf(3, 0),
            "MedKit" to arrayOf(4, 0),
            "Drink" to arrayOf(5, 0),
            "Pain" to arrayOf(6, 0),
            "Syringe" to arrayOf(7, 0),
            // 1 Gear
            "Bag2" to arrayOf(0, 1),
            "Bag3" to arrayOf(1, 1),
            "Arm2" to arrayOf(2, 1),
            "Arm3" to arrayOf(3, 1),
            "Helm2" to arrayOf(4, 1),
            "Helm3" to arrayOf(5, 1),
            // 2 Melee
            "Crowbar" to arrayOf(0, 2),
            "Sickle" to arrayOf(1, 2),
            "Machete" to arrayOf(2, 2),
            "Pan" to arrayOf(3, 2),
            // 5 Ammo
            "9" to arrayOf(1, 3),
            "45" to arrayOf(2, 3),
            "556" to arrayOf(3, 3),
            "762" to arrayOf(4, 3),
            "300" to arrayOf(5, 3),
            // 4 Throwables
            "Smoke" to arrayOf(0, 4),
            "Flash" to arrayOf(1, 4),
            "Molotov" to arrayOf(2, 4),
            "Grenade" to arrayOf(3, 4),
            // 5 Shotguns
            "S686" to arrayOf(0, 5),
            "SK12" to arrayOf(1, 5),
            "S1897" to arrayOf(2, 5),
            "Sawed-off" to arrayOf(3, 5),
            // 6 Pistols
            "R45" to arrayOf(4, 6),
            // 7 MGs
            "Uzi" to arrayOf(0, 7),
            "Ump" to arrayOf(1, 7),
            "Vector" to arrayOf(2, 7),
            "Tommy" to arrayOf(3, 7),
            "Dp" to arrayOf(4, 7),
            "M249" to arrayOf(5, 7),
            // 8 ARs
            "M16" to arrayOf(0, 8),
            "Ak" to arrayOf(1, 8),
            "Scar" to arrayOf(2, 8),
            "M4" to arrayOf(3, 8),
            "Groza" to arrayOf(4, 8),
            "Aug" to arrayOf(5, 8),
            // 9 Rifles
            "Win94" to arrayOf(0, 9),
            "VSS" to arrayOf(1, 9),
            "Mk14" to arrayOf(2, 9),
            "Sks" to arrayOf(3, 9),
            "Mini" to arrayOf(4, 9),
            "98k" to arrayOf(5, 9),
            "M24" to arrayOf(6, 9),
            "Awm" to arrayOf(7, 9),
            // Grips
            "V.Grip" to arrayOf(0, 10),
            "U.Ext" to arrayOf(1, 10),
            "AR.Ext" to arrayOf(2, 10),
            "S.Ext" to arrayOf(3, 10),
            // Compensator
            "U.Comp" to arrayOf(4, 10),
            "AR.Comp" to arrayOf(5, 10),
            "S.Comp" to arrayOf(6, 10),
            "AR.Stock" to arrayOf(7, 10),
            "CheekPad" to arrayOf(8, 10),
            // Extended Mags
            "A.Grip" to arrayOf(0, 11),
            "U.ExtQ" to arrayOf(1, 11),
            "AR.ExtQ" to arrayOf(2, 11),
            "S.ExtQ" to arrayOf(3, 11),
            // Suppressors
            "U.Supp" to arrayOf(4, 11),
            "AR.Supp" to arrayOf(5, 11),
            "S.Supp" to arrayOf(6, 11),
            "H.Supp" to arrayOf(7, 11),
            // Scopes
            "R.Dot" to arrayOf(0, 12),
            "Holo" to arrayOf(1, 12),
            "2x" to arrayOf(2, 12),
            "4x" to arrayOf(3, 12),
            "8x" to arrayOf(4, 12),
            "15x" to arrayOf(5, 12)
    )

    fun setIcon(key: String) {
        val iconCoordinates = coordinates[key]
        if (iconCoordinates !== null) {
            icon.setRegion(
                    iconCoordinates[0] * iconSize,
                    iconCoordinates[1] * iconSize,
                    iconSize,
                    iconSize
            )
        } else {
            icon.setRegion(0, 0, 64, 64)
        }
    }

    init {
        icon = TextureRegion(iconSheet, 0, 0, 64, 64)
    }
}
