package pubg.radar

import pubg.radar.sniffer.Sniffer.Companion.targetAddr
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address

fun main(args: Array<String>) {
    val proc = Runtime.getRuntime().exec("route print -4")
    val input = BufferedReader(InputStreamReader(proc.inputStream))
    val result = input.readText()

    val regex = Regex("\\s*On-link\\s*([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)\\s*")
    val ips = HashMap<String, Int>()
    for (matchResult in regex.findAll(result)) {
        val ip = matchResult.groups[1]!!.value
        ips.compute(ip) { _, count ->
            (count ?: 0) + 1
        }
    }
    ips.remove(targetAddr.hostAddress)
    ips.remove("127.0.0.1")
    val maxIp = ips.maxBy { it.value }
    if (maxIp != null) {
        val routeIpAddr = Inet4Address.getByName(maxIp.key) as Inet4Address
    } else {
        System.exit(0)
    }
}