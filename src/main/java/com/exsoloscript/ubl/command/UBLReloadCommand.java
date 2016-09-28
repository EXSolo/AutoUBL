package com.exsoloscript.ubl.command;

import com.exsoloscript.ubl.AutoUBL;
import com.exsoloscript.ubl.banlist.BanList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.io.IOException;

@AllArgsConstructor
@Singleton
public class UBLReloadCommand implements CommandExecutor {

    @Inject
    private BanList banList;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        try {
            this.banList.load();
            src.sendMessage(AutoUBL.prefix().concat(Text.of("Reloading ban list from backup.")));
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return CommandResult.empty();
        }

        return CommandResult.success();
    }
}
