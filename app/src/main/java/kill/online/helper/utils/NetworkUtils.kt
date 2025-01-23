package kill.online.helper.utils

import android.annotation.SuppressLint
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.roundToInt


object NetworkUtils {
    val sSLSocketFactory: SSLSocketFactory
        // 获取这个SSLSocketFactory
        get() = try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustManager, SecureRandom())
            sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    val trustManager: Array<TrustManager>
        // 获取TrustManager
        get() {
            return arrayOf(@SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })
        }

    fun pingTest(ipAddress: String): Int {
        val TAG = "pingTest"
        return try {
            // 使用 '-c 1' 参数执行1次
            val command =
                if (ipAddress.contains(':')) "ping6 -c 1 $ipAddress" else "ping -c 1 $ipAddress"
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var lastLine: String = ""
            while (reader.readLine()?.also { lastLine = it } != null)
                process.waitFor()
            if (process.exitValue() == 0) {
                val ok = "Ping to $ipAddress succeeded: $lastLine"
                Log.i(TAG, "ok: $ok")
                val argRTT = ok.split("=")[1].split("/")[1].trim()
                Log.i(TAG, "argRTT: $argRTT")
                argRTT.toFloat().roundToInt()
            } else {
                val failed = "Ping to $ipAddress failed"
                Log.i(TAG, "failed: $failed")
                -1
            }
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
            -1
        }
    }
}

