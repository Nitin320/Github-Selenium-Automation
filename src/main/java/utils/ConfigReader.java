package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ConfigReader — loads properties from config.properties on the classpath.
 * Author: Arsath
 */
public class ConfigReader {

    private static final Properties PROPERTIES = loadProperties();
    private static final Map<String, String> DOTENV = loadDotenv();

    private ConfigReader() {
        // Utility class — no instantiation
    }

    public static String get(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key, defaultValue);
        return resolveEnvironmentVariable(value);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load config.properties", e);
        }
        return properties;
    }

    private static Map<String, String> loadDotenv() {
        Map<String, String> values = new HashMap<>();
        Path dotenv = Path.of(".env");
        if (!Files.exists(dotenv)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(dotenv)) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    int separator = trimmed.indexOf('=');
                    if (separator > 0) {
                        values.put(trimmed.substring(0, separator).trim(),
                                trimmed.substring(separator + 1).trim());
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load .env", e);
        }
        return values;
    }

    private static String resolveEnvironmentVariable(String value) {
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            String variableName = value.substring(2, value.length() - 1);
            return System.getenv().getOrDefault(variableName, DOTENV.getOrDefault(variableName, ""));
        }
        return value;
    }
}
