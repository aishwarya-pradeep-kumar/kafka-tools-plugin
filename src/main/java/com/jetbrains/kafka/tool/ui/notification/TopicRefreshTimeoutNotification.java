package com.jetbrains.kafka.tool.ui.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.IconLoader;

public class TopicRefreshTimeoutNotification extends KafkaToolNotification {
    private static final String ICON_PATH = "/icons/error.png";
    private static final String CONTENT = "Failed to collect topic list from confluent. Is it running?";

    private TopicRefreshTimeoutNotification() {
        super("Failed to collect topic list from confluent. Is it running?", NotificationType.INFORMATION);
        this.setIcon(IconLoader.getIcon("/icons/error.png"));
    }

    public static Notification getInstance() {
        return new TopicRefreshTimeoutNotification();
    }
}
