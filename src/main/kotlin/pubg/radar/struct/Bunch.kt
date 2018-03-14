package pubg.radar.struct

import pubg.radar.deserializer.Buffer

class Bunch(
        private val BunchDataBits: Int,
        buffer: Buffer,
        private val PacketID: Int,
        private val ChIndex: Int,
        private val ChType: Int,
        var ChSequence: Int,
        val bOpen: Boolean,
        var bClose: Boolean,
        var bDormant: Boolean,
        var bIsReplicationPaused: Boolean,
        val bReliable: Boolean,
        val bPartial: Boolean,
        val bPartialInitial: Boolean,
        var bPartialFinal: Boolean,
        val bHasPackageMapExports: Boolean,
        var bHasMustBeMappedGUIDs: Boolean
) : Buffer(buffer) {

    override fun deepCopy(copyBits: Int): Bunch {
        val buf = super.deepCopy(copyBits)
        return Bunch(
                BunchDataBits,
                buf,
                PacketID,
                ChIndex,
                ChType,
                ChSequence,
                bOpen,
                bClose,
                bDormant,
                bIsReplicationPaused,
                bReliable,
                bPartial,
                bPartialInitial,
                bPartialFinal,
                bHasPackageMapExports,
                bHasMustBeMappedGUIDs
        )
    }

    var next: Bunch? = null
}