package com.jetbrains.kafka.tool.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.jetbrains.kafka.tool.KafkaToolModule;
import org.jetbrains.annotations.NotNull;

public class KafkaToolToolWindow implements ToolWindowFactory, DumbAware {
    public KafkaToolToolWindow() {
    }

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        if (project == null) {
            this.reportNull(0);
        }

        if (toolWindow == null) {
            this.reportNull(1);
        }

        KafkaProducerComponent kafkaProducerComponent = KafkaToolModule.getInstance(KafkaProducerComponent.class);
        toolWindow.getContentManager().addContent(kafkaProducerComponent.getContent(project));
        KafkaSettingsComponent kafkaSettingsComponent = KafkaToolModule.getInstance(KafkaSettingsComponent.class);
        toolWindow.getContentManager().addContent(kafkaSettingsComponent.getContent(project));
    }

    private static void reportNull(int n) {
        Object[] arrobject;
        Object[] arrobject2 = new Object[3];
        switch (n) {
            default: {
                arrobject = arrobject2;
                arrobject2[0] = "project";
                break;
            }
            case 1: {
                arrobject = arrobject2;
                arrobject2[0] = "toolWindow";
                break;
            }
        }
        arrobject[1] = "com/jetbrains/kafka/tool/ui/KafkaToolToolWindow";
        arrobject[2] = "createToolWindowContent";
        throw new IllegalArgumentException(String.format("Argument for @NotNull parameter '%s' of %s.%s must not be null", arrobject));
    }
}
