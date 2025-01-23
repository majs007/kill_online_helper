package kill.online.helper.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    const val TAG = "FileUtils"
    lateinit var applicationContext: Context

    enum class DataType { Json, String, Int, Boolean }
    sealed class ItemName(val name: String) {
        data object ZTNetworks : ItemName("ztNetworks")
        data object ZTMoons : ItemName("ztMoons")
        data object AppSetting : ItemName("appSetting")
        data object RoomRules : ItemName("roomRules")
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
        defaultValue: T,
        dataType: DataType = DataType.Json,
        context: Context = applicationContext,
        fileName: String = "zeroTier",
        callback: (content: T) -> Unit = {}
    ): T {
        val gson = Gson()
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val strContent = sharedPreferences.getString(itemName.name, null) ?: return defaultValue

        Log.i(TAG, "read:$itemName strContent = $strContent")
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
        Log.i(TAG, "write:$itemName strContent = $strContent")
        editor.putString(itemName.name, strContent)
        editor.apply()
        callback(strContent)
    }

    fun writeBytesToFile(file: File, data: ByteArray) {
        try {
            if (file.parentFile?.exists() == false) {
                // 创建父目录
                file.parentFile?.mkdirs()
            }
            FileOutputStream(file).use { fos ->
                fos.write(data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "writeBytesToFile:\n${e.message}")
        }

    }


    fun writeBytesToFile(context: Context, dirName: String, fileName: String, data: ByteArray) {
        try {
            val dir = context.getExternalFilesDir(dirName)
            val file = File(dir, fileName)
            if (file.parentFile?.exists() == false) {
                // 创建父目录
                file.parentFile?.mkdirs()
            }
            FileOutputStream(file).use { fos ->
                fos.write(data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "writeBytesToFile:\n${e.message}")
        }
    }

    fun deleteFile(context: Context, dirName: String, fileName: String) {
        try {
            val dir = context.getExternalFilesDir(dirName)
            val file = File(dir, fileName)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteFile:\n${e.message}")
        }
    }


}