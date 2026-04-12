package miniredis.command;

import miniredis.core.Context;

public interface Command {
    String execute(Context context);
}
