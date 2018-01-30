package pubg.radar.deserializer

const val RELIABLE_BUFFER = 256
const val MAX_PACKETID = 16384
const val MAX_CHANNELS = 10240
const val MAX_CHSEQUENCE = 1024
const val MAX_BUNCH_HEADER_BITS = 64
const val NAME_SIZE = 1024
const val MAX_NETWORKED_HARDCODED_NAME = 410

const val ROLE_MAX=4

const val CHTYPE_NONE = 0
const val CHTYPE_CONTROL = 1
const val CHTYPE_ACTOR = 2
const val CHTYPE_FILE = 3
const val CHTYPE_VOICE = 4
const val CHTYPE_MAX = 8

const val MAX_PACKET_SIZE = 1228

//control
const val NMT_Hello = 0
const val NMT_Welcome = 1
const val NMT_Upgrade = 2
const val NMT_Challenge = 3
const val NMT_Netspeed = 4
const val NMT_Login = 5
const val NMT_Failure = 6
const val NMT_Join = 9
const val NMT_JoinSplit = 10
const val NMT_Skip = 12
const val NMT_Abort = 13
const val NMT_PCSwap = 15
const val NMT_ActorChannelFailure = 16
const val NMT_DebugText = 17
const val NMT_NetGUIDAssign = 18
const val NMT_EncryptionAck = 21
const val NMT_BeaconWelcome = 25
const val NMT_BeaconJoin = 26
const val NMT_BeaconAssignGUID = 27
const val NMT_BeaconNetGUIDAck = 28