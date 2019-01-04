package com.jetbrains.kafka.tool.ui.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.IconLoader;

public class MissingRequiredFieldsNotification extends KafkaToolNotification {
    private static final String ICON_PATH = "/icons/error.png";
    private static final String CONTENT = "Required fields must be populated: topic, schema, payload.";

    private MissingRequiredFieldsNotification() {
        super("Required fields must be populated: topic, schema, payload.", NotificationType.INFORMATION);
        this.setIcon(IconLoader.getIcon("/icons/error.png"));
    }

    public static Notification getInstance() {
        return new MissingRequiredFieldsNotification();
    }
}
