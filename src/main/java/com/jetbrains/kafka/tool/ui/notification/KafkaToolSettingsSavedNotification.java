package com.jetbrains.kafka.tool.ui.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.IconLoader;

public class KafkaToolSettingsSavedNotification extends KafkaToolNotification {
    private static final String ICON_PATH = "/icons/GreenOK.png";
    private static final String CONTENT = "Saved.";

    private KafkaToolSettingsSavedNotification() {
        super("Saved.", NotificationType.INFORMATION);
        this.setIcon(IconLoader.getIcon("/icons/GreenOK.png"));
    }

    public static Notification getInstance() {
        return new KafkaToolSettingsSavedNotification();
    }
}

