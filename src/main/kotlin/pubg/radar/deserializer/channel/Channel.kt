package pubg.radar.deserializer.channel

import pubg.radar.*
import pubg.radar.deserializer.RELIABLE_BUFFER
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetGUIDCache.Companion.guidCache

abstract class Channel(val chIndex: Int, val chType: Int, val client: Boolean = true) {
    companion object : GameListener {
        init {
            register(this)
        }

        override fun onGameOver() {
            inChannels.clear()
            outChannels.clear()
            closedInChannels.clear()
            closedOutChannels.clear()
            globalInReliable = -1
            globalOutReliable = -1
        }

        val inChannels = hashMapOf<Int, Channel>()
        val outChannels = HashMap<Int, Channel>()
        val closedInChannels = hashSetOf<Int>()
        val closedOutChannels = hashSetOf<Int>()
        var globalOutReliable = -1
        var globalInReliable = -1
            set(value) {
                if (!gameStarted)
                    field = value
            }
    }

    var bDormant = false
    var Closing = false
    var InReliable: Int = if (client) globalInReliable else globalOutReliable
    var inQueueFirst: Bunch? = null//Incoming data with queued dependencies.
    var inQueueLast: Bunch? = null
    var numInRec = 0
    var inPartialBunch: Bunch? = null// Partial bunch we are receiving (incoming partial bunches are appended to this)

    fun ReceiveNetGUIDBunch(bunch: Bunch) {
        val bHasRepLayoutExport = bunch.readBit()
        if (bHasRepLayoutExport) {
            bug { "bHasRepLayoutExport:$bHasRepLayoutExport" }
        }
        guidCache.isExportingNetGUIDBunch = true
        val NumGUIDsInBunch = bunch.readInt32()
        val MAX_GUID_COUNT = 2048

        if (NumGUIDsInBunch > MAX_GUID_COUNT) {
            guidCache.isExportingNetGUIDBunch = false
            throw Exception("NumGUIDsInBunch:$NumGUIDsInBunch > MAX_GUID_COUNT:$MAX_GUID_COUNT")
        }
        for (i in 0 until NumGUIDsInBunch)
            bunch.readObject()

        guidCache.isExportingNetGUIDBunch = false

    }

    fun ReceivedRawBunch(bunch: Bunch) {
        if (bunch.bHasPackageMapExports)
            ReceiveNetGUIDBunch(bunch)
        if (bunch.bReliable && bunch.ChSequence <= InReliable) return
        if (client) {
            if (globalInReliable < 0) {
                globalInReliable = bunch.ChSequence - 1
                InReliable = globalInReliable
            }
        } else {
            if (globalOutReliable < 0) {
                globalOutReliable = bunch.ChSequence - 1
                InReliable = globalOutReliable
            }
        }
        if (bunch.bReliable && bunch.ChSequence != InReliable + 1) {
            when {
                inQueueFirst == null -> {
                    inQueueFirst = bunch
                    inQueueLast = bunch
                }
                bunch.ChSequence < inQueueFirst!!.ChSequence -> {
                    bunch.next = inQueueFirst
                    inQueueFirst = bunch
                }
                bunch.ChSequence > inQueueLast!!.ChSequence -> {
                    inQueueLast!!.next = bunch
                    inQueueLast = bunch
                }
                else -> {
                    var pre = inQueueFirst
                    while (bunch.ChSequence > pre?.next!!.ChSequence)
                        pre = pre.next!!
                    bunch.next = pre.next
                    pre.next = bunch
                }
            }

            numInRec++
            if (numInRec >= RELIABLE_BUFFER)
                throw Exception("UChannel::ReceivedRawBunch: Too many reliable messages queued up")
        } else {
            var bDeleted = ReceivedNextBunch(bunch)
            if (bDeleted) return
            // Dispatch any waiting bunches.
            while (inQueueFirst != null) {
                if (inQueueFirst!!.ChSequence != InReliable + 1) break
                val release = inQueueFirst!!
                inQueueFirst = inQueueFirst!!.next
                numInRec--
                bDeleted = ReceivedNextBunch(release)
                if (bDeleted) return
            }
        }
    }

    fun ReceivedNextBunch(bunch: Bunch): Boolean {
        /*
        * We received the next bunch. Basically at this point:
        * -We know this is in order if reliable
        * -We don't know if this is partial or not
        * If it's not a partial bunch, or it completes a partial bunch, we can call ReceivedSequencedBunch to actually handle it
        */

        if (bunch.bReliable)
            InReliable = bunch.ChSequence// Reliables should be ordered properly at this point

        var HandleBunch: Bunch? = bunch
        if (bunch.bPartial) {
            HandleBunch = null
            if (bunch.bPartialInitial) {
                // Create new InPartialBunch if this is the initial bunch of a new sequence.
                val inPartialBunch = inPartialBunch
                if (inPartialBunch != null) {
                    if (!inPartialBunch.bPartialFinal) {
                        if (inPartialBunch.bReliable)//Unreliable partial trying to destroy reliable partial 1
                            return false
                        //We didn't complete the last partial bunch - this isn't fatal since they can be unreliable
                    }
                    this.inPartialBunch = null
                }

                this.inPartialBunch = bunch
                if (!bunch.bHasPackageMapExports && bunch.bitsLeft() > 0) {
                    check(bunch.bitsLeft() % 8 == 0)
//        print("Received New partial bunch. Channel: ${InPartialBunch!!.ChIndex} ChSequence:${InPartialBunch!!.ChSequence} . NumBits Total: ${InPartialBunch!!.bitsLeft()}")
                } else {
//          print("Received New partial bunch. It only contained NetGUIDs")
                }
            } else {
                /* Merge in next partial bunch to InPartialBunch if:
                *	-We have a valid InPartialBunch
                *	-The current InPartialBunch wasn't already complete
                * -ChSequence is next in partial sequence
                *	-Reliability flag matches
                */
                val inPartialBunch = inPartialBunch
                if (inPartialBunch != null) {
                    val bReliableSequencesMatches = bunch.ChSequence == inPartialBunch.ChSequence + 1
                    val bUnreliableSequenceMatches = bReliableSequencesMatches || bunch.ChSequence == inPartialBunch.ChSequence;
                    val bSequenceMatches = if (inPartialBunch.bReliable) bReliableSequencesMatches else bUnreliableSequenceMatches

                    if (!inPartialBunch.bPartialFinal && bSequenceMatches && inPartialBunch.bReliable == bunch.bReliable) {
                        // Merge.
                        if (!bunch.bHasPackageMapExports && bunch.notEnd()) {
                            inPartialBunch.append(bunch)
                        }

                        // Only the final partial bunch should ever be non byte aligned. This is enforced during partial bunch creation
                        // This is to ensure fast copies/appending of partial bunches. The final partial bunch may be non byte aligned.
                        check(bunch.bHasPackageMapExports || bunch.bPartialFinal || bunch.bitsLeft() % 8 == 0)

                        // Advance the sequence of the current partial bunch so we know what to expect next
                        inPartialBunch.ChSequence = bunch.ChSequence

                        if (bunch.bPartialFinal) {
                            check(!bunch.bHasPackageMapExports) // Shouldn't have these, they only go in initial partial export bunches
                            HandleBunch = inPartialBunch

                            inPartialBunch.bPartialFinal = true
                            inPartialBunch.bClose = bunch.bClose
                            inPartialBunch.bDormant = bunch.bDormant
                            inPartialBunch.bIsReplicationPaused = bunch.bIsReplicationPaused
                            inPartialBunch.bHasMustBeMappedGUIDs = bunch.bHasMustBeMappedGUIDs
                        }
                    } else {
                        // Merge problem - delete InPartialBunch. This is mainly so that in the unlikely chance that ChSequence wraps around, we wont merge two completely separate partial bunches.
                        if (inPartialBunch.bReliable) {
                            info { "Unreliable partial trying to destroy reliable partial 2" }
                            return false
                        }

                        this.inPartialBunch = null
                    }
                }
            }

            val MAX_CONSTRUCTED_PARTIAL_SIZE_IN_BYTES = 1024 * 64
            if (inPartialBunch != null && inPartialBunch!!.numBytes() > MAX_CONSTRUCTED_PARTIAL_SIZE_IN_BYTES) {
                info { "Final partial bunch too large" }
                return false
            }
        }

        if (HandleBunch != null) {
            // Remember the range.
            // In the case of a non partial, HandleBunch == radar.radar.struct.Bunch
            // In the case of a partial, HandleBunch should == InPartialBunch, and radar.radar.struct.Bunch should be the last bunch.

            // Receive it in sequence.
            return ReceivedSequencedBunch(HandleBunch)
        }
        return false
    }

    fun ReceivedSequencedBunch(bunch: Bunch): Boolean {
        if (!Closing || bunch.bOpen) {
            Closing = false
            try {
                ReceivedBunch(bunch)
            } catch (e: IndexOutOfBoundsException) {

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // We have fully received the bunch, so process it.
        if (bunch.bClose) {
            bDormant = bunch.bDormant
            Closing = true
            closedOutChannels.add(chIndex)//memo
            inQueueFirst = null
            inQueueLast = null
            inPartialBunch = null
            close()
            return true
        }
        return false
    }

    abstract fun ReceivedBunch(bunch: Bunch)

    abstract fun close()
}