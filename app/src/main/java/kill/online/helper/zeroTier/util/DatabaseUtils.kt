package kill.online.helper.zeroTier.util

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 数据库访问工具类
 */
object DatabaseUtils {
    var readLock: Lock? = null
    var readWriteLock: ReadWriteLock? = null
    var writeLock: Lock? = null

    init {
        val reentrantReadWriteLock = ReentrantReadWriteLock()
        readWriteLock = reentrantReadWriteLock
        writeLock = reentrantReadWriteLock.writeLock()
        readLock = reentrantReadWriteLock.readLock()
    }
}
