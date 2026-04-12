package miniredis.command.impl.expire;

import miniredis.command.Command;
import miniredis.core.Context;

public class ExpireCommand implements Command {

    private String key;
    private int seconds;

    public ExpireCommand(String key, int seconds) {
        this.key = key;
        this.seconds = seconds;
    }

    @Override
    public String execute(Context context) {

        if (!context.getStore().containsKey(key)) {
            return "0";
        }

        long expireTime = System.currentTimeMillis() + seconds * 1000L;
        context.getExpireMap().put(key, expireTime);

        return "1";
    }
}
