package com.jetbrains.kafka.tool.ui.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.IconLoader;

public class FailedToAvrifyPayloadNotification extends KafkaToolNotification {
    private static final String ICON_PATH = "/icons/error.png";
    private static final String CONTENT = "Failed to Avrify payload. Check that topic, schema and payload are correct.";

    private FailedToAvrifyPayloadNotification() {
        super("Failed to Avrify payload. Check that topic, schema and payload are correct.", NotificationType.INFORMATION);
        this.setIcon(IconLoader.getIcon("/icons/error.png"));
    }

    public static Notification getInstance() {
        return new FailedToAvrifyPayloadNotification();
    }
}