package com.jetbrains.kafka.tool.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

import com.jetbrains.kafka.tool.exception.KafkaToolProducerException;
import com.jetbrains.kafka.tool.service.util.URLUtil;
import org.apache.avro.specific.SpecificRecordBase;

public class AvroClassScannerImpl implements AvroClassScanner {
    private static final String JAR_URL_PREFIX = "jar:";
    private static final String FILE_URL_PREFIX = "file:";
    private static final String DOLLAR_SIGN_CHARACTER = "$";
    private static final String ANNOYING_CHARACTERS = "!/";
    private static final String EMPTY_STRING = "";
    private static final String MAIN_SUFFIX = "_main";

    public AvroClassScannerImpl() {
    }

    public Set<Class> loadAvroClassesFromProject(Project project, String packageName) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(packageName);
        Set<Class> output = new HashSet<>();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Arrays.stream(modules).filter((module) -> {
            return module.getName().contains("_main");
        }).forEach((module) -> {
            output.addAll(this.getAvroClassesFromModule(module, packageName));
        });
        return output;
    }

    private Collection<? extends Class> getAvroClassesFromModule(Module module, String packageName) {
        ModuleRootManager instance = ModuleRootManager.getInstance(module);
        List<String> entries = (List)Arrays.stream(instance.getOrderEntries()).map((orderEntry) -> {
            return Arrays.asList(orderEntry.getUrls(OrderRootType.CLASSES));
        }).flatMap(Collection::stream).collect(Collectors.toList());
        URL[] urls = (URL[])entries.stream().map((url) -> {
            return url.replace("jar:", "file:").replace("!/", "");
        }).map(URLUtil::get).toArray((x$0) -> {
            return new URL[x$0];
        });
        URLClassLoader classLoader = new URLClassLoader(urls);

        try {
            ClassPath classPath = ClassPath.from(classLoader);
            ImmutableSet<ClassInfo> topLevelClasses = classPath.getAllClasses();
            return (Collection)topLevelClasses.stream().filter(Objects::nonNull).filter((classInfo) -> {
                return classInfo.getName().contains(packageName);
            }).filter((classInfo) -> {
                return !classInfo.getName().contains("$");
            }).map(ClassInfo::load).filter((aClass) -> {
                return Objects.nonNull(aClass.getSuperclass());
            }).filter((aClass) -> {
                return aClass.getSuperclass().getCanonicalName().equals(SpecificRecordBase.class.getCanonicalName());
            }).collect(Collectors.toList());
        } catch (IOException var9) {
            throw new KafkaToolProducerException(var9);
        }
    }
}

