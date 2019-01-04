package com.jetbrains.kafka.tool.ui.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public abstract class KafkaToolNotification extends Notification {
    private static final String NOTIFICATION_GROUP_DISPLAY_ID = "Kafka Tool Notification";
    private static final String NOTIFICATION_TITLE = "Kafka Tool";

    protected KafkaToolNotification(@NotNull String content, @NotNull NotificationType type) {
        super("Kafka Tool Notification", "Kafka Tool", content, type);
        if (content == null) {
//            $$$reportNull$$$0(0);
        }

        if (type == null) {
//            $$$reportNull$$$0(1);
        }
    }
}