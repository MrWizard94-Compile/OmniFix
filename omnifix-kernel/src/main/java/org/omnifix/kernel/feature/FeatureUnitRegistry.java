package org.omnifix.kernel.feature;

import org.omnifix.kernel.StackDomain;
import org.omnifix.kernel.StackPolicyEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * Registry of FeatureUnits plus early-loadable config overrides.
 *
 * <p>Config is a simple properties file so it can be read during Mixin plugin load
 * (before Forge config systems are ready). Keys are FeatureUnit ids; values are
 * {@code true}/{@code false}. Missing keys use the unit's default.
 */
public final class FeatureUnitRegistry {

    private static final Map<String, FeatureUnit> UNITS = new LinkedHashMap<>();
    private static final Map<String, Boolean> OVERRIDES = new LinkedHashMap<>();
    private static volatile boolean configLoaded;
    private static Path lastConfigPath;

    private FeatureUnitRegistry() {}

    public static synchronized void register(FeatureUnit unit) {
        UNITS.put(unit.id(), unit);
    }

    public static FeatureUnit get(String id) {
        return UNITS.get(id);
    }

    public static Collection<FeatureUnit> all() {
        return Collections.unmodifiableCollection(UNITS.values());
    }

    /**
     * Load (or create) {@code omnifix-features.properties}. Safe to call from the Mixin plugin
     * and again from the mod constructor.
     */
    public static synchronized void loadConfig(Path configPath) {
        lastConfigPath = configPath;
        OVERRIDES.clear();
        try {
            if (Files.notExists(configPath)) {
                writeDefaultConfig(configPath);
            }
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
            }
            for (String name : props.stringPropertyNames()) {
                if (name.startsWith("#") || name.isBlank()) {
                    continue;
                }
                String raw = props.getProperty(name);
                if (raw == null) {
                    continue;
                }
                OVERRIDES.put(name.trim(), Boolean.parseBoolean(raw.trim()));
            }
            // Keep the file in sync when new units are registered after first write.
            writeDefaultConfig(configPath);
            configLoaded = true;
        } catch (IOException e) {
            configLoaded = true; // do not retry in a tight loop; defaults apply
        }
    }

    /** Ensures config has been attempted when a game config directory is already known. */
    public static void ensureConfigLoaded(Path configPath) {
        if (!configLoaded || (lastConfigPath != null && !lastConfigPath.equals(configPath))) {
            loadConfig(configPath);
        }
    }

    public static boolean isConfigEnabled(String id) {
        Boolean override = OVERRIDES.get(id);
        if (override != null) {
            return override;
        }
        FeatureUnit unit = UNITS.get(id);
        return unit == null || unit.defaultEnabled();
    }

    /**
     * Config enabled and every required {@link StackDomain} active via {@link StackPolicyEngine}.
     * Use after domain probes have run (mod constructor / later).
     */
    public static boolean isActive(String id) {
        return isActive(id, StackPolicyEngine::isDomainActive);
    }

    public static boolean isActive(String id, Predicate<StackDomain> domainActive) {
        if (!isConfigEnabled(id)) {
            return false;
        }
        FeatureUnit unit = UNITS.get(id);
        if (unit == null) {
            return false;
        }
        for (StackDomain domain : unit.requiredDomains()) {
            if (!domainActive.test(domain)) {
                return false;
            }
        }
        return true;
    }

    private static void writeDefaultConfig(Path configPath) throws IOException {
        Files.createDirectories(configPath.getParent());
        Properties props = new Properties();
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
            }
        }
        for (FeatureUnit unit : UNITS.values()) {
            if (!props.containsKey(unit.id())) {
                props.setProperty(unit.id(), Boolean.toString(unit.defaultEnabled()));
            }
        }
        try (OutputStream out = Files.newOutputStream(configPath)) {
            props.store(out, "OmniFix FeatureUnit toggles — set false to disable a fix. See RESEARCH_MASTER.md. Restart after edits.");
        }
    }
}
