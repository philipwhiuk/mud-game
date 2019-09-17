package com.whiuk.philip.mud.server;

import java.util.concurrent.ScheduledFuture;

public class Task {
    public final ScheduledFuture<?> future;
    public final String cancelledMessage;

    Task(ScheduledFuture<?> future, String cancelledMessage) {
        this.future = future;
        this.cancelledMessage = cancelledMessage;
    }
}
