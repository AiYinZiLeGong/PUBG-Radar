package pubg.radar

import org.pcap4j.core.BpfProgram.BpfCompileMode.OPTIMIZE
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.IpV4Packet
import org.pcap4j.packet.Packet
import org.pcap4j.packet.PppSelector
import kotlin.concurrent.thread
import kotlin.experimental.and

const val PPTPFlag: Byte = 0b0011_0000
const val ACKFlag: Byte = 0b1000_0000.toByte()

fun ByteArray.toIntBE(pos: Int, num: Int): Int {
    var value = 0
    for (i in 0 until num)
        value = value or ((this[pos + num - 1 - i].toInt() and 0xff) shl 8 * i)
    return value
}

fun parsePPTPGRE(raw: ByteArray) {
    var i = 0
    if (raw[i] == PPTPFlag) {//PPTP
        i++
        val hasAck = (raw[i] and ACKFlag) != 0.toByte()
        i++
        val protocolType = raw.toIntBE(i, 2)
        i += 2
        if (protocolType != 0x880b) return
        val payloadLength = raw.toIntBE(i, 2)
        i += 2
        val callID = raw.toIntBE(i, 2)
        i += 2
        val seq = raw.toIntBE(i, 4)
        i += 4
        if (hasAck) {
            val ack = raw.toIntBE(i, 4)
            i += 4
        }
        println("ack=$hasAck,protocolType=${protocolType.toString(16)}," +
                "payloadLength=$payloadLength,callID=$callID," +
                "seq=$seq")
        i--
        raw[i] = 0
        val pppPkt = PppSelector.newPacket(raw, i, raw.size - i)
        print(pppPkt)
    }
}

fun main(args: Array<String>) {
//  val raw = byteArrayOf(0x30, 0x81.toByte(), 0x88.toByte(), 0x0b, 0x00, 0x47.toByte(),
//                        0xe6.toByte(), 0x80.toByte(), 0x00, 0x00, 0x7d, 0x31, 0x00,
//                        0x00, 0x56, 0xe9.toByte(),
//                        0x21,
//                        0x45, 0x00, 0x00, 0x46, 0x76, 0x14, 0x00, 0x00, 0x80.toByte(),
//                        0x11, 0x90.toByte(), 0x09, 0xac.toByte(), 0x10, 0x02, 0x09, 0x34, 0x4e, 0x52, 0x22,
//                        0xd0.toByte(), 0x9a.toByte(), 0x1e, 0x1b, 0x00, 0x32, 0x1b, 0x04,
//                        0x24, 0x2f, 0xb5.toByte(), 0x38, 0x00, 0xb7.toByte(), 0x38, 0x00, 0xb9.toByte(),
//                        0x38, 0x00, 0xbb.toByte(), 0x38, 0x00,
//                        0xbd.toByte(), 0x38, 0x00, 0xbf.toByte(), 0x38, 0x00, 0xc1.toByte(), 0x38, 0x00, 0xc3.toByte(),
//                        0x38, 0x00, 0xc5.toByte(), 0x38,
//                        0x00, 0xc7.toByte(), 0x38, 0x00, 0xc9.toByte(), 0x38, 0x00, 0xcb.toByte(), 0x38, 0x00,
//                        0xcd.toByte(), 0x38, 0x00, 0x03)
//  for (byte in raw)
//    print((byte.toInt() and 0xff).toString(16) + " ")
//  println()
//  parsePPTPGRE(raw)

    val nif = Pcaps.findAllDevs()[0]
    val handle = nif.openLive(65535, NONPROMISCUOUS, 10)
    handle.setFilter("ip[9]=47", OPTIMIZE)

    thread(isDaemon = true) {
        handle.loop(-1) { packet: Packet? ->
            try {
                packet!!
                val ipPkt = packet[IpV4Packet::class.java]
                val ipHeader = ipPkt.header
                val raw = ipPkt.payload.rawData
                parsePPTPGRE(raw)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }.join()
}