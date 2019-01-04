package com.jetbrains.kafka.tool.ui.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.IconLoader;

public class FailedToProduceMessageNotification extends KafkaToolNotification {
    private static final String ICON_PATH = "/icons/error.png";
    private static final String CONTENT = "Failed to produce message to Kafka.";

    private FailedToProduceMessageNotification() {
        super("Failed to produce message to Kafka.", NotificationType.INFORMATION);
        this.setIcon(IconLoader.getIcon("/icons/error.png"));
    }

    public static Notification getInstance() {
        return new FailedToProduceMessageNotification();
    }
}
