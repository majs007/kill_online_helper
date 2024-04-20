package kill.online.helper.zeroTier.util

import android.util.Log
import androidx.core.view.MotionEventCompat
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
            j += (bArr[i].toInt() shl 8 and MotionEventCompat.ACTION_POINTER_INDEX_MASK).toLong()
            if (j and -65536L > 0) {
                j = (j and 65535L) + 1
            }
        }
        return j.inv() and 65535L
    }
}
