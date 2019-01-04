package com.jetbrains.kafka.tool.service;

import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.util.Set;

@FunctionalInterface
public interface AvroClassScanner {
    Set<Class> loadAvroClassesFromProject(Project var1, String var2) throws IOException;
}