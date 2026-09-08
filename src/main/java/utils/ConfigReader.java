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
 *
 * <p>Property values that use the {@code ${VAR_NAME}} placeholder are resolved
 * in this priority order:
 * <ol>
 *   <li>OS / CI environment variable ({@link System#getenv()})</li>
 *   <li>Optional {@code .env} file in the working directory</li>
 *   <li>Empty string (so callers always receive a non-null value)</li>
 * </ol>
 *
 * <p>Sensitive credentials (github.username / github.password) MUST be
 * supplied via environment variables and must NEVER be hardcoded here or
 * in config.properties.
 *
 * Author: Arsath
 */
public class ConfigReader {

    private static final Properties PROPERTIES = loadProperties();
    private static final Map<String, String> DOTENV   = loadDotenv();

    private ConfigReader() {
        // Utility class — no instantiation
    }

    // ------------------------------------------------------------------ //
    //  Public API                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Returns the resolved value for {@code key}, or {@code defaultValue}
     * when the key is absent.
     */
    public static String getProperty(String key, String defaultValue) {
        String raw = PROPERTIES.getProperty(key, defaultValue);
        return resolveEnvPlaceholder(raw);
    }

    /**
     * Returns the resolved value for {@code key}.
     *
     * @throws IllegalArgumentException when the key is absent and no
     *                                  environment variable covers it.
     */
    public static String getProperty(String key) {
        String raw = PROPERTIES.getProperty(key);
        if (raw == null) {
            throw new IllegalArgumentException(
                "Required configuration key not found: " + key);
        }
        return resolveEnvPlaceholder(raw);
    }

    /** Convenience overload — delegates to {@link #getProperty(String, String)}. */
    public static String get(String key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load config.properties", e);
        }
        return props;
    }

    /**
     * Reads an optional {@code .env} file from the working directory.
     * Each non-comment, non-blank line must follow the {@code KEY=VALUE} format.
     * Values are NOT resolved for nested placeholders.
     */
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
                    int sep = trimmed.indexOf('=');
                    if (sep > 0) {
                        values.put(trimmed.substring(0, sep).trim(),
                                   trimmed.substring(sep + 1).trim());
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load .env file", e);
        }
        return values;
    }

    /**
     * If {@code value} matches the {@code ${VAR_NAME}} pattern, resolves it
     * against the environment then the .env map.  Returns the original value
     * unchanged when no placeholder is present.
     */
    private static String resolveEnvPlaceholder(String value) {
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            String varName = value.substring(2, value.length() - 1);
            // Priority 1 — real OS / CI environment variable
            String envValue = System.getenv(varName);
            if (envValue != null && !envValue.isEmpty()) {
                return envValue;
            }
            // Priority 2 — .env file
            String dotenvValue = DOTENV.get(varName);
            if (dotenvValue != null && !dotenvValue.isEmpty()) {
                return dotenvValue;
            }
            return "";
        }
        return value;
    }
}
