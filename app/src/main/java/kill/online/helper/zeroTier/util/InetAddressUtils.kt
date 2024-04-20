package kill.online.helper.zeroTier.util

import android.util.Log
import java.math.BigInteger
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import kotlin.math.abs

object InetAddressUtils {
    const val TAG = "InetAddressUtils"
    const val BROADCAST_MAC_ADDRESS = 0xffffffffffffL

    /**
     * 获得地址指定 CIDR 的子网掩码
     */
    fun addressToNetmask(address: InetAddress, cidr: Int): ByteArray {
        val length = address.address.size
        val subnetLength = length * 8 - cidr
        val fullMasked = ByteArray(length)
        for (i in 0 until length) {
            fullMasked[i] = -1
        }
        return if (length == 4) {
            // IPv4 地址
            ByteBuffer.allocate(4)
                .putInt(ByteBuffer.wrap(fullMasked).getInt() shr subnetLength shl subnetLength)
                .array()
        } else {
            // IPv6 地址
            if (cidr == 0) {
                // 若 CIDR 为 0 则返回空子网掩码
                return ByteArray(length)
            }
            val shiftedAddress = BigInteger(fullMasked)
                .shiftRight(subnetLength)
                .shiftLeft(subnetLength)
                .toByteArray()
            if (shiftedAddress.size == length) {
                return shiftedAddress
            }
            // 高位为 0 时需要在前补 0
            val netmask = ByteArray(length)
            val offset = abs((length - shiftedAddress.size).toDouble()).toInt()
            for (i in 0 until offset) {
                netmask[i] = shiftedAddress[0]
            }
            System.arraycopy(shiftedAddress, 0, netmask, offset, shiftedAddress.size)
            netmask
        }
    }

    fun addressToRoute(inetAddress: InetAddress, i: Int): InetAddress? {
        if (i == 0) {
            if (inetAddress is Inet4Address) {
                return try {
                    Inet4Address.getByAddress(byteArrayOf(0, 0, 0, 0))
                } catch (unused: UnknownHostException) {
                    null
                }
            } else if (inetAddress is Inet6Address) {
                val bArr = ByteArray(16)
                for (i2 in 0..15) {
                    bArr[i2] = 0
                }
                return try {
                    Inet6Address.getByAddress(bArr)
                } catch (unused2: UnknownHostException) {
                    null
                }
            }
        }
        return addressToRouteNo0Route(inetAddress, i)
    }

    /**
     * 获得地址对应的网络前缀
     */
    fun addressToRouteNo0Route(address: InetAddress, cidr: Int): InetAddress? {
        val netmask = addressToNetmask(address, cidr)
        val rawAddress = ByteArray(netmask.size)
        for (i in netmask.indices) {
            rawAddress[i] = (address.address[i].toInt() and netmask[i].toInt()).toByte()
        }
        return try {
            InetAddress.getByAddress(rawAddress)
        } catch (unused: UnknownHostException) {
            Log.e(TAG, "Unknown Host Exception calculating route")
            null
        }
    }

    fun ipv6ToMulticastAddress(inetAddress: InetAddress): Long {
        val address = inetAddress.address
        return if (address.size != 16) {
            0
        } else ByteBuffer.wrap(
            byteArrayOf(
                0,
                0,
                51,
                51,
                -1,
                address[13],
                address[14],
                address[15]
            )
        ).getLong()
    }
}
