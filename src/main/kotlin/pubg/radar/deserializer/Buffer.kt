package pubg.radar.deserializer

import com.badlogic.gdx.math.Vector3
import pubg.radar.struct.ExportFlags
import pubg.radar.struct.Names
import pubg.radar.struct.NetGUIDCache.Companion.guidCache
import pubg.radar.struct.NetGuidCacheObject
import pubg.radar.struct.NetworkGUID
import java.nio.charset.Charset
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.min

val GShift = ByteArray(8) { (1 shl it).toByte() }
const val zeroByte: Byte = 0
const val shortRotationScale = 360.0f / 65536.0f
const val byteRotationScale = 360.0f / 256.0f
typealias ObjectPtr = Pair<NetworkGUID, NetGuidCacheObject?>

open class Buffer(
        private val raw: ByteArray,
        private var posBits: Int = 0,
        private var localTotalBits: Int = raw.size * 8,
        private var totalBits: Int = localTotalBits) {

    constructor(buffer: Buffer) : this(buffer.raw, buffer.posBits, buffer.localTotalBits, buffer.totalBits)

    private fun shallowCopy(maxTotalBits: Int = localTotalBits): Buffer {
        return Buffer(raw, posBits, min(maxTotalBits, localTotalBits))
    }

    open fun deepCopy(copyBits: Int = this.totalBits): Buffer {
        check(copyBits <= this.totalBits) { "$copyBits>${this.totalBits}" }
        val cpy = cur.shallowCopy(copyBits)
        var iter = cur.nextBuffer
        while (iter != null) {
            cpy.append(iter.shallowCopy(copyBits - cpy.totalBits))
            iter = iter.nextBuffer
        }
        return cpy
    }

    private var nextBuffer: Buffer? = null
    private var last: Buffer = this
    private var cur: Buffer = this

    fun append(buffer: Buffer) {
        last.nextBuffer = buffer
        last = buffer.last
        totalBits += buffer.totalBits
    }

    fun notEnd() = bitsLeft() > 0
    fun atEnd() = bitsLeft() <= 0
    fun bitsLeft(): Int = totalBits
    fun numBytes(): Int = (bitsLeft() + 7) shl 3

    private fun readLocalBit(): Boolean {
        val b = raw[posBits ushr 3] and GShift[posBits and 0b0111]//x & 0b0111 == x % 8
        posBits++
        localTotalBits--
        return b != zeroByte
    }

    fun readBit(): Boolean {
        if (cur.localTotalBits > 0) {
            totalBits--
            return cur.readLocalBit()
        } else while (cur.nextBuffer != null) {
            cur = cur.nextBuffer!!
            if (cur.localTotalBits > 0) {
                totalBits--
                return cur.readLocalBit()
            }
        }
        throw IndexOutOfBoundsException()
    }

    fun readByte(): Int {
        var value: Byte = 0

        for (i in 0 until 8)
            if (readBit())
                value = value or GShift[i and 0b0111]

        return value.toInt() and 0xFF
    }

    fun readBytes(sizeBytes: Int = 1): ByteArray {
        return readBits(sizeBytes * 8)
    }

    fun readBits(sizeBits: Int = 8): ByteArray {
        val value = ByteArray((sizeBits + 7) ushr 3)//ceil(sizeBits / 8.0)
        for (i in 0 until sizeBits)
            if (readBit()) {
                val b = i ushr 3
                value[b] = value[b] or GShift[i and 0b0111]
            }
        return value
    }

    fun readInt(MaxValue: Int = MAX_PACKETID): Int {
        var mask = 1
        var value = 0
        while (value + mask < MaxValue && mask != 0) {
            if (readBit()) value = value or mask
            mask = mask shl 1
        }
        return value
    }

    fun readIntPacked(): Int {
        var value = 0
        var count = 0
        var more = 1
        while (more != 0) {
            var nextByte = readByte() // Read next byte
            more = nextByte and 1 // Check 1 bit to see if there're more after this
            nextByte = nextByte ushr 1 // Shift to get actual 7 bit value
            value += nextByte shl (7 * count++) // Add to total value
        }
        return value
    }

    fun readInt8(): Int {
        return readByte().toByte().toInt()
    }

    fun readUInt8(): Int {
        return readByte()
    }

    fun readInt16(): Int {
        return readUInt16().toShort().toInt()
    }

    fun readUInt16(): Int {
        var value = readByte()
        value = value or (readByte() shl 8)
        return value
    }

    fun readInt32(): Int {
        return readUInt32().toInt()
    }

    fun readInt64(): Long {
        var value: Long = readByte().toLong()
        value = value or (readByte().toLong() shl 8)
        value = value or (readByte().toLong() shl 16)
        value = value or (readByte().toLong() shl 24)
        value = value or (readByte().toLong() shl 32)
        value = value or (readByte().toLong() shl 40)
        value = value or (readByte().toLong() shl 48)
        value = value or (readByte().toLong() shl 56)
        return value
    }

    fun readInt24(): Int {
        var value = readByte()
        value = value or (readByte() shl 8)
        value = value or (readByte() shl 16)
        return value
    }

    fun readUInt32(): Long {
        var value: Long = readByte().toLong()
        value = value or (readByte().toLong() shl 8)
        value = value or (readByte().toLong() shl 16)
        value = value or (readByte().toLong() shl 24)
        return value
    }

    fun readFloat(): Float {
        var value: Int = readByte()
        value = value or (readByte() shl 8)
        value = value or (readByte() shl 16)
        value = value or (readByte() shl 24)
        return Float.fromBits(value)
    }

    fun readString(): String {
        var SaveNum = readInt32()
        val LoadUCS2Char = SaveNum < 0
        if (LoadUCS2Char)
            SaveNum = -SaveNum
        if (SaveNum > NAME_SIZE)
            throw ArrayIndexOutOfBoundsException()
        if (SaveNum == 0) return ""
        return if (LoadUCS2Char)
            readBytes(SaveNum * 2).toString(Charset.forName("UTF-16"))
        else {
            val bytes = readBytes(SaveNum)
            String(bytes, 0, bytes.size - 1, Charset.forName("UTF-8"))
        }
    }

    fun readName(): String? {
        val bHardcoded = readBit()
        return if (bHardcoded) {
            val nameIndex = readInt(MAX_NETWORKED_HARDCODED_NAME + 1)
            Names[nameIndex]
        } else {
            val inString = readString()
            val inNumber = readInt32()
            inString
        }
    }

    fun skipNetFieldExport() {
        val Flags = readUInt8()
        if (Flags == 1) {
            readIntPacked()
            readUInt32()
            readString()
            readString()
        }
    }

    fun readObject(): ObjectPtr {
        val netGUID = readNetworkGUID()
        var obj: NetGuidCacheObject? = null
        if (!netGUID.isValid()) return Pair(netGUID, obj)
        if (netGUID.isValid() && !netGUID.isDefault())
            obj = guidCache.getObjectFromNetGUID(netGUID)

        if (netGUID.isDefault() || guidCache.isExportingNetGUIDBunch) {//NetGUID.IsDefault() || GuidCache->radar.radar.deserializer.getIsExportingNetGUIDBunch
            val exportFlags = ExportFlags(readUInt8())
            if (exportFlags.bHasPath) {
                val (outerGUID, outerObj) = readObject()
                val pathName = readString()
                val networkChecksum = if (exportFlags.bHasNetworkChecksum) readUInt32().toInt() else 0
                val bIsPackage = netGUID.isStatic() && !outerGUID.isValid()
                if (obj != null) return Pair(netGUID, obj)
                if (netGUID.isDefault()) {//assign guid
                    return Pair(netGUID, obj)
                }

                val bIgnoreWhenMissing = exportFlags.bNoLoad
                // Register this path and outer guid combo with the net guid
                guidCache.registerNetGUIDFromPath_Client(netGUID, pathName, outerGUID, networkChecksum, exportFlags.bNoLoad, bIgnoreWhenMissing)

                // Try again now that we've registered the path
                obj = guidCache.getObjectFromNetGUID(netGUID)
            }
        }
        return Pair(netGUID, obj)
    }

    fun readVector(scaleFactor: Int = 10, MaxBitsPerComponent: Int = 24): Vector3 {
        val bits = readInt(MaxBitsPerComponent)
        val bias = 1 shl (bits + 1)
        val max = 1 shl (bits + 2)

        return Vector3(
                (readInt(max) - bias) / scaleFactor.toFloat(),
                (readInt(max) - bias) / scaleFactor.toFloat(),
                (readInt(max) - bias) / scaleFactor.toFloat())
    }

    fun readFixedVector(maxValue: Int, numBits: Int) =
            Vector3(readFixedCompressedFloat(maxValue, numBits),
                    readFixedCompressedFloat(maxValue, numBits),
                    readFixedCompressedFloat(maxValue, numBits))

    fun readFixedCompressedFloat(maxValue: Int, numBits: Int): Float {
        val maxBitValue = (1 shl (numBits - 1)) - 1//0111 1111 - Max abs value we will serialize
        val bias = 1 shl (numBits - 1)//1000 0000 - Bias to pivot around (in order to support signed values)
        val serIntMax = 1 shl (numBits - 0)// 1 0000 0000 - What we pass into SerializeInt
        val maxDelta = (1 shl (numBits - 0)) - 1//   1111 1111 - Max delta is
        val delta = readInt(serIntMax)
        val unscaledValue = (delta - bias).toFloat()
        return if (maxValue > maxBitValue) {
            val invScale = maxValue / maxBitValue.toFloat()
            unscaledValue * invScale
        } else {
            val scale = maxBitValue / maxValue
            val invScale = 1f / scale
            unscaledValue * invScale
        }
    }

    fun readRotationShort(): Vector3 {
        return Vector3(
                (if (readBit()) readUInt16() else 0) * shortRotationScale,//pitch
                (if (readBit()) readUInt16() else 0) * shortRotationScale,//yaw
                (if (readBit()) readUInt16() else 0) * shortRotationScale//roll
        )
    }

    fun readRotation(): Vector3 {
        return Vector3(
                (if (readBit()) readUInt8() else 0) * byteRotationScale,
                (if (readBit()) readUInt8() else 0) * byteRotationScale,
                (if (readBit()) readUInt8() else 0) * byteRotationScale
        )
    }

    fun readNetworkGUID(): NetworkGUID {
        return NetworkGUID(readIntPacked())
    }

    fun skipBits(bits: Int) {
        var bits = bits
        var decrease = min(cur.localTotalBits, bits)
        cur.localTotalBits -= decrease
        cur.posBits += decrease
        totalBits -= decrease
        bits -= decrease

        while (bits > 0 && cur.nextBuffer != null) {
            cur = cur.nextBuffer!!
            decrease = min(cur.localTotalBits, bits)
            cur.localTotalBits -= decrease
            cur.posBits += decrease
            totalBits -= decrease
            bits -= decrease
        }
        if (bits > 0)
            throw IndexOutOfBoundsException()
    }

}