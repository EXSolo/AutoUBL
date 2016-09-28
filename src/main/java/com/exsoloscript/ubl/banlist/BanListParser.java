package com.exsoloscript.ubl.banlist;

import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BanListParser {

    public static String IGN_FIELD = "IGN";
    public static String UUID_FIELD = "UUID";
    public static String REASON_FIELD = "Reason";
    public static String DATE_BANNED_FIELD = "Date Banned";
    public static String EXPIRY_DATE_FIELD = "Expiry Date";
    public static String CASE_FIELD = "Case";

    public static Set<BanListRecord> parseBans(String data) throws IOException {
        Set<BanListRecord> bans = Sets.newHashSet();

        CSVParser parser = CSVFormat.EXCEL.withHeader().parse(new StringReader(data));

        for (CSVRecord csvRecord : parser) {
            UUID uuid;

            try {
                uuid = UUID.fromString(csvRecord.get(UUID_FIELD));
            } catch (Exception e) {
                continue;
            }

            BanListRecord banListRecord = new BanListRecord(uuid, csvRecord.get(IGN_FIELD));

            Map<String, String> recordProperties = banListRecord.getProperties();

            recordProperties.put("reason", csvRecord.get(REASON_FIELD));
            recordProperties.put("dateBanned", csvRecord.get(DATE_BANNED_FIELD));
            recordProperties.put("expiryDate", csvRecord.get(EXPIRY_DATE_FIELD));
            recordProperties.put("case", csvRecord.get(CASE_FIELD).replaceAll("https?://redd\\.it/", ""));

            bans.add(banListRecord);
        }

        return bans;
    }
}
