package com.jetbrains.kafka.tool.ui;

import com.google.inject.Inject;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactory.SERVICE;
import com.jetbrains.kafka.tool.service.KafkaProducerProxy;
import com.jetbrains.kafka.tool.service.KafkaToolPersistentStateComponent;
import com.jetbrains.kafka.tool.ui.notification.KafkaToolSettingsSavedNotification;

import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.basic.BasicPanelUI;

public class KafkaSettingsComponent implements DumbAware {
    private static final String SETTINGS_LABEL = "Settings";
    @Inject
    private KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;
    @Inject
    private KafkaProducerProxy kafkaProducerProxy;
    private Project project;
    private JPanel mainPanel;
    private JTextField schemaRegistrySettingField;
    private JTextField bootstrapServersSettingField;
    private JButton saveSettingsButton;
    private JTextField avroPackagePrefixField;

    public KafkaSettingsComponent() {
        this.mainPanel.setUI(mainPanel.getUI());
    }

    public Content getContent(Project project) {
        this.project = project;
        this.bootstrapServersSettingField.setText(this.kafkaToolPersistentStateComponent.getBootstrapServers());
        this.schemaRegistrySettingField.setText(this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl());
        this.avroPackagePrefixField.setText(this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        this.avroPackagePrefixField.setText(this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        this.saveSettingsButton.addActionListener((e) -> {
            this.kafkaToolPersistentStateComponent.setBootstrapServers(this.bootstrapServersSettingField.getText());
            this.kafkaToolPersistentStateComponent.setSchemaRegistryUrl(this.schemaRegistrySettingField.getText());
            this.kafkaToolPersistentStateComponent.setAvroPackagePrefix(this.avroPackagePrefixField.getText());
            Bus.notify(KafkaToolSettingsSavedNotification.getInstance());
            this.kafkaProducerProxy.reset();
        });
        ContentFactory contentFactory = SERVICE.getInstance();
        return contentFactory.createContent(this.mainPanel, "Settings", false);
    }
}
