package com.exsoloscript.ubl.command;

import com.exsoloscript.ubl.banlist.BanList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.io.IOException;

@Singleton
public class ReloadCommand implements CommandExecutor {


    private BanList banList;

    @Inject
    public ReloadCommand(BanList banList) {
        this.banList = banList;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        try {
            this.banList.load();
            src.sendMessage(Text.of("Reloading ban list and exempts from the hard drive."));
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return CommandResult.empty();
        }

        return CommandResult.success();
    }
}
