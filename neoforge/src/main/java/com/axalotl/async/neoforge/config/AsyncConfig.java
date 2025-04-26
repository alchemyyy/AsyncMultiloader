package com.axalotl.async.neoforge.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.axalotl.async.common.config.AsyncConfig.*;

public class AsyncConfig {
    public static final Logger LOGGER = LoggerFactory.getLogger("Async Config");
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.ConfigValue<Boolean> disabledv;
    private static final ModConfigSpec.ConfigValue<Integer> paraMaxv;
    private static final ModConfigSpec.ConfigValue<Boolean> enableEntityMoveSyncv;
    private static final ModConfigSpec.ConfigValue<List<String>> synchronizedEntitiesv;
    private static final ModConfigSpec.ConfigValue<Boolean> enableAsyncSpawnv;
    private static final ModConfigSpec.ConfigValue<Boolean> recoverFromErrorsv;

    static {
        BUILDER.push("Async Config");

        disabledv = BUILDER.comment("Globally disable all toggleable functionality within the async system. Set to true to stop all asynchronous operations.")
                .define("disabled", disabled);

        paraMaxv = BUILDER.comment("Maximum number of threads to use for parallel processing. Set to -1 to use default value.")
                .define("paraMax", paraMax);

        synchronizedEntitiesv = BUILDER.comment("Disables Item entity parallelization.")
                .define("synchronizedEntities", synchronizedEntities.stream().map(ResourceLocation::toString).toList());

        enableEntityMoveSyncv = BUILDER.comment("Modifies entity movement processing: true for synchronous movement (vanilla mechanics intact, less performance), false for asynchronous movement (better performance, may break mechanics).")
                .define("enableEntityMoveSync", enableEntityMoveSync);

        enableAsyncSpawnv = BUILDER.comment("Enables parallel processing of entity spawns. Warning, incompatible with VMP mod && Carpet mod lagFreeSpawning rule.")
                .define("enableAsyncSpawn", enableAsyncSpawn);

        recoverFromErrorsv = BUILDER.comment("Tries to recover from entity processing errors instead of crashing.")
                .define("recoverFromErrors", recoverFromErrors);

        BUILDER.pop();
        SPEC = BUILDER.build();
        LOGGER.info("Configuration successfully loaded.");
    }

    public static void loadConfig() {
        disabled = disabledv.get();
        paraMax = paraMaxv.get();
        enableAsyncSpawn = enableAsyncSpawnv.get();
        recoverFromErrors = recoverFromErrorsv.get();
        synchronizedEntities = new HashSet<>();
        SPEC.getSpec().<List<String>>getOptional("synchronizedEntities").ifPresentOrElse(ids -> {
            for (String id : ids) {
                ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
                if (resourceLocation != null) {
                    synchronizedEntities.add(resourceLocation);
                }
            }
        }, () -> synchronizedEntities = new HashSet<>(Set.of(
                Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "tnt")),
                Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "item")),
                Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "experience_orb"))
        )));
        enableEntityMoveSync = enableEntityMoveSyncv.get();
    }

    public static void saveConfig() {
        disabledv.set(disabled);
        paraMaxv.set(paraMax);
        enableAsyncSpawnv.set(enableAsyncSpawn);
        recoverFromErrorsv.set(recoverFromErrors);
        synchronizedEntitiesv.set(synchronizedEntities.stream().map(ResourceLocation::toString).toList());
        enableEntityMoveSyncv.set(enableEntityMoveSync);
        LOGGER.info("Configuration successfully saved.");
    }
}
