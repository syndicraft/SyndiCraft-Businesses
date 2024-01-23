package org.crewco.SCBusinesses;

import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.crewco.BusinessManager;
import org.crewco.commands.BusinessAdminCommand;
import org.crewco.commands.BusinessCommand;
import org.crewco.commands.TabCompletions.BusinessTab;
import org.crewco.events.RegisterEvents;
import org.crewco.file.ConfigurationManager;
import org.crewco.file.MessageManager;

public final class SCBusinesses extends JavaPlugin {
    private ConfigurationManager configManager;

    private MessageManager messageManager;

    private BusinessManager businessManager;

    private PluginManager pm = getServer().getPluginManager();

    private Economy econ = null;

    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling.");
            this.pm.disablePlugin((Plugin)this);
            return;
        }
        getLogger().info("Vault found! Version " + getServer().getPluginManager().getPlugin("Vault").getDescription().getVersion());
        this.configManager = new ConfigurationManager(this);
        this.configManager.load();
        this.messageManager = new MessageManager(this);
        this.messageManager.load();
        this.businessManager = new BusinessManager(this);
        this.businessManager.load();
        registerEvents();
        getCommands();
    }

    public void onDisable() {
        this.businessManager.unload();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        this.econ = rsp.getProvider();
        return true;
    }

    public void registerEvents() {
        this.pm.registerEvents(new RegisterEvents(this),this);
    }

    public void getCommands() {
        getCommand("business").setExecutor(new BusinessCommand(this));
        getCommand("business").setTabCompleter(new BusinessTab(this));
        getCommand("businessadmin").setExecutor(new BusinessAdminCommand(this));
    }

    public Economy getEconomy() {
        return this.econ;
    }

    public ConfigurationManager getConfigManager() {
        return this.configManager;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public BusinessManager getBusinessManager() {
        return this.businessManager;
    }
}
