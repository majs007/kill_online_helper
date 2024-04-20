package kill.online.helper.zeroTier.service

import android.util.Log
import kill.online.helper.zeroTier.util.IPPacketUtils
import java.net.InetAddress
import java.nio.ByteBuffer

// TODO: clear up
class NDPTable {
    private val entriesMap = HashMap<Long, NDPEntry>()
    private val inetAddressToMacAddress = HashMap<InetAddress, Long>()
    private val ipEntriesMap = HashMap<InetAddress?, NDPEntry>()
    private val macAddressToInetAddress = HashMap<Long, InetAddress>()
    private val timeoutThread: Thread = object : Thread("NDP Timeout Thread") {
        override fun run() {
            while (!isInterrupted) {
                try {
                    for (nDPEntry in HashMap(entriesMap).values) {
                        if (nDPEntry.time + ENTRY_TIMEOUT < System.currentTimeMillis()) {
                            synchronized(macAddressToInetAddress) {
                                macAddressToInetAddress.remove(
                                    nDPEntry.mac
                                )
                            }
                            synchronized(inetAddressToMacAddress) {
                                inetAddressToMacAddress.remove(
                                    nDPEntry.address
                                )
                            }
                            synchronized(entriesMap) { entriesMap.remove(nDPEntry.mac) }
                            synchronized(ipEntriesMap) { ipEntriesMap.remove(nDPEntry.address) }
                        }
                    }
                    sleep(1000)
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    return
                }
            }
        }
    }

    init {
        timeoutThread.start()
    }

    /* access modifiers changed from: protected */
    fun stop() {
        try {
            timeoutThread.interrupt()
            timeoutThread.join()
        } catch (ignored: InterruptedException) {
        }
    }

    /* access modifiers changed from: package-private */
    fun setAddress(inetAddress: InetAddress, j: Long) {
        synchronized(inetAddressToMacAddress) { inetAddressToMacAddress.put(inetAddress, j) }
        synchronized(macAddressToInetAddress) { macAddressToInetAddress.put(j, inetAddress) }
        val nDPEntry = NDPEntry(j, inetAddress)
        synchronized(entriesMap) { entriesMap.put(j, nDPEntry) }
        synchronized(ipEntriesMap) { ipEntriesMap.put(inetAddress, nDPEntry) }
    }

    /* access modifiers changed from: package-private */
    fun hasMacForAddress(inetAddress: InetAddress): Boolean {
        var containsKey: Boolean
        synchronized(inetAddressToMacAddress) {
            containsKey = inetAddressToMacAddress.containsKey(inetAddress)
        }
        return containsKey
    }

    /* access modifiers changed from: package-private */
    fun hasAddressForMac(j: Long): Boolean {
        var containsKey: Boolean
        synchronized(macAddressToInetAddress) {
            containsKey = macAddressToInetAddress.containsKey(j)
        }
        return containsKey
    }

    /* access modifiers changed from: package-private */
    fun getMacForAddress(inetAddress: InetAddress): Long {
        synchronized(inetAddressToMacAddress) {
            if (!inetAddressToMacAddress.containsKey(inetAddress)) {
                return -1
            }
            val longValue = inetAddressToMacAddress[inetAddress]!!
            updateNDPEntryTime(longValue)
            return longValue
        }
    }

    /* access modifiers changed from: package-private */
    fun getAddressForMac(j: Long): InetAddress? {
        synchronized(macAddressToInetAddress) {
            if (!macAddressToInetAddress.containsKey(j)) {
                return null
            }
            val inetAddress = macAddressToInetAddress[j]
            updateNDPEntryTime(inetAddress)
            return inetAddress
        }
    }

    private fun updateNDPEntryTime(inetAddress: InetAddress?) {
        synchronized(ipEntriesMap) {
            val nDPEntry = ipEntriesMap[inetAddress]
            nDPEntry?.updateTime()
        }
    }

    private fun updateNDPEntryTime(j: Long) {
        synchronized(entriesMap) {
            val nDPEntry = entriesMap[j]
            nDPEntry?.updateTime()
        }
    }

    /* access modifiers changed from: package-private */
    fun getNeighborSolicitationPacket(
        inetAddress: InetAddress,
        inetAddress2: InetAddress,
        j: Long
    ): ByteArray {
        val bArr = ByteArray(72)
        System.arraycopy(inetAddress.address, 0, bArr, 0, 16)
        System.arraycopy(inetAddress2.address, 0, bArr, 16, 16)
        System.arraycopy(ByteBuffer.allocate(4).putInt(32).array(), 0, bArr, 32, 4)
        bArr[39] = 58
        bArr[40] = -121
        System.arraycopy(inetAddress2.address, 0, bArr, 48, 16)
        val array = ByteBuffer.allocate(8).putLong(j).array()
        bArr[64] = 1
        bArr[65] = 1
        System.arraycopy(array, 2, bArr, 66, 6)
        System.arraycopy(
            ByteBuffer.allocate(2).putShort(
                IPPacketUtils.calculateChecksum(bArr, 0, 0, 72).toInt().toShort()
            ).array(), 0, bArr, 42, 2
        )
        for (i in 0..39) {
            bArr[i] = 0
        }
        bArr[0] = 96
        System.arraycopy(ByteBuffer.allocate(2).putShort(32.toShort()).array(), 0, bArr, 4, 2)
        bArr[6] = 58
        bArr[7] = -1
        System.arraycopy(inetAddress.address, 0, bArr, 8, 16)
        System.arraycopy(inetAddress2.address, 0, bArr, 24, 16)
        return bArr
    }

    companion object {
        const val TAG = "NDPTable"
        private const val ENTRY_TIMEOUT: Long = 120000
    }
}
