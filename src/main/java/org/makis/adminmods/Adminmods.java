package org.makis.adminmods;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Adminmods implements ModInitializer {
    public static final String MOD_ID = "adminmods";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AdminModeData.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AmCommand.register(dispatcher);
        });
    }
}
