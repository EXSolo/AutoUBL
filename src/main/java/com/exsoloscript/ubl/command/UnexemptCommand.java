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
public class UnexemptCommand implements CommandExecutor {
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
                if (throwable == null) {
                    try {
                        boolean unexempted = UnexemptCommand.this.banList.exempt(profile.getUniqueId());
                        if (unexempted) {
                            src.sendMessage(Text.of("Player " + profile.getName().get() + " was unexempted successfully."));
                        } else {
                            src.sendMessage(Text.of("Player " + profile.getName().get() + " is not on the exempt list."));
                        }
                    } catch (IOException | ObjectMappingException e) {
                        src.sendMessage(Text.builder("An error occurred while unexempting the player").color(TextColors.RED).build());
                        UnexemptCommand.this.logger.error("An exception occurred while exempting a player", e);
                    }
                } else {
                    src.sendMessage(Text.builder("An error occurred while exempting the player. Did you specify the player's name correctly?").color(TextColors.RED).build());
                    UnexemptCommand.this.logger.warn("Couldn't get the UUID for player '" + profile.getName().get() + "'. Name was probably spelled wrong");
                }
                return null;
            }
        });


        return CommandResult.success();
    }
}
