package miniredis.utils;

import miniredis.core.Context;

public class ExpireUtil {

    public static boolean isExpired(Context context, String key) {
        Long expireTime = context.getExpireMap().get(key);

        if (expireTime == null) {
            return false;
        }

        if (System.currentTimeMillis() > expireTime) {
            // 删除
            context.getStore().remove(key);
            context.getExpireMap().remove(key);
            return true;
        }

        return false;
    }
}
