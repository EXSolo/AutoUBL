package com.exsoloscript.ubl;

import com.exsoloscript.ubl.banlist.BanList;
import com.exsoloscript.ubl.command.ExemptCommand;
import com.exsoloscript.ubl.command.ReloadCommand;
import com.exsoloscript.ubl.command.UnexemptCommand;
import com.exsoloscript.ubl.command.UpdateCommand;
import com.exsoloscript.ubl.listener.LoginListener;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Plugin(id = "autoubl", name = "AutoUBL", version = "1.0", description = "AutoUBL Plugin for Sponge")
public class AutoUBL {

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer plugin;

    @Inject
    private BanList banList;

    // Command executors
    @Inject
    private ReloadCommand reloadCommand;

    @Inject
    private ExemptCommand exemptCommand;

    @Inject
    private UnexemptCommand unexemptCommand;

    @Inject
    private UpdateCommand ublUpdateCommand;

    // Event listeners

    @Inject
    private LoginListener loginListener;

    @Listener
    public void onInitialize(GameInitializationEvent event) {
        try {
            loadConfig();
        } catch (ObjectMappingException | IOException e) {
            this.logger.error("An error occurred while loading the config.", e);
        }

        registerCommands();
        registerListeners();

        loadBanList();
    }

    private void loadConfig() throws IOException, ObjectMappingException {
        Path exempts = Paths.get(this.configDir.toString(), "exempts.conf");
        Path bans = Paths.get(this.configDir.toString(), "bans.conf");

        if (Files.notExists(this.defaultConfig)) {
            Optional<Asset> defaultConfig = this.plugin.getAsset(this.plugin.getId() + ".conf");

            if (defaultConfig.isPresent()) {
                defaultConfig.get().copyToFile(this.defaultConfig);
                this.logger.info("Copied default config file from JAR to config directory.");
            } else {
                this.logger.error("A default configuration file is missing. This should never happen, please contact a developer.");
            }
        }

        if (Files.notExists(exempts) || Files.notExists(bans)) {
            // Create files is they don't exist.
            this.banList.save();
        }
    }

    private void loadBanList() {
        // Update the ban list when the server starts
        this.banList.update();
        // Start scheduler that updates the backup file over time.
        this.banList.schedule();
    }

    private void registerListeners() {
        Sponge.getEventManager().registerListeners(this, this.loginListener);
    }

    private void registerCommands() {
        CommandSpec ublReloadSpec = CommandSpec.builder()
                .executor(this.reloadCommand)
                .description(Text.of("Reload the UBL from a backup file"))
                .permission("autoubl.command.reload")
                .build();

        CommandSpec ublExemptSpec = CommandSpec.builder()
                .executor(this.exemptCommand)
                .description(Text.of("Exempt a player"))
                .permission("autoubl.command.exempt")
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .build();

        CommandSpec ublUnexemptSpec = CommandSpec.builder()
                .executor(this.unexemptCommand)
                .description(Text.of("Unexempt a player"))
                .permission("autoubl.command.unexempt")
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .build();

        CommandSpec ublUpdateSpec = CommandSpec.builder()
                .executor(this.ublUpdateCommand)
                .description(Text.of("Reload the ban list from the configured URL"))
                .permission("autoubl.command.update")
                .build();

        CommandSpec ublCommandSpec = CommandSpec.builder()
                .child(ublReloadSpec, "reload")
                .child(ublExemptSpec, "exempt")
                .child(ublUnexemptSpec, "unexempt")
                .child(ublUpdateSpec, "update")
                .build();

        Sponge.getCommandManager().register(this.plugin, ublCommandSpec, "ubl");
    }
}
