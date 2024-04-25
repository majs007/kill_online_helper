package kill.online.helper.zeroTier.service

import android.util.Log
import com.zerotier.sdk.Node
import com.zerotier.sdk.PacketSender
import com.zerotier.sdk.ResultCode
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException

class UdpCom internal constructor(
    private val ztService: ZeroTierOneService,
    private val svrSocket: DatagramSocket?
) : PacketSender, Runnable {
    private var node: Node? = null
    fun setNode(node2: Node?) {
        node = node2
    }

    // com.zerotier.sdk.PacketSender
    override fun onSendPacketRequested(
        j: Long,
        inetSocketAddress: InetSocketAddress,
        bArr: ByteArray,
        i: Int
    ): Int {
        if (svrSocket == null) {
            Log.e(TAG, "Attempted to send packet on a null socket")
            return -1
        }
        return try {
            val datagramPacket = DatagramPacket(bArr, bArr.size, inetSocketAddress)
            Log.d(
                TAG,
                "onSendPacketRequested: Sent " + datagramPacket.length + " bytes to " + inetSocketAddress.toString()
            )
            svrSocket.send(datagramPacket)
            0
        } catch (unused: Exception) {
            -1
        }
    }

    override fun run() {
        Log.d(TAG, "UDP Listen Thread Started.")
        try {
            val jArr = LongArray(1)
            val bArr = ByteArray(16384)
            while (!Thread.interrupted()) {
                jArr[0] = 0
                val datagramPacket = DatagramPacket(bArr, 16384)
                try {
                    svrSocket!!.receive(datagramPacket)
                    if (datagramPacket.length > 0) {
                        val bArr2 = ByteArray(datagramPacket.length)
                        System.arraycopy(datagramPacket.data, 0, bArr2, 0, datagramPacket.length)
                        Log.d(
                            TAG,
                            "Got " + datagramPacket.length + " Bytes From: " + datagramPacket.address.toString() + ":" + datagramPacket.port
                        )
                        val processWirePacket = node!!.processWirePacket(
                            System.currentTimeMillis(),
                            -1,
                            InetSocketAddress(datagramPacket.address, datagramPacket.port),
                            bArr2,
                            jArr
                        )
                        if (processWirePacket != ResultCode.RESULT_OK) {
                            Log.e(TAG, "processWirePacket returned: $processWirePacket")
                            ztService.stopZeroTier()
                        }
                        ztService.nextBackgroundTaskDeadline = jArr[0]
                    }
                } catch (ignored: SocketTimeoutException) {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d(TAG, "UDP Listen Thread Ended.")
    }

    companion object {
        private const val TAG = "UdpCom"
    }
}
