package utils;

import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * FakerDataFactory — generates unique, dynamic test data at runtime using
 * Java Faker so that tests never collide on shared GitHub resources.
 *
 * <p>All methods are static; the underlying {@link Faker} instance is shared
 * and initialised once for the JVM lifetime.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * String repoName   = FakerDataFactory.getRandomRepoName();
 * String issueTitle = FakerDataFactory.getRandomIssueTitle();
 * String gistDesc   = FakerDataFactory.getRandomGistDescription();
 * }</pre>
 *
 * Author: Arsath
 */
public class FakerDataFactory {

    private static final Faker FAKER = new Faker(Locale.ENGLISH);

    private FakerDataFactory() {
        // Utility class — no instantiation
    }

    // ------------------------------------------------------------------ //
    //  Repository                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Returns a lowercase slug suitable for a GitHub repository name,
     * e.g. {@code repo-test-8492}.
     */
    public static String getRandomRepoName() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 99999);
        return "repo-test-" + suffix;
    }

    /**
     * Returns a readable, hyphenated repository name using Faker words,
     * e.g. {@code agile-monkey-framework}.
     */
    public static String getRandomRepoNameVerbose() {
        return FAKER.hacker().adjective().toLowerCase().replace(" ", "-")
            + "-" + FAKER.hacker().noun().toLowerCase().replace(" ", "-")
            + "-" + FAKER.hacker().verb().toLowerCase().replace(" ", "-");
    }

    /**
     * Returns a short, plain-text repository description.
     */
    public static String getRandomRepoDescription() {
        return "Automated test repo — " + FAKER.lorem().sentence(5);
    }

    // ------------------------------------------------------------------ //
    //  Issues                                                              //
    // ------------------------------------------------------------------ //

    /**
     * Returns a realistic-looking GitHub issue title,
     * e.g. {@code [Bug] Cache invalidation fails on retry}.
     */
    public static String getRandomIssueTitle() {
        String[] prefixes = {"[Bug]", "[Feature]", "[Docs]", "[Refactor]", "[Question]"};
        String prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.length)];
        return prefix + " " + FAKER.hacker().ingverb() + " "
            + FAKER.hacker().noun() + " "
            + FAKER.hacker().verb() + "s on "
            + FAKER.hacker().adjective() + " input";
    }

    /**
     * Returns a multi-sentence issue body / description.
     */
    public static String getRandomIssueBody() {
        return "**Steps to reproduce**\n1. " + FAKER.lorem().sentence()
            + "\n\n**Expected:** " + FAKER.lorem().sentence()
            + "\n**Actual:** " + FAKER.lorem().sentence();
    }

    // ------------------------------------------------------------------ //
    //  Gists                                                               //
    // ------------------------------------------------------------------ //

    /**
     * Returns a short gist description sentence,
     * e.g. {@code Utility snippet for parsing JSON responses}.
     */
    public static String getRandomGistDescription() {
        return "Utility snippet for "
            + FAKER.hacker().ingverb() + " "
            + FAKER.hacker().noun() + " "
            + FAKER.hacker().noun() + "s";
    }

    /**
     * Returns a gist filename with a {@code .txt} extension.
     */
    public static String getRandomGistFileName() {
        return FAKER.hacker().noun().toLowerCase().replace(" ", "_")
            + "_" + ThreadLocalRandom.current().nextInt(100, 9999) + ".txt";
    }

    // ------------------------------------------------------------------ //
    //  Search / misc                                                       //
    // ------------------------------------------------------------------ //

    /**
     * Returns a random programming-domain search term.
     */
    public static String getRandomSearchQuery() {
        return FAKER.hacker().adjective() + " " + FAKER.hacker().noun();
    }
}
