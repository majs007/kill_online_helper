package kill.online.helper.zeroTier.service

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.zerotier.sdk.DataStoreGetListener
import com.zerotier.sdk.DataStorePutListener
import kill.online.helper.zeroTier.util.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Zerotier 文件数据源
 */
class DataStore(private val context: Context) : DataStoreGetListener, DataStorePutListener {
    override fun onDataStorePut(name: String, buffer: ByteArray, secure: Boolean): Int {
        Log.d(TAG, "Writing File: " + name + ", to: " + context.filesDir)
        // 保护自定义 Planet 文件
        return if (hookPlanetFile(name)) {
            0
        } else try {
            if (name.contains("/")) {
                val file = File(context.filesDir, name.substring(0, name.lastIndexOf('/')))
                if (!file.exists()) {
                    file.mkdirs()
                }
                val fileOutputStream =
                    FileOutputStream(File(file, name.substring(name.lastIndexOf('/') + 1)))
                fileOutputStream.write(buffer)
                fileOutputStream.flush()
                fileOutputStream.close()
                return 0
            }
            val openFileOutput = context.openFileOutput(name, 0)
            openFileOutput.write(buffer)
            openFileOutput.flush()
            openFileOutput.close()
            0
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            -1
        } catch (e2: IOException) {
            val stringWriter = StringWriter()
            e2.printStackTrace(PrintWriter(stringWriter))
            Log.e(TAG, stringWriter.toString())
            -2
        } catch (e3: IllegalArgumentException) {
            val stringWriter2 = StringWriter()
            e3.printStackTrace(PrintWriter(stringWriter2))
            Log.e(TAG, stringWriter2.toString())
            -3
        }
    }

    override fun onDelete(name: String): Int {
        Log.d(TAG, "Deleting File: $name")
        // 保护自定义 Planet 文件
        if (hookPlanetFile(name)) {
            return 0
        }
        val deleted: Boolean = if (name.contains("/")) {
            val file = File(context.filesDir, name)
            if (!file.exists()) {
                true
            } else {
                file.delete()
            }
        } else {
            context.deleteFile(name)
        }
        return if (!deleted) 1 else 0
    }

    override fun onDataStoreGet(name: String, outBuffer: ByteArray): Long {
        var planetName = name
        Log.d(TAG, "Reading File: $planetName")
        if (hookPlanetFile(planetName)) {
            planetName = Constants.FILE_CUSTOM_PLANET
        }
        // 读入文件
        return try {
            if (planetName.contains("/")) {
                val file =
                    File(context.filesDir, planetName.substring(0, planetName.lastIndexOf('/')))
                if (!file.exists()) {
                    file.mkdirs()
                }
                val file2 = File(file, planetName.substring(planetName.lastIndexOf('/') + 1))
                if (!file2.exists()) {
                    return 0
                }
                val fileInputStream = FileInputStream(file2)
                val read = fileInputStream.read(outBuffer)
                fileInputStream.close()
                return read.toLong()
            }
            val openFileInput = context.openFileInput(planetName)
            val read2 = openFileInput.read(outBuffer)
            openFileInput.close()
            read2.toLong()
        } catch (unused: FileNotFoundException) {
            -1
        } catch (e: IOException) {
            Log.e(TAG, "", e)
            -2
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            -3
        }
    }

    /**
     * 判断自定义 Planet 文件
     */
    fun hookPlanetFile(name: String): Boolean {
        return if (Constants.FILE_PLANET == name) {
            PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(Constants.PREF_PLANET_USE_CUSTOM, false)
        } else false
    }

    companion object {
        private const val TAG = "DataStore"
    }
}
