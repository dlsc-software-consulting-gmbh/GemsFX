package com.dlsc.gemsfx.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResourceBundleManagerTest {

    private static final Pattern REQUIRED_KEY_LOOKUP_PATTERN = Pattern.compile(
            "ResourceBundleManager\\.getString\\(\\s*ResourceBundleManager\\.BundleType\\.([A-Z_]+)\\s*,\\s*\"([^\"]+)\"\\s*\\)");

    private Locale originalLocale;

    @Before
    public void setUp() {
        originalLocale = ResourceBundleManager.getLocale();
    }

    @After
    public void tearDown() {
        ResourceBundleManager.setLocale(originalLocale);
    }

    @Test
    public void missingKey_usesKeyAsDefaultFallback() {
        String value = ResourceBundleManager.getString("test-i18n", "missing.key");
        assertEquals("missing.key", value);
    }

    @Test
    public void missingKey_usesProvidedFallback() {
        String value = ResourceBundleManager.getString("test-i18n", "missing.key", "fallback value");
        assertEquals("fallback value", value);
    }

    @Test
    public void missingBundle_usesProvidedFallback() {
        String value = ResourceBundleManager.getString("missing-test-i18n", "greeting", "fallback value");
        assertEquals("fallback value", value);
    }

    @Test
    public void localeSwitch_loadsBundleForNewLocale() {
        ResourceBundleManager.setLocale(Locale.ENGLISH);
        assertEquals("hello-default", ResourceBundleManager.getString("test-i18n", "greeting"));

        ResourceBundleManager.setLocale(Locale.FRENCH);
        assertEquals("bonjour", ResourceBundleManager.getString("test-i18n", "greeting"));
    }

    @Test
    public void bundleTypeLookupAndFormat_areSupported() {
        ResourceBundleManager.setLocale(Locale.ENGLISH);

        assertEquals("fallback", ResourceBundleManager.BundleType.DURATION_PICKER.getString("missing.key", "fallback"));
        assertEquals("Hello Duke", ResourceBundleManager.format("test-i18n", "hello.pattern", "Duke"));
    }

    @Test
    public void requiredKeysWithoutFallback_existInBundleDomains() throws IOException {
        ResourceBundleManager.setLocale(Locale.ENGLISH);
        Map<ResourceBundleManager.BundleType, Set<String>> requiredKeysByBundleType = collectRequiredKeysWithoutFallback();

        for (Map.Entry<ResourceBundleManager.BundleType, Set<String>> entry : requiredKeysByBundleType.entrySet()) {
            ResourceBundle bundle = ResourceBundleManager.getBundle(entry.getKey());
            for (String key : entry.getValue()) {
                assertTrue("Missing required key '" + key + "' in bundle '" + entry.getKey().getBaseName() + "'",
                        bundle.containsKey(key));
            }
        }
    }

    @Test
    public void localizedBundles_areUsedWhenAvailable() {
        ResourceBundleManager.setLocale(Locale.GERMAN);
        assertEquals(":", ResourceBundleManager.getString(ResourceBundleManager.BundleType.DURATION_PICKER, "format.separator.time"));

        ResourceBundleManager.setLocale(Locale.CHINESE);
        String noNotifications = ResourceBundleManager.getString(ResourceBundleManager.BundleType.INFO_CENTER_VIEW, "placeholder.no-notifications");
        String showAll = ResourceBundleManager.getString(ResourceBundleManager.BundleType.INFO_CENTER_VIEW, "group.header.show.all");

        assertTrue(!"No notifications".equals(noNotifications) && !noNotifications.isBlank());
        assertTrue(!"Show All".equals(showAll) && !showAll.isBlank());
    }

    @Test
    public void format_usesLocaleSpecificLookupPattern() {
        ResourceBundleManager.setLocale(Locale.ENGLISH);
        assertEquals("Hello Duke", ResourceBundleManager.format("test-i18n", "hello.pattern", "Duke"));

        ResourceBundleManager.setLocale(Locale.FRENCH);
        assertEquals("Bonjour Duke", ResourceBundleManager.format("test-i18n", "hello.pattern", "Duke"));
    }

    private Map<ResourceBundleManager.BundleType, Set<String>> collectRequiredKeysWithoutFallback() throws IOException {
        Map<ResourceBundleManager.BundleType, Set<String>> required = new TreeMap<>();
        Path sourceRoot = resolveSourceRoot();

        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            stream.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String source = Files.readString(path);
                    Matcher matcher = REQUIRED_KEY_LOOKUP_PATTERN.matcher(source);
                    while (matcher.find()) {
                        ResourceBundleManager.BundleType bundleType = ResourceBundleManager.BundleType.valueOf(matcher.group(1));
                        String key = matcher.group(2);
                        required.computeIfAbsent(bundleType, ignored -> new TreeSet<>()).add(key);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to read source file: " + path, ex);
                }
            });
        }

        return required;
    }

    private Path resolveSourceRoot() {
        Path modulePath = Path.of("src/main/java/com/dlsc/gemsfx");
        if (Files.exists(modulePath)) {
            return modulePath;
        }

        Path repositoryPath = Path.of("gemsfx/src/main/java/com/dlsc/gemsfx");
        if (Files.exists(repositoryPath)) {
            return repositoryPath;
        }

        throw new IllegalStateException("Could not resolve source root for i18n key validation tests.");
    }
}
