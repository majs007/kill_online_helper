package kill.online.helper.zeroTier;

import android.content.Context;
import android.util.Log;

import kill.online.helper.zeroTier.model.DaoMaster;
import kill.online.helper.zeroTier.model.DaoSession;
import kill.online.helper.zeroTier.model.ZTOpenHelper;

/**
 * zeroTierFix database 单例类，此类必须首先初始化
 */
public class ZerotierFix {
    private static DaoSession mDaoSession = null;

    public static void init(Context context) {
        // 创建 DAO 会话
        mDaoSession = new DaoMaster(new ZTOpenHelper(context, "ztfixdb", null)
                .getWritableDatabase()).newSession();
        Log.i("ZerotierFix", "Starting ZerotierFix datebase");
    }

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }
}
