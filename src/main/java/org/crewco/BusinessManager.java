package org.crewco;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.crewco.businesses.SCBusinesses;

public class BusinessManager {
    private final SCBusinesses vcBusiness;

    private final List<Business> businesses = new ArrayList<>();

    private final HashMap<Player, ArrayList<Business>> businessInvites = new HashMap<>();

    private final File[] businessFiles;

    private static void IGNORE_RESULT(boolean b) {}

    public BusinessManager(SCBusinesses vcBusiness) {
        this.vcBusiness = vcBusiness;
        File businessesDir = new File(this.vcBusiness.getDataFolder(), "/businesses");
        if (!businessesDir.exists())
            IGNORE_RESULT(businessesDir.mkdir());
        this.businessFiles = businessesDir.listFiles();
    }

    public void load() {
        if (this.businessFiles == null)
            return;
        for (File businessFile : this.businessFiles) {
            String businessName = businessFile.getName();
            if (!businessName.equalsIgnoreCase(".DS_Store")) {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(businessFile);
                OfflinePlayer owner = this.vcBusiness.getServer().getOfflinePlayer(UUID.fromString(yamlConfiguration.getString("owner")));
                String description = yamlConfiguration.getString("description");
                double balance = yamlConfiguration.getDouble("balance");
                List<Employee> employees = new ArrayList<>();
                if (!yamlConfiguration.isConfigurationSection("employees"))
                    return;
                for (String loopEmployee : yamlConfiguration.getConfigurationSection("employees").getKeys(false)) {
                    Employee employee = new Employee(this.vcBusiness.getServer().getOfflinePlayer(UUID.fromString(loopEmployee)), yamlConfiguration.getConfigurationSection("employees." + loopEmployee).getString("title"), yamlConfiguration.getConfigurationSection("employees." + loopEmployee).getDouble("salary"));
                    employees.add(employee);
                }
                Business business = new Business(businessFile.getName().replace(".yml", ""), owner, description, balance, employees);
                this.businesses.add(business);
            }
        }
    }

    public void unload() {
        List<String> businessNames = new ArrayList<>();
        for (Business business : this.businesses)
            businessNames.add(business.getName());
        List<File> businessesToDelete = new ArrayList<>();
        if (this.businessFiles != null)
            for (File businessFile : this.businessFiles) {
                if (businessNames.contains(businessFile.getName().replace(".yml", "")))
                    businessesToDelete.add(businessFile);
            }
        for (File businessFile : businessesToDelete)
            IGNORE_RESULT(businessFile.delete());
        for (Business business : this.businesses) {
            File businessFile = new File(this.vcBusiness.getDataFolder() + "/businesses", business.getName() + ".yml");
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.set("owner", business.getOwner().getUniqueId().toString());
            configuration.set("description", business.getDescription().trim());
            configuration.set("balance", business.getBalance());
            for (Employee employee : business.getEmployees()) {
                configuration.set("employees." + employee.getBukkitPlayer().getUniqueId() + ".title", employee.getTitle());
                configuration.set("employees." + employee.getBukkitPlayer().getUniqueId() + ".salary", employee.getSalary());
            }
            try {
                configuration.save(businessFile);
            } catch (IOException e) {
                this.vcBusiness.getLogger().log(Level.SEVERE, "Could not save business " + business.getName());
            }
        }
    }

    public List<Business> getBusinesses() {
        return this.businesses;
    }

    public Business getBusiness(String name) {
        Business business = null;
        for (Business loopBusiness : this.businesses) {
            if (!loopBusiness.getName().equalsIgnoreCase(name))
                continue;
            business = loopBusiness;
        }
        return business;
    }

    public Map<String, List<Business>> getBusinessesOf(OfflinePlayer player) {
        List<Business> ownedBusinesses = new ArrayList<>();
        List<Business> employedBusinesses = new ArrayList<>();
        for (Business business : this.businesses) {
            if (isBusinessOwner(business, player))
                ownedBusinesses.add(business);
            if (isBusinessEmployee(business, player))
                employedBusinesses.add(business);
        }
        Map<String, List<Business>> businesses = new HashMap<>();
        businesses.put("OWNER", ownedBusinesses);
        businesses.put("EMPLOYEE", employedBusinesses);
        return businesses;
    }

    public boolean createBusiness(String name, OfflinePlayer owner) {
        if (getBusiness(name) != null)
            return false;
        this.businesses.add(new Business(name, owner));
        return true;
    }

    public boolean deleteBusiness(Business business) {
        if (!this.businesses.contains(business))
            return false;
        this.vcBusiness.getEconomy().depositPlayer(business.getOwner(), business.getBalance());
        this.businesses.remove(business);
        return true;
    }

    public boolean isBusinessOwner(Business business, OfflinePlayer player) {
        return business.getOwner().getUniqueId().equals(player.getUniqueId());
    }

    public boolean isBusinessEmployee(Business business, OfflinePlayer player) {
        if (business.getOwner().getUniqueId().equals(player.getUniqueId()))
            return true;
        Employee employee = null;
        for (Employee loopEmployee : business.getEmployees()) {
            if (!loopEmployee.getBukkitPlayer().getUniqueId().equals(player.getUniqueId()))
                continue;
            employee = loopEmployee;
        }
        return (employee != null);
    }

    public void invite(Player invitedEmployee, Business business, Player inviter) {
        ArrayList<Business> invitingBusinesses;
        if (!this.businesses.contains(business))
            return;
        if (business.getEmployee(invitedEmployee.getUniqueId()) != null) {
            inviter.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("Player Already An Employee").replace("%player%", invitedEmployee.getName()));
            return;
        }
        if (this.businessInvites.containsKey(invitedEmployee)) {
            invitingBusinesses = this.businessInvites.get(invitedEmployee);
        } else {
            invitingBusinesses = new ArrayList<>();
        }
        invitingBusinesses.add(business);
        this.businessInvites.put(invitedEmployee, invitingBusinesses);
        inviter.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("Business Invite Sent").replace("%business%", business.getName()).replace("%player%", invitedEmployee.getName()));
        invitedEmployee.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("Business Invite Received").replace("%business%", business.getName()).replace("%inviter%", invitedEmployee.getName()));

        // Fix: Use a Runnable instead of a lambda expression
        this.vcBusiness.getServer().getScheduler().runTaskLater((Plugin) this.vcBusiness, new Runnable() {
            @Override
            public void run() {
                if (!businessInvites.containsKey(invitedEmployee))
                    return;
                if (!businessInvites.get(invitedEmployee).contains(business))
                    return;
                businessInvites.get(invitedEmployee).remove(business);
                invitedEmployee.sendMessage(ChatColor.RED + "Business invite expired...");
                if (!businessInvites.get(invitedEmployee).isEmpty())
                    return;
                businessInvites.remove(invitedEmployee);
            }
        }, 1200L);
    }

    public void fire(Player firedEmployee, Business business, Player kicker) {
        if (!this.businesses.contains(business))
            return;
        if (business.getEmployee(firedEmployee.getUniqueId()) == null) {
            kicker.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("Player Not An Employee").replace("%player%", firedEmployee.getName()));
            return;
        }
        Employee employee = business.getEmployee(firedEmployee.getUniqueId());
        business.removeEmployee(employee);
        kicker.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("Employee Fired").replace("%business%", business.getName()).replace("%employee%", firedEmployee.getName()));
        firedEmployee.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("Fired").replace("%business%", business.getName()).replace("%firer%", kicker.getName()));
    }

    public void acceptBusinessInvite(Player invitedEmployee, Business business) {
        if (!this.businessInvites.containsKey(invitedEmployee) || !(this.businessInvites.get(invitedEmployee)).contains(business)) {
            invitedEmployee.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("No Business Invite"));
            return;
        }
        Employee employee = new Employee((OfflinePlayer)invitedEmployee);
        business.addEmployee(employee);
        (this.businessInvites.get(invitedEmployee)).remove(business);
        invitedEmployee.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("Business Invite Accepted").replace("%business%", business.getName()));
    }

    public void denyBusinessInvite(Player invitedEmployee, Business business) {
        if (!this.businessInvites.containsKey(invitedEmployee) || !(this.businessInvites.get(invitedEmployee)).contains(business)) {
            invitedEmployee.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("No Business Invite"));
            return;
        }
        this.businessInvites.remove(invitedEmployee);
        (this.businessInvites.get(invitedEmployee)).remove(business);
        invitedEmployee.sendMessage(this.vcBusiness.getMessageManager().getMessage("Prefix") + " " + this.vcBusiness.getMessageManager().getMessage("Business Invite Declined").replace("%business%", business.getName()));
    }
}
