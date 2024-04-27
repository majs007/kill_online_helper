package kill.online.helper.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FileUtils {
    const val TAG = "FileUtils"
    lateinit var applicationContext: Context

    enum class DataType { Json, String, Int, Boolean }
    sealed class ItemName(val name: String) {
        data object Network : ItemName("network")
        data object NetworkConfig : ItemName("networkConfig")
        data object AppSetting : ItemName("appSetting")
        data object RoomRule : ItemName("roomRule")
        data object Room : ItemName("room")
        data object Blacklist : ItemName("blacklist")
    }

    fun isExist(
        itemName: ItemName,
        context: Context = applicationContext,
        fileName: String = "zeroTier",
    ): Boolean {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPreferences.contains(itemName.name)
    }

    inline fun <reified T> read(
        itemName: ItemName,
        defValue: T,
        dataType: DataType = DataType.Json,
        context: Context = applicationContext,
        fileName: String = "zeroTier",
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
        itemName: ItemName,
        content: T,
        dataType: DataType = DataType.Json,
        context: Context = applicationContext,
        fileName: String = "zeroTier",
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