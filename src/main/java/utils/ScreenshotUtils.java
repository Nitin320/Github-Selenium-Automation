package utils;

import driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * ScreenshotUtils — captures and stores screenshots on test failure.
 * Author: Sulthan
 */
public class ScreenshotUtils {

    private ScreenshotUtils() {
        // Utility class — no instantiation
    }

    public static void capture(String name) {
        WebDriver driver = DriverManager.getDriver();
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return;
        }

        Path outputDirectory = Path.of("target", "screenshots");
        try {
            Files.createDirectories(outputDirectory);
            Path target = outputDirectory.resolve(sanitizeFileName(name) + ".png");
            Files.copy(
                    screenshotDriver.getScreenshotAs(OutputType.FILE).toPath(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save screenshot", e);
        }
    }

    private static String sanitizeFileName(String name) {
        return name == null || name.isBlank() ? "screenshot" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
