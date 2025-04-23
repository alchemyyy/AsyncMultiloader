package com.axalotl.async.common.config;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.axalotl.async.common.platform.PlatformEventBus.saveConfig;

public class AsyncConfig {
    public static final Logger LOGGER = LoggerFactory.getLogger("Async Config");

    public static boolean disabled = false;
    public static int paraMax = -1;
    public static boolean enableEntityMoveSync = false;
    public static boolean enableAsyncSpawn = false;
    public static boolean recoverFromErrors = false;
    public static Set<ResourceLocation> synchronizedEntities = new HashSet<>(Set.of(
            Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "tnt")),
            Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "item")),
            Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "experience_orb"))
    ));

    public static int getParallelism() {
        if (paraMax <= 0) return Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), paraMax));
    }

    public static void syncEntity(ResourceLocation entityId) {
        if (synchronizedEntities.add(entityId)) {
            saveConfig();
            LOGGER.info("Sync entity class: {}", entityId);
        } else {
            LOGGER.warn("Entity class already synchronized: {}", entityId);
        }
    }

    public static void asyncEntity(ResourceLocation entityId) {
        if (synchronizedEntities.remove(entityId)) {
            saveConfig();
            LOGGER.info("Enable async process entity class: {}", entityId);
        } else {
            LOGGER.warn("Entity class not found: {}", entityId);
        }
    }
}
