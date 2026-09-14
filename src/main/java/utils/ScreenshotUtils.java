package utils;

import driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtils — captures and stores screenshots during and on test failure.
 *
 * <p>Each screenshot is written to {@code target/screenshots/} with a timestamped
 * filename so that screenshots from consecutive runs never overwrite each other.
 *
 * Filename format: {@code <sanitized-name>_<yyyyMMdd_HHmmss_SSS>.png}
 *
 * Author: Sulthan
 */
public class ScreenshotUtils {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {
        // Utility class — no instantiation
    }

    /**
     * Captures the current browser viewport and writes it to
     * {@code target/screenshots/<name>_<timestamp>.png}.
     *
     * @param name logical name for the screenshot (scenario name, step label, etc.)
     * @return the absolute path of the saved file, or {@code null} if the driver
     *         does not support screenshots
     */
    public static Path capture(String name) {
        WebDriver driver = DriverManager.getDriver();
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return null;
        }

        Path outputDirectory = Path.of("target", "screenshots");
        try {
            Files.createDirectories(outputDirectory);
            String timestamp = LocalDateTime.now().format(TS_FMT);
            String fileName  = sanitizeFileName(name) + "_" + timestamp + ".png";
            Path   target    = outputDirectory.resolve(fileName);
            Files.copy(
                    screenshotDriver.getScreenshotAs(OutputType.FILE).toPath(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save screenshot: " + e.getMessage(), e);
        }
    }

    private static String sanitizeFileName(String name) {
        return (name == null || name.isBlank())
                ? "screenshot"
                : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
