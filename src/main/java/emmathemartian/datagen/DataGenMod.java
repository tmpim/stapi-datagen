package emmathemartian.datagen;

import emmathemartian.datagen.entrypoint.DataEntrypoint;
import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.mine_diver.unsafeevents.listener.ListenerPriority;
import net.modificationstation.stationapi.api.event.registry.DimensionRegistryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DataGenMod {
    public static final Logger LOGGER = LoggerFactory.getLogger("datagen");

    public static Path targetPath;

    private static Path getTargetPath() {
        String strPath = Objects.requireNonNull(System.getProperty("datagen.path"), "System property datagen.path was null. Make sure it is defined in your buildscript.");
        return Path.of(strPath);
    }

    @SuppressWarnings("unused")
    @EventListener(priority = ListenerPriority.LOWEST)
    public static void onPostRegistries(DimensionRegistryEvent event) {
        String property = System.getProperty("datagen.run");

        var toRun = property != null ? property.split(",") : new String[]{};
        var toRunSet = Set.of(toRun);
        if (!toRunSet.isEmpty()) {
            LOGGER.info("Data generator will be executed for mods: [{}]", String.join(", ", toRun));
        } else {
            LOGGER.info("Data generator will be executed for all valid entrypoints in the classpath");
        }

        targetPath = getTargetPath();
        LOGGER.info("Data generation target path is `{}`", targetPath);
        LOGGER.info("Note: The game will be stopped after data generation finishes.");

        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOGGER.error("Data generators should not be executed in production environments. Exiting.");
            System.exit(1);
        }

        var entrypoints = FabricLoader.getInstance()
            .getEntrypointContainers("data", DataEntrypoint.class)
            .stream()
            .filter(c -> toRunSet.isEmpty() || toRunSet.contains(c.getProvider().getMetadata().getId()))
            .collect(Collectors.toSet());

        if (entrypoints.isEmpty()) {
            LOGGER.info("No data generators found! Exiting.");
        } else {
            entrypoints.forEach(c -> c.getEntrypoint().run());
            LOGGER.info("Data generation finished, closing game.");
        }

        System.exit(0);
    }
}
