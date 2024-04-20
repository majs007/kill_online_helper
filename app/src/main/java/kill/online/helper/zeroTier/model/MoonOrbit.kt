package kill.online.helper.zeroTier.model

import android.content.Context
import java.io.File

/**
 * Moon 入轨信息实体
 */
data class MoonOrbit(
    /**
     * Moon 地址
     */
    var moonWorldId: Long? = null,

    /**
     * Moon 种子
     */
    var moonSeed: Long? = null,

    /**
     * Moon 是否从文件导入
     */
    var fromFile: Boolean = false
) {
    /**
     * Moon 文件是否已缓存
     */
    private var cacheFile = false

    /**
     * 判断 Moon 是否有缓存
     */
    fun checkCacheFile(context: Context) {
        val moon = File(context.filesDir, String.format(MOON_FILE_PATH, moonWorldId))
        cacheFile = moon.exists()
    }

    /**
     * 删除 Moon 缓存
     */
    fun deleteCacheFile(context: Context) {
        val moon = File(context.filesDir, String.format(MOON_FILE_PATH, moonWorldId))
        moon.delete()
    }

    companion object {
        /**
         * Moon 文件相对路径格式
         */
        const val MOON_FILE_PATH = "moons.d/%016x.moon"
    }
}
