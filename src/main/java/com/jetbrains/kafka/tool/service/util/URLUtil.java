package com.jetbrains.kafka.tool.service.util;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtil {
    public URLUtil() {
    }

    public static URL get(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException var2) {
            throw new RuntimeException(var2);
        }
    }
}
