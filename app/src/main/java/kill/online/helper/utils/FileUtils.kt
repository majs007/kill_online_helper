package kill.online.helper.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FileUtils {
    const val TAG = "FileUtils"

    enum class DataType { Json, String, Int, Boolean }
    sealed class ItemName(val name: String) {
        data object Network : ItemName("network")
        data object NetworkConfig : ItemName("networkConfig")
        data object AppSetting : ItemName("appSetting")
        data object RoomRule : ItemName("roomRule")
    }

    fun isExist(
        fileName: String = "zeroTier",
        context: Context,
        itemName: ItemName
    ): Boolean {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPreferences.contains(itemName.name)
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
        Log.i(TAG, "read: strContent = $strContent")
        return when (dataType) {
            DataType.Json -> {
                val type = object : TypeToken<T>() {}.type
                val content: T = gson.fromJson(strContent, type)
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
        prefix: String = "",
        content: T,
        suffix: String = "",
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
        Log.i(TAG, "write: strContent = $strContent")
        editor.putString(itemName.name, strContent)
        editor.apply()
        callback(strContent)
    }

}