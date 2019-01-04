package com.jetbrains.kafka.tool.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactory.SERVICE;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.swing.*;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.jetbrains.kafka.tool.exception.KafkaToolProducerException;
import com.jetbrains.kafka.tool.produce.avro.shared.util.Opt;
import com.jetbrains.kafka.tool.service.*;
import com.jetbrains.kafka.tool.service.model.ProducerHistoryEntry;
import com.jetbrains.kafka.tool.service.shared.DefaultJsonToAvroConverter;
import com.jetbrains.kafka.tool.ui.notification.FailedToAvrifyPayloadNotification;
import com.jetbrains.kafka.tool.ui.notification.FailedToProduceMessageNotification;
import com.jetbrains.kafka.tool.ui.notification.MissingRequiredFieldsNotification;
import com.jetbrains.kafka.tool.ui.notification.SuccessfullyProducedMessageNotification;
import org.apache.avro.Schema.Parser;
import org.jetbrains.annotations.NotNull;

public class KafkaProducerComponent implements DumbAware {
    private static final String SCHEMA_FIELD_NAME = "SCHEMA$";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PRODUCE_LABEL = "Producer";
    private static final int DOUBLE_CLICK_COUNT = 2;
    private static final int ZERO_INDEX = 0;
    private final Map<String, Class> avroClasses;
    private Project project;
    @Inject
    private ConfluentServiceImpl confluentService;
    @Inject
    private AvroClassScannerImpl avroService;
    @Inject
    private DefaultJsonToAvroConverter defaultJsonToAvroConverter;
    @Inject
    private KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;
    @Inject
    private KafkaToolProducerCache kafkaToolProducerCache;
    @Inject
    private KafkaProducerProxy kafkaProducerProxy;
    @Inject
    private KafkaToolSerializerService kafkaToolSerializerService;
    private JTabbedPane tabbedPane;
    private JComboBox<String> topicComboBox;
    private JButton produceButton;
    private JComboBox<String> avroClassComboBox;
    private JTextArea schemaEditor;
    private JTextField keyField;
    private JTextField selectedTopicField;
    private JComboBox<String> serializerComboBox;
    private JButton topicRefreshButton;
    private JButton refreshAvroClasses;
    private JTextArea payloadEditor;
    private JList<String> producerHistoryList;
    private JLabel historyCountLabel;
    private DefaultListModel<String> historyListModel;

    public KafkaProducerComponent() {
        this.setupUI();
        this.avroClasses = new HashMap();
    }

    public Content getContent(@NotNull Project project) {
        if (project == null) {
            this.reportNull(0);
        }

        this.project = project;
        this.kafkaProducerProxy.reset();
        this.historyListModel = new DefaultListModel();
        this.producerHistoryList.setModel(this.historyListModel);
        this.topicComboBox.removeAllItems();
        this.avroClassComboBox.removeAllItems();
        Set var10000 = this.kafkaToolSerializerService.getAllSerializers();
        JComboBox var10001 = this.serializerComboBox;
        var10000.forEach(var10001::addItem);
        this.topicComboBox.addItemListener((e) -> {
            if (e.getStateChange() == 1) {
                this.selectedTopicField.setText(String.valueOf(e.getItem()).trim());
            }

        });
        this.produceButton.addActionListener((e) -> {
            this.produceMessage();
        });
        this.produceButton.setCursor(new Cursor(12));
        this.topicRefreshButton.addActionListener((e) -> {
            this.refreshTopicList();
        });
        this.topicRefreshButton.setCursor(new Cursor(12));
        this.refreshAvroClasses.addActionListener((e) -> {
            this.refreshAvroClassesList();
        });
        this.refreshAvroClasses.setCursor(new Cursor(12));
        this.avroClassComboBox.addItemListener((e) -> {
            if (e.getStateChange() == 1) {
                this.populateAvroSchema();
                this.kafkaToolProducerCache.setSelectedAvroClass(String.valueOf(this.avroClassComboBox.getSelectedItem()));
            }

        });
        this.refreshHistoryList();
        this.refreshTopicList();
        this.producerHistoryList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList list = (JList)mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 2) {
                    ProducerHistoryEntry producerHistoryEntry = (ProducerHistoryEntry)KafkaProducerComponent.this.kafkaToolPersistentStateComponent.getProducerHistoryDataStack().get(list.getSelectedIndex());
                    KafkaProducerComponent.this.selectedTopicField.setText(producerHistoryEntry.getTopic());
                    KafkaProducerComponent.this.serializerComboBox.setSelectedItem(producerHistoryEntry.getSerializerClass().getCanonicalName());
                    KafkaProducerComponent.this.schemaEditor.setText(producerHistoryEntry.getSchema());
                    KafkaProducerComponent.this.keyField.setText(producerHistoryEntry.getKey());
                    KafkaProducerComponent.this.payloadEditor.setText(producerHistoryEntry.getPayload());
                    KafkaProducerComponent.this.tabbedPane.setSelectedIndex(0);
                    KafkaProducerComponent.this.schemaEditor.setCaretPosition(0);
                    KafkaProducerComponent.this.payloadEditor.setCaretPosition(0);
                }

            }
        });
        this.refreshAvroClassesList();
        this.serializerComboBox.setSelectedItem(this.kafkaToolProducerCache.getSerializerKey());
        this.selectedTopicField.setText(this.kafkaToolProducerCache.getTopic());
        this.schemaEditor.setText(this.kafkaToolProducerCache.getSchema());
        this.schemaEditor.setCaretPosition(0);
        this.keyField.setText(this.kafkaToolProducerCache.getKey());
        this.payloadEditor.setText(this.kafkaToolProducerCache.getPayload());
        this.payloadEditor.setCaretPosition(0);
        this.avroClassComboBox.setSelectedItem(this.kafkaToolProducerCache.getSelectedAvroClass());
        this.avroClassComboBox.updateUI();
        ContentFactory contentFactory = SERVICE.getInstance();
        return contentFactory.createContent(this.tabbedPane, "Producer", false);
    }

    private boolean readyToProduce() {
        return !Strings.isNullOrEmpty(this.selectedTopicField.getText()) && !Strings.isNullOrEmpty(this.payloadEditor.getText()) && !Strings.isNullOrEmpty(this.schemaEditor.getText());
    }

    private void produceMessage() {
        if (!this.readyToProduce()) {
            Bus.notify(MissingRequiredFieldsNotification.getInstance());
        } else {
            String payloadString = this.payloadEditor.getText();
            Object message;
            if ("KafkaAvroSerializer".equals(this.serializerComboBox.getSelectedItem())) {
                String schemaString = this.schemaEditor.getText();

                try {
                    Opt<JsonNode> rawMessage = Opt.of(OBJECT_MAPPER.readTree(payloadString));
                    rawMessage.notPresent(() -> {
                        throw new KafkaToolProducerException("Could not parse raw message");
                    });
                    message = this.defaultJsonToAvroConverter.convert(((JsonNode)rawMessage.get()).toString(), schemaString);
                } catch (IOException var6) {
                    Bus.notify(FailedToAvrifyPayloadNotification.getInstance());
                    return;
                }
            } else {
                message = payloadString;
            }

            try {
                this.kafkaProducerProxy.send(this.selectedTopicField.getText(), this.keyField.getText(), message);
                this.addProducerHistoryEntry();
                this.cache();
                Bus.notify(SuccessfullyProducedMessageNotification.getInstance());
            } catch (KafkaToolProducerException var5) {
                Bus.notify(FailedToProduceMessageNotification.getInstance());
            }

        }
    }

    private void cache() {
        this.kafkaToolProducerCache.setSerializerKey(String.valueOf(this.serializerComboBox.getSelectedItem()));
        this.kafkaToolProducerCache.setTopic(this.selectedTopicField.getText());
        this.kafkaToolProducerCache.setSchema(this.schemaEditor.getText());
        this.kafkaToolProducerCache.setKey(this.keyField.getText());
        this.kafkaToolProducerCache.setPayload(this.payloadEditor.getText());
    }

    private void refreshTopicList() {
        this.topicComboBox.removeAllItems();
        Stream var10000 = this.confluentService.getTopicList().stream().filter(Objects::nonNull);
        JComboBox var10001 = this.topicComboBox;
        var10000.forEach(var10001::addItem);
    }

    private void refreshAvroClassesList() {
        this.avroClasses.clear();
        this.avroClassComboBox.removeAllItems();
        Set<Class> classesInPackage = this.avroService.loadAvroClassesFromProject(this.project, this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        Stream var10000 = classesInPackage.stream().peek((aClass) -> {
            Class stream = this.avroClasses.put(aClass.getName(), aClass);
        }).map(Class::getName).sorted();
        JComboBox var10001 = this.avroClassComboBox;
        var10000.forEach(var10001::addItem);
    }

    private void populateAvroSchema() {
        Class aClass = (Class)Objects.requireNonNull(this.avroClasses.get(String.valueOf(this.avroClassComboBox.getSelectedItem())));

        try {
            Field schemaField = aClass.getField("SCHEMA$");
            String schema = (new Parser()).parse(schemaField.get((Object)null).toString()).toString(true);
            this.schemaEditor.setText(schema);
            this.schemaEditor.setCaretPosition(0);
        } catch (IllegalAccessException | NoSuchFieldException var4) {
            throw new KafkaToolProducerException(var4);
        }
    }

    private void addProducerHistoryEntry() {
        ProducerHistoryEntry producerHistoryEntry = new ProducerHistoryEntry(UUID.randomUUID().toString(), this.selectedTopicField.getText(), this.kafkaToolSerializerService.getSerializerByKey(String.valueOf(this.serializerComboBox.getSelectedItem())), this.schemaEditor.getText(), this.keyField.getText(), this.payloadEditor.getText(), Instant.now().toString());
        this.kafkaToolPersistentStateComponent.addProducerHistoryEntry(producerHistoryEntry);
        this.refreshHistoryList();
    }

    private void refreshHistoryList() {
        this.historyListModel.removeAllElements();
        this.kafkaToolPersistentStateComponent.getProducerHistoryDataStack().forEach((producerHistoryEntry) -> {
            this.historyListModel.addElement(producerHistoryEntry.toString());
        });
        this.historyCountLabel.setText(String.format("%s items", this.kafkaToolPersistentStateComponent.getProducerHistoryDataStack().size()));
    }

    private /* synthetic */ void setupUI() {
        JButton jButton;
        JTextArea jTextArea;
        JLabel jLabel;
        JTextArea jTextArea2;
        JButton jButton2;
        JTabbedPane jTabbedPane;
        JButton jButton3;
        JTextField jTextField;
        JTextField jTextField2;
        this.tabbedPane = jTabbedPane = new JTabbedPane();
        JPanel jPanel = new JPanel();
        jPanel.setLayout((LayoutManager)new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("Produce", null, jPanel, null);
        JToolBar jToolBar = new JToolBar();
        jToolBar.setFloatable(false);
        jPanel.add((Component)jToolBar, (Object)new GridConstraints(0, 0, 1, 1, 0, 1, 6, 0, null, new Dimension(-1, 20), null));
        JComboBox jComboBox = new JComboBox();
        this.topicComboBox = jComboBox;
        jComboBox.setMaximumRowCount(1000);
        jComboBox.setName("Topic Selector");
        jComboBox.setPopupVisible(false);
        jComboBox.setToolTipText("Topic");
        Component component = jToolBar.add(jComboBox);
        this.topicRefreshButton = jButton2 = new JButton();
        jButton2.setIcon(new ImageIcon(this.getClass().getResource("/actions/refresh.png")));
        jButton2.setSelectedIcon(new ImageIcon(this.getClass().getResource("/actions/refresh.png")));
        jButton2.setText("");
        jButton2.setToolTipText("Refresh Topics");
        Component component2 = jToolBar.add(jButton2);
        this.selectedTopicField = jTextField = new JTextField();
        Component component3 = jToolBar.add(jTextField);
        JComboBox jComboBox2 = new JComboBox();
        this.serializerComboBox = jComboBox2;
        Component component4 = jToolBar.add(jComboBox2);
        JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setDividerLocation(400);
        jSplitPane.setOrientation(0);
        jPanel.add((Component)jSplitPane, (Object)new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, new Dimension(-1, 500), new Dimension(200, 500), null));
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout((LayoutManager)new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jPanel2.setEnabled(true);
        jPanel2.setVisible(true);
        jSplitPane.setLeftComponent(jPanel2);
        JToolBar jToolBar2 = new JToolBar();
        jToolBar2.setFloatable(false);
        jPanel2.add((Component)jToolBar2, (Object)new GridConstraints(0, 0, 1, 1, 0, 1, 6, 0, null, new Dimension(-1, 20), null));
        JComboBox jComboBox3 = new JComboBox();
        this.avroClassComboBox = jComboBox3;
        jComboBox3.setMaximumRowCount(1000);
        jComboBox3.setToolTipText(" Schema Name");
        Component component5 = jToolBar2.add(jComboBox3);
        this.refreshAvroClasses = jButton3 = new JButton();
        jButton3.setIcon(new ImageIcon(this.getClass().getResource("/actions/refresh.png")));
        jButton3.setText("");
        jButton3.setToolTipText("Refresh Avro Classes");
        Component component6 = jToolBar2.add(jButton3);
        JScrollPane jScrollPane = new JScrollPane();
        jPanel2.add((Component)jScrollPane, (Object)new GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, new Dimension(-1, 5), null));
        this.schemaEditor = jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);
        jTextArea.setMargin(new Insets(15, 15, 15, 15));
        jTextArea.setText("<raw schema>");
        jTextArea.setToolTipText("Raw Schema");
        jTextArea.setWrapStyleWord(false);
        jScrollPane.setViewportView(jTextArea);
        JPanel jPanel3 = new JPanel();
        jPanel3.setLayout((LayoutManager)new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jSplitPane.setRightComponent(jPanel3);
        this.keyField = jTextField2 = new JTextField();
        jTextField2.setName("Key");
        jTextField2.setText("<key>");
        jTextField2.setToolTipText("Key");
        jPanel3.add((Component)jTextField2, (Object)new GridConstraints(0, 0, 1, 1, 8, 1, 6, 0, null, new Dimension(150, -1), null));
        JScrollPane jScrollPane2 = new JScrollPane();
        jPanel3.add((Component)jScrollPane2, (Object)new GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        this.payloadEditor = jTextArea2 = new JTextArea();
        jTextArea2.setMargin(new Insets(15, 15, 15, 15));
        jTextArea2.setText("<payload>");
        jScrollPane2.setViewportView(jTextArea2);
        this.produceButton = jButton = new JButton();
        jButton.setBorderPainted(false);
        jButton.setContentAreaFilled(true);
        jButton.setIcon(new ImageIcon(this.getClass().getResource("/general/run@2x.png")));
        jButton.setLabel("");
        jButton.setMargin(new Insets(2, 8, 2, 8));
        jButton.setSelectedIcon(new ImageIcon(this.getClass().getResource("/general/run@2x.png")));
        jButton.setText("");
        jPanel3.add((Component)jButton, (Object)new GridConstraints(2, 0, 1, 1, 0, 3, 3, 3, null, null, null));
        JPanel jPanel4 = new JPanel();
        jPanel4.setLayout((LayoutManager)new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("History", null, jPanel4, null);
        JToolBar jToolBar3 = new JToolBar();
        jToolBar3.setFloatable(false);
        jPanel4.add((Component)jToolBar3, (Object)new GridConstraints(0, 0, 1, 1, 0, 1, 6, 0, null, new Dimension(-1, 20), null));
        this.historyCountLabel = jLabel = new JLabel();
        jLabel.setText("Label");
        Component component7 = jToolBar3.add(jLabel);
        JScrollPane jScrollPane3 = new JScrollPane();
        jPanel4.add((Component)jScrollPane3, (Object)new GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        JList jList = new JList();
        this.producerHistoryList = jList;
        jList.setSelectionMode(0);
        jScrollPane3.setViewportView(jList);
    }

    public /* synthetic */ JComponent $$$getRootComponent$$$() {
        return this.tabbedPane;
    }

    private static /* synthetic */ void reportNull(int n) {
        throw new IllegalArgumentException(String.format("Argument for @NotNull parameter '%s' of %s.%s must not be null", "project", "com/jetbrains/kafka/tool/ui/KafkaProducerComponent", "getContent"));
    }

}
