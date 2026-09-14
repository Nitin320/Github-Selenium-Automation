package session;

import driver.DriverManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.LoginPage;
import utils.ConfigReader;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SessionManager — eliminates the per-scenario login/logout cost by
 * capturing GitHub session cookies after the first login and replanting
 * them into every subsequent driver instance.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>On the first scenario that calls {@link #ensureLoggedIn()}, a real
 *       username/password login is performed and the resulting cookies are
 *       copied into {@code SAVED_COOKIES}.</li>
 *   <li>For every subsequent scenario, the driver navigates to github.com,
 *       injects the saved cookies, and refreshes the page — skipping the
 *       entire login form interaction entirely.</li>
 *   <li>If the cookie restore fails (session expired, GitHub invalidated it),
 *       the manager transparently falls back to a fresh password login and
 *       refreshes the cookie store.</li>
 * </ol>
 *
 * <h3>Time savings</h3>
 * A typical GitHub login + wait cycle takes 8–20 seconds per scenario.
 * Cookie restore completes in under 2 seconds (one navigation + one refresh).
 *
 * <h3>Thread safety</h3>
 * {@code SAVED_COOKIES} is protected by a {@link ReentrantLock} so parallel
 * scenarios sharing the same JVM (e.g. when parallelism is re-enabled) do not
 * race on the initial login.
 *
 * Author: Group 5 — Session optimisation
 */
public class SessionManager {

    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

    /** Cookie names GitHub uses to identify an authenticated session. */
    private static final String SESSION_COOKIE      = "user_session";
    private static final String SESSION_COOKIE_ALT  = "_gh_sess";
    private static final String DOTCOM_USER_COOKIE  = "dotcom_user";

    /** Shared cookie store — populated once, reused by all scenarios. */
    private static final Set<Cookie> SAVED_COOKIES = new HashSet<>();

    /** Guards first-login initialisation in a parallel context. */
    private static final ReentrantLock LOCK = new ReentrantLock();

    /** Whether a valid session has been cached. */
    private static volatile boolean sessionCached = false;

    private SessionManager() {
        // Utility class — no instantiation
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Ensures the current driver has an active GitHub session.
     *
     * <p>This is the only method step definitions / Hooks need to call.
     * It will either restore cookies or perform a fresh login, whichever is
     * appropriate for the current state.
     *
     * @return {@code true} if the session is active after this call
     */
    public static boolean ensureLoggedIn() {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            LOG.error("  ❌  ensureLoggedIn() called before a driver was set in DriverManager");
            return false;
        }

        // Fast path — try cookie restore first (avoids the lock entirely once warmed up)
        if (sessionCached && tryRestoreSession(driver)) {
            LOG.info("  🍪  Session restored from cookie cache — login skipped");
            return true;
        }

        // Slow path — acquire lock so only one thread does the actual login
        LOCK.lock();
        try {
            // Double-check after acquiring the lock (another thread may have logged in)
            if (sessionCached && tryRestoreSession(driver)) {
                LOG.info("  🍪  Session restored after lock — login skipped");
                return true;
            }
            // Perform a real login and cache the cookies
            return performLoginAndCache(driver);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Clears the cached session so the next call to {@link #ensureLoggedIn()}
     * performs a fresh login.  Call this if tests deliberately sign out.
     */
    public static void invalidateSession() {
        LOCK.lock();
        try {
            SAVED_COOKIES.clear();
            sessionCached = false;
            LOG.info("  🗑️  Session cache invalidated — next scenario will do a fresh login");
        } finally {
            LOCK.unlock();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Plants the saved cookies into the driver, navigates to GitHub, and
     * verifies that the session is active by checking for the account menu.
     */
    private static boolean tryRestoreSession(WebDriver driver) {
        if (SAVED_COOKIES.isEmpty()) {
            return false;
        }
        try {
            String baseUrl = ConfigReader.getProperty("base.url", "https://github.com");

            // Cookies can only be set on the cookie's domain — navigate there first
            driver.get(baseUrl);

            // Delete any stale cookies and plant the saved set
            driver.manage().deleteAllCookies();
            for (Cookie c : SAVED_COOKIES) {
                try {
                    driver.manage().addCookie(c);
                } catch (Exception ignored) {
                    // Individual cookie failures are non-fatal; skip and continue
                }
            }

            // Refresh so GitHub processes the injected session cookies
            driver.navigate().refresh();

            // Verify the session is live
            LoginPage probe = new LoginPage();
            boolean isLoggedIn = probe.isLoggedIn();
            if (!isLoggedIn) {
                LOG.warn("  ⚠️  Cookie restore did not produce a live session — will re-login");
                sessionCached = false;
            }
            return isLoggedIn;
        } catch (Exception e) {
            LOG.warn("  ⚠️  Cookie restore threw an exception: {} — will re-login", e.getMessage());
            sessionCached = false;
            return false;
        }
    }

    /**
     * Performs a real username/password login, then captures all cookies
     * that belong to github.com for future reuse.
     */
    private static boolean performLoginAndCache(WebDriver driver) {
        LOG.info("  🔐  Performing real login to cache session cookies…");
        try {
            LoginPage loginPage = new LoginPage().open();
            loginPage.loginFromConfigAndWaitForLogin();

            if (!loginPage.isLoggedIn()) {
                LOG.error("  ❌  Login failed — cannot cache session");
                return false;
            }

            // Capture all cookies now that the session is live
            LOCK.lock();
            try {
                SAVED_COOKIES.clear();
                SAVED_COOKIES.addAll(driver.manage().getCookies());
                sessionCached = true;
            } finally {
                LOCK.unlock();
            }

            LOG.info("  ✅  Login successful — {} cookies cached for session reuse",
                     SAVED_COOKIES.size());
            return true;
        } catch (Exception e) {
            LOG.error("  ❌  Login threw an exception: {}", e.getMessage());
            return false;
        }
    }
}
