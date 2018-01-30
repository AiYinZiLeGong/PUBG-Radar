package pubg.radar.struct

class Item {
  companion object {
    
    val category = mapOf(
        "Attach" to mapOf(
            "Weapon" to mapOf(
                "Lower" to mapOf(
                    "AngledForeGrip" to "grip",
                    "Foregrip" to "grip"),
                "Magazine" to mapOf(
                    "Extended" to mapOf(
                        "Large" to "Extended",
                        "SniperRifle" to "Extended"),
                    "ExtendedQuickDraw" to mapOf(
                        "Large" to "Extended",
                        "SniperRifle" to "Extended")),
                "Muzzle" to mapOf(
                    "Choke" to "Choke",
                    "Compensator" to mapOf(
                        "Large" to "Compensator",
                        "SniperRifle" to "Compensator"),
                    "FlashHider" to mapOf(
                        "Large" to "FlashHider",
                        "SniperRifle" to "FlashHider"),
                    "Suppressor" to mapOf(
                        "Large" to "Suppressor",
                        "SniperRifle" to "Suppressor")),
                "Stock" to mapOf(
                    "AR" to "AR_Composite",
                    "SniperRifle" to mapOf(
                        "BulletLoops" to "BulletLoops",
                        "CheekPad" to "CheekPad")),
                "Upper" to mapOf(
                    "ACOG" to "4x",
                    "CQBSS" to "8x"))),
        "Boost" to "drink",
        "Heal" to mapOf(
            "FirstAid" to "heal",
            "MedKit" to "heal"
        ),
        "Weapon" to mapOf(
            "HK416" to "m416",
            "Kar98K" to "98k",
            "SCAR-L" to "scar",
            "AK47" to "ak",
            "SKS" to "sks",
            "Grenade" to "grenade"),
        "Ammo" to mapOf(
            "556mm" to "556",
            "762mm" to "762"),
        "Armor" to mapOf(
            "C" to mapOf("01" to mapOf("Lv3" to "armor3")),
            "D" to mapOf("01" to mapOf("Lv2" to "armor2"))),
        "Back" to mapOf(
            "C" to mapOf(
                "01" to mapOf("Lv3" to "bag3"),
                "02" to mapOf("Lv3" to "bag3")),
            "F" to mapOf(
                "01" to mapOf("Lv2" to "bag2"),
                "02" to mapOf("Lv2" to "bag2"))),
        "Head" to mapOf(
            "F" to mapOf(
                "01" to mapOf("Lv2" to "helmet2"),
                "02" to mapOf("Lv2" to "helmet2")),
            "G" to mapOf("01" to mapOf("Lv3" to "helmet3"))))
    
    /**
     * @return null if not good, or short name for it
     */
    fun isGood(description: String): String? {
      try {
        val start = description.indexOf("Item_")
        if (start == -1) return null//not item
        val words = description.substring(start + 5).split("_")
        var c = category
        for (word in words) {
          if (word !in c)
            return null
          val sub = c[word]
          if (sub is String)
            return sub
          c = sub as Map<String, Any>
        }
      } catch (e: Exception) {
      }
      return null
    }
    
  }
}