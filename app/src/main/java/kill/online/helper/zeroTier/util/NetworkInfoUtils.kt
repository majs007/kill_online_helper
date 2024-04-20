package kill.online.helper.zeroTier.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

/**
 * 网络连接信息处理工具类
 */
object NetworkInfoUtils {
    const val TAG = "NetworkInfoUtils"
    fun getNetworkInfoCurrentConnection(context: Context): CurrentConnection {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            ?: return CurrentConnection.CONNECTION_NONE
        return if (Build.VERSION.SDK_INT >= 23) {
            val networkCapabilities = connectivityManager
                .getNetworkCapabilities(connectivityManager.activeNetwork)
                ?: return CurrentConnection.CONNECTION_NONE
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                CurrentConnection.CONNECTION_MOBILE
            } else CurrentConnection.CONNECTION_OTHER
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
                ?: return CurrentConnection.CONNECTION_NONE
            if (!activeNetworkInfo.isConnectedOrConnecting) {
                return CurrentConnection.CONNECTION_NONE
            }
            if (activeNetworkInfo.type == 0) {
                CurrentConnection.CONNECTION_MOBILE
            } else CurrentConnection.CONNECTION_OTHER
        }
    }

    fun listMulticastGroupOnInterface(interfaceName: String, isIpv6: Boolean): List<String> {
        val groups = ArrayList<String>()
        val igmpFilePath: String = if (isIpv6) {
            "/proc/net/igmp6"
        } else {
            "/proc/net/igmp"
        }

        /*
         * 从 /proc/net/igmp 或 /proc/net/igmp6 的信息格式大致为:
         *
         * Idx     Device    : Count Querier       Group    Users Timer    Reporter
         * 1       tun0      :     2      V3
         *                                 010000E0     1 0:00000000               0
         * 2       wlan0     :     1      V3
         *                                 010000E0     1 0:00000000               0
         *
         * 因此，解析时需要先找到目标 interface 的行，然后开始读取若干行的组播组信息。
         */try {
            BufferedReader(FileReader(igmpFilePath)).use { igmpInfo ->
                var foundTargetInterface = false
                var line: String
                while (igmpInfo.readLine().also { line = it } != null) {
                    val row = line.split("\\s+".toRegex()).toTypedArray()
                    if (!foundTargetInterface) {
                        if (row.size > 1 && row[1] == interfaceName) {
                            // 找到目标 interface 的行
                            foundTargetInterface = true
                        }
                    } else {
                        if (row[0] == "") {
                            groups.add(row[1])
                        } else {
                            // 目标 interface 的信息结束
                            foundTargetInterface = false
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "IGMP info file not found", e)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading IGMP info", e)
        }
        return groups
    }

    enum class CurrentConnection {
        CONNECTION_NONE,
        CONNECTION_MOBILE,
        CONNECTION_OTHER
    }
}
