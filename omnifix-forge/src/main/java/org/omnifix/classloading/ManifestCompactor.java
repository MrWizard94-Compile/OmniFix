package org.omnifix.classloading;

import com.mojang.logging.LogUtils;
import cpw.mods.jarhandling.impl.Jar;
import net.minecraftforge.fml.loading.LoadingModList;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;

/**
 * Drops per-entry digest-only manifest attributes from mod SecureJars. After verification those
 * digests are never consulted again but still pin large string maps for the JVM lifetime.
 */
public final class ManifestCompactor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ManifestCompactor() {}

    public static void compactManifests() {
        int compacted = 0;
        for (var mfi : LoadingModList.get().getModFiles()) {
            if (!(mfi.getFile().getSecureJar() instanceof Jar jar)) {
                continue;
            }
            var manifest = jar.getManifest();
            if (manifest == null) {
                continue;
            }
            var entries = manifest.getEntries();
            var entryKeys = new HashSet<>(entries.keySet());
            var digests = Set.of(
                    new Attributes.Name("SHA-256-Digest"),
                    new Attributes.Name("SHA-384-Digest"));
            boolean changed = false;
            for (String key : entryKeys) {
                Attributes attrs = entries.get(key);
                if (attrs == null) {
                    continue;
                }
                boolean keep = attrs.keySet().stream().anyMatch(n -> n != null && !digests.contains(n));
                if (!keep) {
                    entries.remove(key);
                    changed = true;
                }
            }
            if (changed) {
                compacted++;
            }
        }
        if (compacted > 0) {
            LOGGER.info("[OmniFix] Compacted digest-only manifest entries in {} jar(s)", compacted);
        }
    }
}
