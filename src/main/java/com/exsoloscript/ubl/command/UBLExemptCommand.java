package com.exsoloscript.ubl.command;

import com.exsoloscript.ubl.banlist.BanList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.function.BiFunction;

@Singleton
public class UBLExemptCommand implements CommandExecutor {

    @Inject
    private BanList banList;

    @Inject
    private Logger logger;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String playerName = args.<String>getOne("player").get();

        GameProfileManager profileManager = Sponge.getServer().getGameProfileManager();

        profileManager.get(playerName, false).handleAsync(new BiFunction<GameProfile, Throwable, Void>() {
            @Override
            public Void apply(GameProfile profile, Throwable throwable) {
                try {
                    UBLExemptCommand.this.banList.exempt(profile.getUniqueId());
                    src.sendMessage(Text.of("Player " + profile.getName().get() + " was exempted successfully."));
                } catch (IOException | ObjectMappingException e) {
                    src.sendMessage(Text.builder("An error occurred while exempting the player").color(TextColors.RED).build());
                    UBLExemptCommand.this.logger.error("An exception occurred while exempting a player", e);
                }
                return null;
            }
        });


        return CommandResult.success();
    }
}
