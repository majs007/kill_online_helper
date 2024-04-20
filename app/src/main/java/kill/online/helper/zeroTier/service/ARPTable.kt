package kill.online.helper.zeroTier.service

import android.util.Log
import java.net.InetAddress
import java.nio.ByteBuffer

// TODO: clear up
class ARPTable {
    private val entriesMap = HashMap<Long, ARPEntry>()
    private val inetAddressToMacAddress = HashMap<InetAddress, Long>()
    private val ipEntriesMap = HashMap<InetAddress?, ARPEntry>()
    private val macAddressToInetAdddress = HashMap<Long, InetAddress>()
    private val timeoutThread: Thread = object : Thread("ARP Timeout Thread") {
        /* class com.zerotier.one.service.ARPTable.AnonymousClass1 */
        override fun run() {
            while (!isInterrupted) {
                try {
                    for (arpEntry in entriesMap.values) {
                        if (arpEntry.time + ENTRY_TIMEOUT < System.currentTimeMillis()) {
                            Log.d(
                                TAG,
                                "Removing " + arpEntry.address.toString() + " from ARP cache"
                            )
                            synchronized(macAddressToInetAdddress) {
                                macAddressToInetAdddress.remove(
                                    arpEntry.mac
                                )
                            }
                            synchronized(inetAddressToMacAddress) {
                                inetAddressToMacAddress.remove(
                                    arpEntry.address
                                )
                            }
                            synchronized(entriesMap) { entriesMap.remove(arpEntry.mac) }
                            synchronized(ipEntriesMap) { ipEntriesMap.remove(arpEntry.address) }
                        }
                    }
                    sleep(1000)
                    setAddress(InetAddress.getByName("255.255.255.255"), 0xffffffffffff)
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Tun/Tap Interrupted", e)
                    break
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }
            }
            Log.d(TAG, "ARP Timeout Thread Ended.")
        }
    }

    init {
        timeoutThread.start()
    }

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
        synchronized(macAddressToInetAdddress) { macAddressToInetAdddress.put(j, inetAddress) }
        val arpEntry = ARPEntry(j, inetAddress)
        synchronized(entriesMap) { entriesMap.put(j, arpEntry) }
        synchronized(ipEntriesMap) { ipEntriesMap.put(inetAddress, arpEntry) }
    }

    private fun updateArpEntryTime(j: Long) {
        synchronized(entriesMap) {
            val arpEntry = entriesMap[j]
            arpEntry?.updateTime()
        }
    }

    private fun updateArpEntryTime(inetAddress: InetAddress?) {
        synchronized(ipEntriesMap) {
            val arpEntry = ipEntriesMap[inetAddress]
            arpEntry?.updateTime()
        }
    }

    /* access modifiers changed from: package-private */
    fun getMacForAddress(inetAddress: InetAddress): Long {
        synchronized(inetAddressToMacAddress) {
            if (!inetAddressToMacAddress.containsKey(inetAddress)) {
                return -1
            }
            Log.d(TAG, "Returning MAC for $inetAddress")
            val longValue = inetAddressToMacAddress[inetAddress]
            if (longValue != null) {
                updateArpEntryTime(longValue)
                return longValue
            }
        }
        return -1
    }

    /* access modifiers changed from: package-private */
    fun getAddressForMac(j: Long): InetAddress? {
        synchronized(macAddressToInetAdddress) {
            if (!macAddressToInetAdddress.containsKey(j)) {
                return null
            }
            val inetAddress = macAddressToInetAdddress[j]
            updateArpEntryTime(inetAddress)
            return inetAddress
        }
    }

    fun hasMacForAddress(inetAddress: InetAddress): Boolean {
        var containsKey: Boolean
        synchronized(inetAddressToMacAddress) {
            containsKey = inetAddressToMacAddress.containsKey(inetAddress)
        }
        return containsKey
    }

    fun hasAddressForMac(j: Long): Boolean {
        var containsKey: Boolean
        synchronized(macAddressToInetAdddress) {
            containsKey = macAddressToInetAdddress.containsKey(j)
        }
        return containsKey
    }

    fun getRequestPacket(j: Long, inetAddress: InetAddress, inetAddress2: InetAddress): ByteArray {
        return getARPPacket(1, j, 0, inetAddress, inetAddress2)
    }

    fun getReplyPacket(
        j: Long,
        inetAddress: InetAddress,
        j2: Long,
        inetAddress2: InetAddress
    ): ByteArray {
        return getARPPacket(2, j, j2, inetAddress, inetAddress2)
    }

    fun getARPPacket(
        i: Int,
        j: Long,
        j2: Long,
        inetAddress: InetAddress,
        inetAddress2: InetAddress
    ): ByteArray {
        val bArr = ByteArray(28)
        bArr[0] = 0
        bArr[1] = 1
        bArr[2] = 8
        bArr[3] = 0
        bArr[4] = 6
        bArr[5] = 4
        bArr[6] = 0
        bArr[7] = i.toByte()
        System.arraycopy(longToBytes(j), 2, bArr, 8, 6)
        System.arraycopy(inetAddress.address, 0, bArr, 14, 4)
        System.arraycopy(longToBytes(j2), 2, bArr, 18, 6)
        System.arraycopy(inetAddress2.address, 0, bArr, 24, 4)
        return bArr
    }

    fun processARPPacket(packetData: ByteArray): ARPReplyData? {
        val srcAddress: InetAddress?
        val dstAddress: InetAddress?
        Log.d(TAG, "Processing ARP packet")

        // 解析包内 IP、MAC 地址
        val rawSrcMac = ByteArray(8)
        System.arraycopy(packetData, 8, rawSrcMac, 2, 6)
        val rawSrcAddress = ByteArray(4)
        System.arraycopy(packetData, 14, rawSrcAddress, 0, 4)
        val rawDstMac = ByteArray(8)
        System.arraycopy(packetData, 18, rawDstMac, 2, 6)
        val rawDstAddress = ByteArray(4)
        System.arraycopy(packetData, 24, rawDstAddress, 0, 4)
        srcAddress = try {
            InetAddress.getByAddress(rawSrcAddress)
        } catch (unused: Exception) {
            null
        }
        dstAddress = try {
            InetAddress.getByAddress(rawDstAddress)
        } catch (unused: Exception) {
            null
        }
        val srcMac = ByteBuffer.wrap(rawSrcMac).getLong()
        val dstMac = ByteBuffer.wrap(rawDstMac).getLong()

        // 更新 ARP 表项
        if (srcMac != 0L && srcAddress != null) {
            setAddress(srcAddress, srcMac)
        }
        if (dstMac != 0L && dstAddress != null) {
            setAddress(dstAddress, dstMac)
        }

        // 处理响应行为
        val packetType = packetData[7]
        return if (packetType.toInt() == REQUEST) {
            // ARP 请求，返回应答数据
            Log.d(TAG, "Reply needed")
            ARPReplyData(srcMac, srcAddress)
        } else {
            null
        }
    }

    companion object {
        const val TAG = "ARPTable"
        private const val ENTRY_TIMEOUT: Long = 120000
        private const val REPLY = 2
        private const val REQUEST = 1
        fun longToBytes(j: Long): ByteArray {
            val allocate = ByteBuffer.allocate(8)
            allocate.putLong(j)
            return allocate.array()
        }
    }
}
