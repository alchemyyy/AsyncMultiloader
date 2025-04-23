package com.axalotl.async.fabric.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.axalotl.async.common.config.AsyncConfig.*;

public class AsyncConfig {
    public static final Logger LOGGER = LoggerFactory.getLogger("Async Config");

    private static final Supplier<CommentedFileConfig> configSupplier =
            () -> CommentedFileConfig.builder(FabricLoader.getInstance().getConfigDir().resolve("async.toml"))
                    .preserveInsertionOrder()
                    .sync()
                    .build();

    private static CommentedFileConfig CONFIG;

    public static void init() {
        LOGGER.info("Initializing Async Config...");
        CONFIG = configSupplier.get();
        try {
            if (!CONFIG.getFile().exists()) {
                LOGGER.warn("Configuration file not found, creating default configuration.");
                setDefaultValues();
                saveConfig();
            } else {
                CONFIG.load();
                loadConfigValues();
                saveConfig();
                LOGGER.info("Configuration successfully loaded.");
            }
        } catch (Throwable t) {
            LOGGER.error("Error loading configuration, resetting to default values.", t);
            setDefaultValues();
            saveConfig();
        }
    }

    public static void saveConfig() {
        CONFIG.set("disabled", disabled);
        CONFIG.setComment("disabled", "Globally disable all toggleable functionality within the async system. Set to true to stop all asynchronous operations.");

        CONFIG.set("paraMax", paraMax);
        CONFIG.setComment("paraMax", "Maximum number of threads to use for parallel processing. Set to -1 to use default value. Note: If 'virtualThreads' is enabled, this setting will be ignored.");

        CONFIG.set("enableEntityMoveSync", enableEntityMoveSync);
        CONFIG.setComment("enableEntityMoveSync", "Modifies entity movement processing: true for synchronous movement (vanilla mechanics intact, less performance), false for asynchronous movement (better performance, may break mechanics).");

        CONFIG.set("synchronizedEntities", synchronizedEntities.stream().map(ResourceLocation::toString).toList());
        CONFIG.setComment("synchronizedEntities", "List of entity class for sync processing.");

        CONFIG.set("enableAsyncSpawn", enableAsyncSpawn);
        CONFIG.setComment("enableAsyncSpawn", "Enables parallel processing of entity spawns. Warning, incompatible with VMP mod && Carpet mod lagFreeSpawning rule.");

        CONFIG.set("recoverFromErrors", recoverFromErrors);
        CONFIG.setComment("recoverFromErrors", "Tries to recover from entity processing errors instead of crashing.");

        CONFIG.save();
        LOGGER.info("Configuration saved successfully.");
    }

    private static void loadConfigValues() {
        Set<String> processedKeys = new HashSet<>(List.of("disabled", "paraMax", "enableEntityMoveSync", "synchronizedEntities"));

        disabled = CONFIG.getOrElse("disabled", false);
        paraMax = CONFIG.getOrElse("paraMax", -1);
        enableEntityMoveSync = CONFIG.getOrElse("enableEntityMoveSync", false);
        enableAsyncSpawn = CONFIG.getOrElse("enableAsyncSpawn", false);
        recoverFromErrors = CONFIG.getOrElse("recoverFromErrors", false);

        synchronizedEntities = new HashSet<>();
        CONFIG.<List<String>>getOptional("synchronizedEntities").ifPresentOrElse(ids -> {
            for (String id : ids) {
                ResourceLocation identifier = ResourceLocation.tryParse(id);
                if (identifier != null) {
                    synchronizedEntities.add(identifier);
                }
            }
        }, () -> synchronizedEntities = new HashSet<>(Set.of(
                ResourceLocation.withDefaultNamespace("tnt"),
                ResourceLocation.withDefaultNamespace("item"),
                ResourceLocation.withDefaultNamespace("experience_orb"))));

        Set<String> keysToRemove = new HashSet<>();
        for (CommentedConfig.Entry entry : CONFIG.entrySet()) {
            String key = entry.getKey();
            if (!processedKeys.contains(key)) {
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            LOGGER.warn("Removing unused configuration key: {}", key);
            CONFIG.remove(key);
        }

        CONFIG.save();
    }

    private static void setDefaultValues() {
        disabled = false;
        paraMax = -1;
        enableEntityMoveSync = false;
        enableAsyncSpawn = false;
        synchronizedEntities = new HashSet<>(Set.of(
                ResourceLocation.withDefaultNamespace("tnt"),
                ResourceLocation.withDefaultNamespace("item"),
                ResourceLocation.withDefaultNamespace("experience_orb")
        ));
    }
}