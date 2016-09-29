package com.exsoloscript.ubl.command;

import com.exsoloscript.ubl.banlist.BanList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

@Singleton
public class UBLUpdateCommand implements CommandExecutor {

    @Inject
    private BanList banList;

    @Inject
    private Logger logger;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        this.banList.update();
        src.sendMessage(Text.of("Checking the UBL for updates."));
        this.logger.info("Fetching new ban list from the given URL.");

        return CommandResult.success();
    }
}
