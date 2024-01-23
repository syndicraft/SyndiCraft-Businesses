package org.crewco.file;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.crewco.businesses.SCBusinesses;

public class ConfigurationManager {
    private final SCBusinesses vcBusiness;

    private File file;

    private FileConfiguration configuration;

    public ConfigurationManager(SCBusinesses instance) {
        this.file = new File("plugins/SC-Business", "config.yml");
        this.configuration = null;
        this.vcBusiness = instance;
    }

    public void load() {
        if (this.file.exists()) {
            this.vcBusiness.getLogger().config("Configuration file found! Loading it now...");
            this.configuration = (FileConfiguration)YamlConfiguration.loadConfiguration(this.file);
            return;
        }
        this.vcBusiness.getLogger().config("Configuration file not found! Creating it now...");
        this.configuration = (FileConfiguration)YamlConfiguration.loadConfiguration(this.file);
        this.configuration.set("Max Owned Businesses", 3);
        this.configuration.set("Business Name Character Limit", 30);
        this.configuration.set("Max Business Deposit", 100000);
        this.configuration.set("Max Business Withdrawal", 50000);
        this.configuration.set("Max Business Payment", 50000);
        try {
            this.configuration.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getData() {
        return this.configuration;
    }
}
