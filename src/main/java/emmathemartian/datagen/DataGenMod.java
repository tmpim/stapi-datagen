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
        if (property == null)
            return;

        final String[] toRun = property.split(",");
        LOGGER.info("Data generator will be executed for mods: [{}]", String.join(", ", toRun));
        LOGGER.info("Data generation target path is `{}`", getTargetPath());
        LOGGER.info("Note: The game will be stopped after data generation finishes.");

        if (toRun.length > 0) {
            targetPath = getTargetPath();

            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
                throw new DataGenException("Data generators should not be executed in production environments. Exiting.");
            }

            FabricLoader.getInstance()
                    .getEntrypointContainers("data", DataEntrypoint.class)
                    .forEach(entrypoint -> entrypoint.getEntrypoint().run());

            LOGGER.info("Data generation finished, closing game.");
            System.exit(0);
        }
    }
}
