package org.omnifix.kernel.feature;

import org.omnifix.kernel.StackDomain;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A single, toggleable OmniFix fix. FeatureUnits are the atomic unit of the research backlog
 * ({@code RESEARCH_MASTER.md}): gated by optional mod domains and a config flag.
 */
public final class FeatureUnit {

    private final String id;
    private final String displayName;
    private final String description;
    private final boolean defaultEnabled;
    private final Set<StackDomain> requiredDomains;

    public FeatureUnit(String id, String displayName, String description, boolean defaultEnabled,
                       StackDomain... requiredDomains) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.description = Objects.requireNonNull(description, "description");
        this.defaultEnabled = defaultEnabled;
        if (requiredDomains == null || requiredDomains.length == 0) {
            this.requiredDomains = Collections.emptySet();
        } else {
            this.requiredDomains = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(requiredDomains)));
        }
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public boolean defaultEnabled() {
        return defaultEnabled;
    }

    /** Empty = always eligible from a domain perspective (vanilla/Forge fixes). */
    public Set<StackDomain> requiredDomains() {
        return requiredDomains;
    }

    @Override
    public String toString() {
        return id;
    }
}
