package com.exsoloscript.ubl.banlist;

import com.exsoloscript.ubl.tasks.UpdaterTask;
import com.exsoloscript.ubl.tasks.UpdaterTask.UpdaterTaskCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This task attempts to update the ban-list from the ban-list server and
 * backs up the ban-list locally.
 * <p>
 * If the ban-list server is not available, it will be reloaded from backup
 *
 * @author XHawk87
 * @author EXSolo
 */
@Singleton
public class BanListUpdater {

    @Inject
    private PluginContainer plugin;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> mainConfigManager;

    private Task autoChecker;

    /**
     * Schedule regular updates
     *
     * @param interval How often to update
     */
    public void schedule(long interval, TimeUnit unit, UpdaterTaskCallback callback) {
        // Stop the current updater from running
        cancel();

        CommentedConfigurationNode rootNode = loadNode();
        String banListUrl = rootNode.getNode("banlist-url").getString();
        int timeoutSeconds = rootNode.getNode("timeout").getInt();

        // Schedule the updater to run asynchronously with the new interval
        UpdaterTask updaterTask = new UpdaterTask(banListUrl, timeoutSeconds, logger, callback);
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        this.autoChecker = taskBuilder.execute(updaterTask)
                .async().interval(interval, unit)
                .delay(interval, unit).submit(this.plugin);
    }

    /**
     * Schedule an immediate update
     */
    public void download(UpdaterTaskCallback callback) {
        CommentedConfigurationNode rootNode = loadNode();
        String banListUrl = rootNode.getNode("banlist-url").getString();
        int timeoutSeconds = rootNode.getNode("timeout").getInt();

        UpdaterTask updaterTask = new UpdaterTask(banListUrl, timeoutSeconds, logger, callback);
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.execute(updaterTask).async().submit(this.plugin);
    }

    /**
     * Stop the regular updater
     */
    public void cancel() {
        if (this.autoChecker != null) {
            this.autoChecker.cancel();
        }
    }

    private CommentedConfigurationNode loadNode() {
        try {
            return this.mainConfigManager.load();
        } catch (IOException e) {
            return null;
        }
    }
}
