package com.exsoloscript.ubl.banlist;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Data;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Data
@Singleton
public class BanList {

    private BanListUpdater updater;

    private ConfigurationLoader<CommentedConfigurationNode> defaultConfigManager;
    private ConfigurationLoader<CommentedConfigurationNode> backupConfigManager;
    private ConfigurationLoader<CommentedConfigurationNode> exemptConfigManager;

    private Set<BanListRecord> bans;
    private Set<UUID> exempts;

    private String banMessageTemplate;

    @Inject
    public BanList(@ConfigDir(sharedRoot = false) Path configDir,
                   @DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> defaultConfigManager,
                   BanListUpdater updater) {
        this.updater = updater;
        this.bans = Sets.newHashSet();
        this.exempts = Sets.newHashSet();

        Path backupConfigPath = Paths.get(configDir.toString(), "bans.conf");
        Path exemptConfigPath = Paths.get(configDir.toString(), "exempts.conf");
        this.backupConfigManager = HoconConfigurationLoader.builder().setPath(backupConfigPath).build();
        this.exemptConfigManager = HoconConfigurationLoader.builder().setPath(exemptConfigPath).build();
        this.defaultConfigManager = defaultConfigManager;

        getBanMessageTemplate(true);
    }

    public void update() {
        this.updater.download(profiles -> {
            this.bans.clear();
            this.bans.addAll(profiles);
            this.saveBans();
        });
    }

    public void schedule() {
        this.updater.cancel();
        this.updater.schedule(profiles -> {
            this.bans.clear();
            this.bans.addAll(profiles);
            this.saveBans();
        });
    }

    public void load() throws IOException, ObjectMappingException {
        this.loadBans();
        this.loadExempts();
        getBanMessageTemplate(true);
    }

    private void loadBans() throws IOException, ObjectMappingException {
        CommentedConfigurationNode banNode = backupConfigManager.load();
        this.bans = Sets.newHashSet(banNode.getNode("bans").getList(TypeToken.of(BanListRecord.class)));
    }

    private void loadExempts() throws IOException, ObjectMappingException {
        CommentedConfigurationNode exemptNode = exemptConfigManager.load();
        this.exempts = Sets.newHashSet(exemptNode.getNode("exempts").getList(TypeToken.of(UUID.class)));
    }

    public void save() throws IOException, ObjectMappingException {
        this.saveBans();
        this.saveExempts();
    }

    private void saveBans() throws IOException, ObjectMappingException {
        CommentedConfigurationNode rootNode = backupConfigManager.load();
        TypeToken<List<BanListRecord>> token = new TypeToken<List<BanListRecord>>() {
        };
        rootNode.getNode("bans").setComment("List of all UBL'd players").setValue(token, Lists.newArrayList(this.bans));
        this.backupConfigManager.save(rootNode);
    }

    private void saveExempts() throws IOException, ObjectMappingException {
        CommentedConfigurationNode rootNode = this.exemptConfigManager.load();
        TypeToken<List<UUID>> token = new TypeToken<List<UUID>>() {
        };
        rootNode.getNode("exempts").setComment("List of all exempted players").setValue(token, Lists.newArrayList(this.exempts));
        this.exemptConfigManager.save(rootNode);
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

    public String getBanMessageTemplate(boolean loadFromConfig) {
        if (loadFromConfig || this.banMessageTemplate == null) {
            try {
                CommentedConfigurationNode rootNode = this.defaultConfigManager.load();
                this.banMessageTemplate = rootNode.getNode("ban-message").getString();
            } catch (IOException ignored) {
                this.banMessageTemplate = "You are on the Reddit UHC Universal Ban List";
            }
        }

        return this.banMessageTemplate;
    }

    /**
     * @param uuid The universally unique identifier of the banned player
     * @return A personalised ban message for this player
     */
    public Text getBanMessage(UUID uuid) {
        Optional<BanListRecord> optionalRecord = this.bans.stream().filter(record -> record.getUuid().equals(uuid)).findFirst();

        if (optionalRecord.isPresent()) {
            BanListRecord record = optionalRecord.get();

            Map<String, String> properties = Maps.newHashMap(record.getProperties());
            properties.put("case", "http://redd.it/" + properties.get("case"));

            StrSubstitutor substitutor = new StrSubstitutor(properties);
            return Text.of(substitutor.replace(getBanMessageTemplate(false)));
        }

        return Text.EMPTY;
    }
}
