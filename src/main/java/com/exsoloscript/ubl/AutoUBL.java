package com.exsoloscript.ubl;

import com.exsoloscript.ubl.banlist.BanList;
import com.exsoloscript.ubl.command.UBLReloadCommand;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "autoubl", name = "AutoUBL", version = "1.0", description = "AutoUBL Plugin for Sponge")
public class AutoUBL {

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer plugin;

    @Inject
    private BanList banList;

    // Command executors
    @Inject
    private UBLReloadCommand ublReloadCommand;

    @Listener
    public void onInitialize(GameInitializationEvent event) {
        try {
            loadConfig();
        } catch (IOException e) {
            this.logger.error("An error occurred while loading the config.", e);
        }

        registerCommands();

        this.banList.update();

    }

    private void loadConfig() throws IOException {
        if (Files.notExists(this.defaultConfig)) {
            Optional<Asset> defaultConfig = this.plugin.getAsset(this.plugin.getId() + ".conf");

            if (defaultConfig.isPresent()) {
                defaultConfig.get().copyToFile(this.defaultConfig);
                this.logger.info("Copied default config file from JAR to config directory.");
            } else {
                this.logger.error("A default configuration file is missing. This should never happen, please contact a developer.");
            }
        }
    }

    private void registerCommands() {
        CommandSpec ublReloadSpec = CommandSpec.builder()
                .executor(this.ublReloadCommand)
                .description(Text.of("Reload the UBL from a backup file"))
                .permission("autoubl.command.reload")
                .build();

        CommandSpec ublCommandSpec = CommandSpec.builder()
                .child(ublReloadSpec, "reload")
                .build();

        Sponge.getCommandManager().register(this.plugin, ublCommandSpec, "ubl");
    }

    public static Text prefix() {
        return Text.builder("[").color(TextColors.AQUA)
                .append(
                        Text.builder("AutoUBL").color(TextColors.GOLD).build(),
                        Text.builder("] ").color(TextColors.AQUA).build())
                .build();
    }
}
