package pubg.radar.struct

import pubg.radar.GameListener
import pubg.radar.bugln
import pubg.radar.debugln
import pubg.radar.deserializer.channel.ActorChannel.Companion.actors
import pubg.radar.register
import pubg.radar.struct.NetGUIDCache.Companion.guidCache

class NetGuidCacheObject(
        val pathName: String,
        val outerGUID: NetworkGUID) {
    var holdObj: Any? = null
    override fun toString(): String {
        return "{path='$pathName', outer[$outerGUID]}"
    }
}

class NetGUIDCache {
    companion object : GameListener {
        init {
            register(this)
        }

        val guidCache = NetGUIDCache()

        override fun onGameOver() {
            guidCache.isExportingNetGUIDBunch = false
            guidCache.objectLoop.clear()
        }
    }

    val objectLoop = HashMap<NetworkGUID, NetGuidCacheObject>()
    var isExportingNetGUIDBunch = false

    fun get(index: Int) = objectLoop[NetworkGUID(index)]

    fun getObjectFromNetGUID(netGUID: NetworkGUID): NetGuidCacheObject? {
        val cacheObject = objectLoop[netGUID] ?: return null
        if (cacheObject.pathName.isBlank()) {
//      check(netGUID.isDynamic())
            return null
        }
        return cacheObject
    }

    fun registerNetGUIDFromPath_Client(
            netGUID: NetworkGUID,
            pathName: String,
            outerGUID: NetworkGUID,
            networkChecksum: Int,
            bNoLoad: Boolean,
            bIgnoreWhenMissing: Boolean) {
        val existingCacheObjectPtr = objectLoop[netGUID]

        // If we find this guid, make sure nothing changes
        if (existingCacheObjectPtr != null) {
            bugln { "already register path!! original=$existingCacheObjectPtr --------------> new=$netGUID $pathName" }
            var bPathnameMismatch = false
            var bOuterMismatch = false
            var bNetGuidMismatch = false
            if (existingCacheObjectPtr.pathName != pathName)
                bPathnameMismatch = true
            if (existingCacheObjectPtr.outerGUID != outerGUID)
                bOuterMismatch = true

            if (bPathnameMismatch || bOuterMismatch)
                bugln { ",bPathnameMismatch:$bPathnameMismatch,bOuterMismatch:$bOuterMismatch" }
            return
        }

        // Register a new guid with this path
        val cacheObject = NetGuidCacheObject(
                pathName, outerGUID)
        objectLoop[netGUID] = cacheObject
        debugln { "register path [$netGUID] $cacheObject" }
    }

    fun registerNetGUID_Client(netGUID: NetworkGUID, obj: Any) {
        val existingCacheObjectPtr = objectLoop[netGUID]

        // If we find this guid, make sure nothing changes
        if (existingCacheObjectPtr != null) {
            bugln { "already register client!! original=${actors[existingCacheObjectPtr.outerGUID]} --------------> new=$netGUID obj $obj" }
            val oldObj = existingCacheObjectPtr.holdObj
            if (oldObj != null && oldObj != obj)
                bugln { "Reassigning NetGUID $netGUID" }
            objectLoop.remove(netGUID)
        }
        val cacheObject = when (obj) {
                 is NetGuidCacheObject -> NetGuidCacheObject(obj.pathName, netGUID)
                  is Actor -> NetGuidCacheObject(obj.archetype.pathName, netGUID)
                  else -> NetGuidCacheObject("", netGUID)
                }
        objectLoop[netGUID] = cacheObject
        debugln { "register obj:$obj" }
    }
}