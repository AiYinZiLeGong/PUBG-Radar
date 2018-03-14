package pubg.radar.struct

data class NetworkGUID(val value: Int) {
    fun isDynamic(): Boolean = value > 0 && (value and 1) == 0
    fun isValid(): Boolean = value > 0
    fun isStatic(): Boolean = value and 1 == 0
    fun isDefault(): Boolean = value == 1
}