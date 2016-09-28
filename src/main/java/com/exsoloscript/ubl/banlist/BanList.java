package com.exsoloscript.ubl.banlist;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Data;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.config.ConfigDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Singleton
public class BanList {

    private BanListUpdater updater;

    private ConfigurationLoader<CommentedConfigurationNode> backupFileManager;
    private ConfigurationLoader<CommentedConfigurationNode> exemptFileManager;

    private Set<BanListRecord> bans;
    private Set<UUID> exempts;

    @Inject
    public BanList(@ConfigDir(sharedRoot = false) Path configDir, BanListUpdater updater) {
        this.updater = updater;
        this.bans = Sets.newHashSet();
        this.exempts = Sets.newHashSet();

        Path backupConfigPath = Paths.get(configDir.toString(), "ubl.bak");
        Path exemptConfigPath = Paths.get(configDir.toString(), "exempts.conf");
        this.backupFileManager = HoconConfigurationLoader.builder().setPath(backupConfigPath).build();
        this.exemptFileManager = HoconConfigurationLoader.builder().setPath(exemptConfigPath).build();
    }

    public void update() {
        this.updater.download(profiles -> {
            this.bans.clear();
            this.bans.addAll(profiles);
            this.saveBans();
        });
    }

    public void load() throws IOException, ObjectMappingException {
        this.loadBans();
        this.loadExempts();
    }

    private void loadBans() throws IOException, ObjectMappingException {
        CommentedConfigurationNode banNode = backupFileManager.load();
        TypeToken<List<BanListRecord>> token = new TypeToken<List<BanListRecord>>() {
        };
        this.bans = Sets.newHashSet(banNode.getNode("bans").getValue(token));
    }

    private void loadExempts() throws IOException, ObjectMappingException {
        CommentedConfigurationNode exemptNode = backupFileManager.load();
        TypeToken<List<UUID>> token = new TypeToken<List<UUID>>() {
        };
        this.exempts = Sets.newHashSet(exemptNode.getNode("exempts").getValue(token));
    }

    public void save() throws IOException, ObjectMappingException {
        this.saveBans();
        this.saveExempts();
    }

    private void saveBans() throws IOException, ObjectMappingException {
        CommentedConfigurationNode rootNode = backupFileManager.load();
        TypeToken<List<BanListRecord>> token = new TypeToken<List<BanListRecord>>() {
        };
        rootNode.getNode("bans").setComment("List of all UBL'd players").setValue(token, Lists.newArrayList(this.bans));
        this.backupFileManager.save(rootNode);
    }

    private void saveExempts() throws IOException, ObjectMappingException {
        CommentedConfigurationNode rootNode = this.exemptFileManager.load();
        TypeToken<List<UUID>> token = new TypeToken<List<UUID>>() {
        };
        rootNode.getNode("exempts").setComment("List of all exempted players").setValue(token, Lists.newArrayList(this.exempts));
        this.exemptFileManager.save(rootNode);
    }

    /**
     * Check if the given player is banned on the UBL and is not exempt on
     * this server
     *
     * @param uuid The uuid of the player to check
     * @return True, if the player is banned and not exempt, otherwise false
     */
    public boolean isBanned(UUID uuid) {
        return this.bans.stream().anyMatch(record -> record.getUuid().equals(uuid)) &&
                this.exempts.stream().noneMatch(exemptedUuid -> exemptedUuid.equals(uuid));
    }

    /**
     * Add a player to the exempt list
     *
     * @param uuid The player to add
     * @return False, if the player was already exempt, otherwise true
     */
    public boolean exempt(UUID uuid) throws IOException, ObjectMappingException {
        if (this.exempts.stream().noneMatch(exemptedUuid -> exemptedUuid.equals(uuid))) {
            this.exempts.add(uuid);
            this.saveExempts();

            return true;
        }

        return false;
    }

    /**
     * Removes a player from the exempt list
     *
     * @param uuid The player to remove
     * @return False, if the player was not exempt, otherwise true
     */
    public boolean unexempt(UUID uuid) throws IOException, ObjectMappingException {
        if (this.exempts.stream().anyMatch(exemptedUuid -> exemptedUuid.equals(uuid))) {
            this.exempts.remove(this.exempts.stream().filter(exemptedUuid -> exemptedUuid.equals(uuid)).findFirst().get());
            this.saveExempts();
            return true;
        }

        return false;
    }
}
