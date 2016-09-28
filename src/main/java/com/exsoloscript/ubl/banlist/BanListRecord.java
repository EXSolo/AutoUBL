package com.exsoloscript.ubl.banlist;

import com.google.common.collect.Maps;
import lombok.Data;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;
import java.util.UUID;

@Data
@ConfigSerializable
public class BanListRecord {

    @Setting
    private UUID uuid;
    @Setting
    private String name;
    @Setting
    private Map<String, String> properties;

    public BanListRecord() {
    }

    public BanListRecord(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.properties = Maps.newHashMap();
    }
}
