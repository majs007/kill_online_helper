package kill.online.helper.zeroTier.util

import android.util.Log
import java.net.InetAddress
import java.net.UnknownHostException


object IPPacketUtils {
    private const val TAG = "IPPacketUtils"
    fun getSourceIP(bArr: ByteArray): InetAddress? {
        val iPVersion = getIPVersion(bArr)
        return if (iPVersion.toInt() == 4) {
            val bArr2 = ByteArray(4)
            System.arraycopy(bArr, 12, bArr2, 0, 4)
            try {
                InetAddress.getByAddress(bArr2)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error creating InetAddress", e)
                null
            }
        } else if (iPVersion.toInt() == 6) {
            val bArr3 = ByteArray(16)
            System.arraycopy(bArr, 8, bArr3, 0, 16)
            try {
                InetAddress.getByAddress(bArr3)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error creating InetAddress", e)
                null
            }
        } else {
            Log.e(TAG, "Unknown IP version")
            null
        }
    }

    fun getDestIP(bArr: ByteArray): InetAddress? {
        val iPVersion = getIPVersion(bArr)
        return if (iPVersion.toInt() == 4) {
            val bArr2 = ByteArray(4)
            System.arraycopy(bArr, 16, bArr2, 0, 4)
            try {
                InetAddress.getByAddress(bArr2)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error creating InetAddress", e)
                null
            }
        } else if (iPVersion.toInt() == 6) {
            val bArr3 = ByteArray(16)
            System.arraycopy(bArr, 24, bArr3, 0, 16)
            try {
                InetAddress.getByAddress(bArr3)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error creating InetAddress", e)
                null
            }
        } else {
            Log.e(TAG, "Unknown IP version")
            null
        }
    }

    fun getIPVersion(bArr: ByteArray): Byte {
        return (bArr[0].toInt() shr 4).toByte()
    }

    fun calculateChecksum(bArr: ByteArray, j: Long, i: Int, i2: Int): Long {
        var j = j
        var i = i
        var i3 = i2 - i
        while (i3 > 1) {
            j += (65280 and (bArr[i].toInt() shl 8) or (bArr[i + 1].toInt() and 255)).toLong()
            if (-65536L and j > 0) {
                j = (j and 65535L) + 1
            }
            i += 2
            i3 -= 2
        }
        if (i3 > 0) {
            j += (bArr[i].toInt() shl 8 and 0xff00)
            if (j and -65536L > 0) {
                j = (j and 65535L) + 1
            }
        }
        return j.inv() and 65535L
    }

    private fun calculateChecksum(bytes: ByteArray): UShort {
        var sum: Long = 0

        // 将每两个字节的数据组合起来，并求和
        var i = 0
        while (i < bytes.size - 1) {
            sum += (bytes[i].toInt() and 0xFF) shl 8 or (bytes[i + 1].toInt() and 0xFF)
            i += 2
        }

        // 如果数据包长度为奇数，则将最后一个字节的值加到和中
        if (bytes.size % 2 != 0) {
            sum += (bytes[bytes.size - 1].toInt() and 0xFF) shl 8
        }

        // 将和的高位字节加到低位字节中
        sum = (sum and 0xFFFF) + (sum shr 16)

        // 取反得到校验和
        return (sum.inv() and 0xFFFF).toUShort()
    }

    fun setIPV4CheckSum(ipv4Header: ByteArray) {
        ipv4Header[10] = 0
        ipv4Header[11] = 0
        val checkSum = calculateChecksum(ipv4Header).toInt()
        ipv4Header[10] = (checkSum shr 8).toByte()
        ipv4Header[11] = checkSum.toByte()
    }

    fun setTCPCheckSum(ipv4: ByteArray) {
        val fakeHeader = ByteArray(12)
        val sourceAddress = ipv4.slice(12..15).toByteArray()
        val destinationAddress = ipv4.slice(16..19).toByteArray()

        sourceAddress.copyInto(fakeHeader, 0)
        destinationAddress.copyInto(fakeHeader, 4)
        fakeHeader[8] = 0
        fakeHeader[9] = 6
        fakeHeader[10] = ((ipv4.size - 20) shr 8).toByte()
        fakeHeader[11] = (ipv4.size - 20).toByte()

        val tcp = ipv4.drop(20).toByteArray()
        tcp[16] = 0
        tcp[17] = 0
        val checkSum = calculateChecksum(fakeHeader + tcp).toInt()
        ipv4[36] = (checkSum shr 8).toByte()
        ipv4[37] = checkSum.toByte()
    }

}
