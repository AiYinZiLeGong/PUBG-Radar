# PUBG-Radar
PUBG-Radar by sniffering udp packet

Written in Kotlin

# Build
Using [maven](https://maven.apache.org/)

# Linux
If you experienced `X Error of failed request:  RenderBadPicture (invalid Picture parameter)` on Linux build, try switch to branch `linux`. Run with arguments `<ip_to_sniff> <PortFilter|PPTPFilter> [target_ip]`

# vmware or middle pc setup
Here is an easy way using `arpspoof` to redirect packets from your game pc to a middle pc.

**Note**: make sure your vmware instance or middle pc is in the same LAN as your game pc.(vmware instance should use bridged network)

Execute the following commands on the vmware or middle pc: 
1. run `sudo apt-get install dsniff` to install `arpspoof`.
2. edit `/etc/sysctl.conf`. add/uncomment `net.ipv4.ip_forward=1`. save and run `sudo sysctl -p` to enable ip_forward.
3. run `sudo arpspoof -i <eth_interface_name> -t <game_pc_ip> <router_ip>` to spoof game pc.
4. run `sudo arpspoof -i <eth_interface_name> -t <router_ip> <game_pc_ip> ` to spoof router.
5. run `sudo java -jar pubg-radar-with-dependencies.jar <middle_pc_ip> <PortFilter|PPTPFilter> <game_pc_ip>`
