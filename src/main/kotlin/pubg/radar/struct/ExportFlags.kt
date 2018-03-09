package pubg.radar.struct

class ExportFlags(val value: Int) {
    val bHasPath = 0 != (value and 1)
    val bHasNetworkChecksum = 0 != (value and 0b100)
    val bNoLoad = 0 != (value and 0b10)
}