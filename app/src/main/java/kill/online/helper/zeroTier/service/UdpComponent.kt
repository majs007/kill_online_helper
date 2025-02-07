package kill.online.helper.zeroTier.service

import android.util.Log
import com.zerotier.sdk.Node
import com.zerotier.sdk.PacketSender
import com.zerotier.sdk.ResultCode
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException

class UdpComponent internal constructor(
    private val ztService: ZeroTierOneService,
    private val svrSocket: DatagramSocket?
) : PacketSender, Runnable {
    private var node: Node? = null

    fun setNode(node: Node?) {
        this.node = node
    }

    // com.zerotier.sdk.PacketSender
    override fun onSendPacketRequested(
        mac: Long,
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

    //服务线程，接受发来的数据包
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
                        //收到udp 数据包 交由zerotier处理
                        val resultCode = node!!.processWirePacket(
                            System.currentTimeMillis(),
                            -1,
                            InetSocketAddress(datagramPacket.address, datagramPacket.port),
                            bArr2,
                            jArr
                        )
                        if (resultCode != ResultCode.RESULT_OK) {
                            Log.e(TAG, "processWirePacket returned: $resultCode")
                            ztService.shutdown()
                        }
                        ztService.setDeadline(jArr[0])
                    }
                } catch (ignored: SocketTimeoutException) {
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        Log.d(TAG, "UDP Listen Thread Ended.")
    }

    companion object {
        private const val TAG = "UdpComponent"
    }
}
