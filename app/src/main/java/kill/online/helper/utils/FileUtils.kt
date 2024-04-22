package kill.online.helper.utils

import android.content.Context
import com.google.gson.Gson

object FileUtils {
    enum class DataType { Json, String, Int, Boolean }
    sealed class ItemName(val name: String) {
        data object Network : ItemName("network")
        data object NetworkConfig : ItemName("networkConfig")
        data object AppSetting : ItemName("appSetting")
        data object UseCellularData : ItemName("useCellularData")
        data object DisableIpv6 : ItemName("DisableIpv6")
    }

    inline fun <reified T> read(
        fileName: String = "zeroTier",
        context: Context,
        dataType: DataType = DataType.Json,
        itemName: ItemName,
        defValue: T,
        callback: (content: T) -> Unit = {}
    ): T {
        val gson = Gson()
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val strContent = sharedPreferences.getString(itemName.name, null) ?: return defValue
        return when (dataType) {
            DataType.Json -> {
                val content: T = gson.fromJson(strContent, T::class.java)
                callback(content)
                content
            }

            DataType.String -> {
                callback(strContent as T)
                strContent
            }

            DataType.Int -> {
                val intContent = strContent.toInt() as T
                callback(intContent)
                intContent
            }

            DataType.Boolean -> {
                val intContent = strContent.toBoolean() as T
                callback(intContent)
                intContent
            }
        }
    }

    fun <T> write(
        fileName: String = "zeroTier",
        context: Context,
        dataType: DataType = DataType.Json,
        itemName: ItemName,
        content: T,
        callback: (strContent: String) -> Unit = {}
    ) {
        val gson = Gson()
        val editor = context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit()
        val strContent: String = when (dataType) {
            DataType.Json -> {
                gson.toJson(content)
            }

            else -> {
                content.toString()
            }
        }
        editor.putString(itemName.name, strContent)
        editor.apply()
        callback(strContent)
    }

}