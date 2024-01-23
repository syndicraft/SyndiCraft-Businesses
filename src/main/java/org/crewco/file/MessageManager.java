package org.crewco.file;

import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.crewco.SCBusinesses.SCBusinesses;

public class MessageManager {
    private SCBusinesses vcBusiness;

    File messagesFile;

    FileConfiguration messagesFileConfig;

    public MessageManager(SCBusinesses instance) {
        this.messagesFile = new File("plugins/SC-Business/messages.yml");
        this.messagesFileConfig = null;
        this.vcBusiness = instance;
    }

    public void load() {
        if (this.messagesFile.exists()) {
            this.vcBusiness.getLogger().config("Messages file found! Loading it now...");
            this.messagesFileConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.messagesFile);
            return;
        }
        this.vcBusiness.getLogger().config("Messages file not found! Creating it now...");
        this.messagesFileConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.messagesFile);
        this.messagesFileConfig.set("Prefix", "&8[&5Syndi&5Craft &dBusiness&8]");
        this.messagesFileConfig.set("Arguments Error", "&7Error, please check your arguments and try again.");
        this.messagesFileConfig.set("Player Not Found", "&7Error, cannot find &a%player%&7.");
        this.messagesFileConfig.set("No Permission", "&7Error, you do not have permission for this command.");
        this.messagesFileConfig.set("Business Established", "&a%owner%&7 has founded the business &a%business%&7.");
        this.messagesFileConfig.set("Business Name Already Taken", "&7Error, the business &a%business%&7 already exists.");
        this.messagesFileConfig.set("Business Name Too Long", "&7Error, the name &a%business%&7 is too long, the maximum amount of characters is %max characters%.");
        this.messagesFileConfig.set("Too Many Businesses", "&7Error, you already own too many businesses.");
        this.messagesFileConfig.set("Business Shutdown", "&a%owner%&7 has shutdown the business &a%business%&7.");
        this.messagesFileConfig.set("Business Not Found", "&7Error, the business &a%business%&7 cannot be found.");
        this.messagesFileConfig.set("Not Business Owner", "&7Error, you are not the owner of &a%business%&7.");
        this.messagesFileConfig.set("Not Business Employee", "&7Error, you are not an employee of &a%business%&7.");
        this.messagesFileConfig.set("Business Created Player", "&7You have created the business &a%business%&7.");
        this.messagesFileConfig.set("Business Description Changed", "&7You changed the description of &a%business%&7 to: &a%description%&7");
        this.messagesFileConfig.set("Deposit Successful", "&7Successfully deposited &a$%value%&7 into the account of &a%business%&7.");
        this.messagesFileConfig.set("Cant Afford Deposit", "&7Error, you cannot afford the deposit of &a$%value%&7 to &a%business%&7.");
        this.messagesFileConfig.set("Withdrawal Successful", "&7Successfully withdrew &a$%value%&7 from the account of &a%business%&7.");
        this.messagesFileConfig.set("Cant Afford Withdrawal", "&7Error, the &a%business%&7 does not have sufficient funds to withdraw &a$%value%&7.");
        this.messagesFileConfig.set("Payment Successful", "&7Successfully paid &a$%value%&7 from the account of &a%business%&7 to &a%player%&7.");
        this.messagesFileConfig.set("Payment Recieved", "&7You have recieved &a$%value%&7 from &a%business%&7.");
        this.messagesFileConfig.set("Cant Afford Payment", "&7Error, the &a%business%&7 does not have sufficient funds to pay out &a$%value%&7.");
        this.messagesFileConfig.set("Business Invite Sent", "&7You have invited &a%player%&7 to &a%business%&7.");
        this.messagesFileConfig.set("Business Invite Recieved", "&7You have recieved an invite to the business &a%business%&7.");
        this.messagesFileConfig.set("Player Already An Employee", "&7Error, &a%player%&7 is already an employee.");
        this.messagesFileConfig.set("Business Invite Accepted", "&7You have accepted your business invite to the &a%business%&7.");
        this.messagesFileConfig.set("Business Invite Declined", "&7You have declined your business invite to the &a%business%&7.");
        this.messagesFileConfig.set("Business Invite Expired", "&7Your business invite to &a%business%&7 has expired.");
        this.messagesFileConfig.set("No Business Invite", "&7Error, you do not have any active business invites.");
        this.messagesFileConfig.set("Successfully Quit", "&7You have successfully quit &a%business%&7.");
        this.messagesFileConfig.set("Owner Cant Quit", "&7Error, you cannot quit &a%business%&7 as you are its owner, please transfer the business if you wish to quit.");
        this.messagesFileConfig.set("No Business Invite", "&7Error, you do not have any active business invites.");
        this.messagesFileConfig.set("Player Not An Employee", "&7Error, &a%player%&7 is not an employee.");
        this.messagesFileConfig.set("Employee Fired", "&7You have fired &a%employee%&7 from &a%business%&7.");
        this.messagesFileConfig.set("Fired", "&7You have been fired from &a%business%&7 by &a%firer%&7.");
        this.messagesFileConfig.set("Business Transferred", "&7You have transferred &a%business%&7 to &a%new owner%&7.");
        this.messagesFileConfig.set("Business Transfer Recieved", "&7You have been transfered ownership of the business &a%business%&7 by &a%old owner%&7.");
        this.messagesFileConfig.set("Cash Register Placed", "&7Successfully placed a cash register for &a%business%&7.");
        this.messagesFileConfig.set("Cash Register Interact", "&7Please specify the amount of money you'd like to pay &a%business%&7.");
        this.messagesFileConfig.set("Cash Register Inactive", "&7Error, this cash register for &a%business%&7 is not currently active.");
        this.messagesFileConfig.set("Cash Register Interact Success", "&7You have successfully paid &a$%value%&7 to &a%business%&7.");
        this.messagesFileConfig.set("Cash Register Interact Failure", "&7A transaction error occurred while making a payment to &a%business%&7.");
        this.messagesFileConfig.set("Employee Title Changed", "&7You changed the title of &a%employee%&7 in &a%business%&7 to: &2%title%&7");
        this.messagesFileConfig.set("Title Changed", "&7Your title in &a%business%&7 has been changed to: &2%title%&7");
        this.messagesFileConfig.set("Balance Set", "&7Set the balance of &a%business%&7 to &a$%value%&7.");
        try {
            this.messagesFileConfig.save(this.messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String path) {
        String message = this.messagesFileConfig.getString(path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
