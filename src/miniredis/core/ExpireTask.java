package miniredis.core;

import java.util.concurrent.*;

public class ExpireTask {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void start(Context context) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanExpiredKeys(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS); // 每5秒执行一次
    }

    private static void cleanExpiredKeys(Context context) {

        var expireMap = context.getExpireMap();

        if (expireMap.isEmpty()) {
            return;
        }

        int sampleSize = Math.min(20, expireMap.size());

        int count = 0;

        for (String key : expireMap.keySet()) {

            if (count >= sampleSize) {
                break;
            }

            Long expireTime = expireMap.get(key);

            if (expireTime != null && System.currentTimeMillis() > expireTime) {
                context.getStore().remove(key);
                expireMap.remove(key);
            }
            count++;
        }
    }
}
