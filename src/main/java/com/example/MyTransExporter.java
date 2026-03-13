package com.example;

import com.example.command.ExportBiomeCommand;
import com.example.command.ExportStructuresCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyTransExporter implements ModInitializer {
    public static final String MOD_ID = "my_trans_exporter";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    @Override
    public void onInitialize() {
        LOGGER.info("Structure and Biome Translation Exporter initialized!");
        CommandRegistrationCallback.EVENT.register((
                (dispatcher, registryAccess, environment) ->
                        ExportStructuresCommand.register(dispatcher)
        ));
        CommandRegistrationCallback.EVENT.register((
                (dispatcher, registryAccess, environment) ->
                        ExportBiomeCommand.register(dispatcher)
        ));
    }
}
