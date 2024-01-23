package org.crewco.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.crewco.Business;
import org.crewco.BusinessManager;
import org.crewco.Employee;
import org.crewco.businesses.SCBusinesses;
import org.crewco.file.ConfigurationManager;
import org.crewco.file.MessageManager;
import org.crewco.util.EconUtils;
import org.crewco.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class BusinessCommand implements CommandExecutor {

    private final ConfigurationManager cManager;

    private final MessageManager msgManager;

    private final BusinessManager businessManager;

    private final Economy econ;

    public BusinessCommand(SCBusinesses instance) {
        this.cManager = instance.getConfigManager();
        this.msgManager = instance.getMessageManager();
        this.businessManager = instance.getBusinessManager();
        this.econ = instance.getEconomy();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Command Reference:");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Shows command reference.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business me" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Shows businesses you are employed in or own.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business establish <business>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Establishes a new business with the provided business name.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business shutdown <business>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Shuts down a business with the provided business name.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business accept <business>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Accepts your most recent business invite.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business decline <business>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Declines your most recent business invite.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business quit <business>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Quits the specified business if you are an employee.");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Displays relevant info for the business with the name provided.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business> transfer <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Transfer your business to the specified player.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business> hire <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Sends a hire request to the specified player to the specified business.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business> fire <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Fires the specified player from the specified business.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business> deposit <money>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Deposits the specified amount of money into the account of the specified business.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business> withdraw <money>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Withdraws the specified amount of money from the account of the specified business.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business> pay <player> <money>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Withdraws the specified amount of money from the account of the specified business.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business> setdesc <description>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Sets the description for the specified business.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/business <business> settitle <employee> <title>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Sets the title for the specified employee in the specified business.");
            return true;
        }
        if (args[0].equalsIgnoreCase("me")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            int businesses = 0;
            p.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Your businesses:");
            p.sendMessage("");
            for (Business business1 : this.businessManager.getBusinesses()) {
                if (this.businessManager.isBusinessOwner(business1, (OfflinePlayer)p)) {
                    p.sendMessage(ChatColor.GRAY + business1.getName() + " - " + ChatColor.LIGHT_PURPLE + "(OWNER)");
                    businesses++;
                    continue;
                }
                if (this.businessManager.isBusinessEmployee(business1, (OfflinePlayer)p)) {
                    p.sendMessage(ChatColor.GRAY + business1.getName() + " - " + ChatColor.LIGHT_PURPLE + "(Employee)");
                    businesses++;
                }
            }
            if (businesses == 0)
                p.sendMessage(ChatColor.GRAY + "You aren't a part of any business.");
            return true;
        }
        if (args[0].equalsIgnoreCase("establish")) {
            if (!(sender instanceof Player))
                return true;
            Player creator = (Player)sender;
            if (args.length <= 1) {
                creator.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            String name = args[1];
            if (name.trim().length() > this.cManager.getData().getInt("Business Name Character Limit")) {
                creator.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Name Too Long").replace("%business%", name.trim()).replace("%max characters%", Integer.toString(this.cManager.getData().getInt("Business Name Character Limit"))));
                return true;
            }
            String allowedChars = "AZERTYUIOPQSDFGHJKLMWXCVBNazertyuiopqsdfghjklmwxcvbn1234567890!@#$%^&*()_-=+{}[]?<>.";
            char[] arrayOfChar;
            int i;
            byte b;
            for (arrayOfChar = name.trim().toCharArray(), i = arrayOfChar.length, b = 0; b < i; ) {
                char c = arrayOfChar[b];
                if (allowedChars.indexOf(c) != -1) {
                    b++;
                    continue;
                }
                creator.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error").trim() + " Please check your business name for any illegal characters.");
                return true;
            }
            this.businessManager.createBusiness(name.trim(), (OfflinePlayer)creator);
            Bukkit.broadcastMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Established").replace("%owner%", creator.getName()).replace("%business%", name.trim()));
            creator.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Created Player").replace("%business%", name.trim()));
            return true;
        }
        if (args[0].equalsIgnoreCase("shutdown")) {
            if (!(sender instanceof Player))
                return true;
            Player deleter = (Player)sender;
            if (args.length <= 1) {
                deleter.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (this.businessManager.getBusiness(args[1]) == null) {
                deleter.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Not Found").replace("%business%", args[1]));
                return true;
            }
            Business business1 = this.businessManager.getBusiness(args[1]);
            if (!this.businessManager.isBusinessOwner(business1, (OfflinePlayer)deleter))
                return true;
            this.businessManager.deleteBusiness(business1);
            return true;
        }
        if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("accept")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (args.length <= 1) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            String businessName = args[1];
            if (this.businessManager.getBusiness(businessName) == null) {
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Not Found").replace("%business%", args[0]));
                return true;
            }
            Business business1 = this.businessManager.getBusiness(businessName);
            this.businessManager.acceptBusinessInvite(p, business1);
            return true;
        }
        if (args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("decline")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (args.length <= 1) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            String businessName = args[1];
            if (this.businessManager.getBusiness(businessName) == null) {
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Not Found").replace("%business%", args[0]));
                return true;
            }
            Business business1 = this.businessManager.getBusiness(businessName);
            this.businessManager.denyBusinessInvite(p, business1);
            return true;
        }
        if (args[0].equalsIgnoreCase("quit")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (args.length <= 1) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            String businessName = args[1];
            if (this.businessManager.getBusiness(businessName) == null) {
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Not Found").replace("%business%", args[0]));
                return true;
            }
            Business business1 = this.businessManager.getBusiness(businessName);
            if (!this.businessManager.isBusinessEmployee(business1, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Employee").replace("%business%", business1.getName()));
                return true;
            }
            if (this.businessManager.isBusinessOwner(business1, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Owner Cant Quit").replace("%business%", business1.getName()));
                return true;
            }
            Employee employee = business1.getEmployee(p.getUniqueId());
            business1.removeEmployee(employee);
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Successfully Quit").replace("%business%", business1.getName()));
            return true;
        }
        Business business = this.businessManager.getBusiness(args[0]);
        if (business == null) {
            sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Not Found").replace("%business%", args[0]));
            return true;
        }
        if (args.length <= 1) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + business.getName() + ":");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "CEO: " + ChatColor.GRAY + business.getOwner().getName());
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Description: " + ChatColor.GRAY + business.getDescription());
            if (this.businessManager.isBusinessEmployee(business, (OfflinePlayer)sender))
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Account: " + ChatColor.GRAY + "$" + business.getBalance());
            if (business.getEmployees().isEmpty()) {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Employees: " + ChatColor.GRAY + "None");
                return true;
            }
            StringBuilder employeeNames = new StringBuilder();
            for (int i = 0; i < business.getEmployees().size(); i++) {
                Employee employee = business.getEmployees().get(i);
                OfflinePlayer employeePlayer = employee.getBukkitPlayer();
                String formattedEmployeeName = ChatColor.GRAY + employeePlayer.getName();
                if (!employee.getTitle().equalsIgnoreCase(""))
                    formattedEmployeeName = ChatColor.DARK_PURPLE + "[" + employee.getTitle() + "] " + formattedEmployeeName;
                if (i != business.getEmployees().size() - 1)
                    formattedEmployeeName = formattedEmployeeName + ChatColor.DARK_GRAY + ", ";
                employeeNames.append(formattedEmployeeName);
            }
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Employees: " + ChatColor.GRAY + employeeNames);
            return true;
        }
        if (args[1].equalsIgnoreCase("transfer")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (!this.businessManager.isBusinessOwner(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Owner").replace("%business%", business.getName()));
                return true;
            }
            if (args.length <= 2) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (Bukkit.getPlayer(args[2]) == null) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Player Not Found").replace("%player%", args[2]));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            business.setOwner((OfflinePlayer)target);
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Transferred").replace("%new owner%", args[2]).replace("%business%", business.getName()));
            target.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Transfer Recieved").replace("%old owner%", p.getName()).replace("%business%", business.getName()));
        }
        if (args[1].equalsIgnoreCase("deposit")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (!this.businessManager.isBusinessEmployee(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Employee").replace("%business%", business.getName()));
                return true;
            }
            if (args.length <= 2) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (!NumberUtils.isDouble(args[2])) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            double deposit = Double.parseDouble(args[2]);
            if (deposit <= 0.0D) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (!EconUtils.canAfford(p, deposit, this.econ.getBalance((OfflinePlayer)p))) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cant Afford Deposit").replace("%value%", args[2]).replace("%business%", business.getName()));
                return true;
            }
            this.econ.withdrawPlayer((OfflinePlayer)p, deposit);
            business.addToBalance(deposit);
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Deposit Successful").replace("%value%", args[2]).replace("%business%", business.getName()));
            return true;
        }
        if (args[1].equalsIgnoreCase("withdraw")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (!this.businessManager.isBusinessEmployee(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Employee").replace("%business%", business.getName()));
                return true;
            }
            if (!this.businessManager.isBusinessOwner(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Owner").replace("%business%", business.getName()));
                return true;
            }
            if (args.length <= 2) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (!NumberUtils.isDouble(args[2])) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            double withdrawal = Double.parseDouble(args[2]);
            if (withdrawal <= 0.0D) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (withdrawal > business.getBalance()) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cant Afford Withdrawal").replace("%value%", args[2]).replace("%business%", business.getName()));
                return true;
            }
            this.econ.depositPlayer((OfflinePlayer)p, withdrawal);
            business.removeFromBalance(withdrawal);
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Withdrawal Successful").replace("%value%", args[2]).replace("%business%", business.getName()));
            return true;
        }
        if (args[1].equalsIgnoreCase("pay")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (!this.businessManager.isBusinessEmployee(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Employee").replace("%business%", business.getName()));
                return true;
            }
            if (!this.businessManager.isBusinessEmployee(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Owner").replace("%business%", business.getName()));
                return true;
            }
            if (args.length <= 3) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (Bukkit.getPlayer(args[2]) == null) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Player Not Found").replace("%player%", args[2]));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (!NumberUtils.isDouble(args[3])) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            double payment = Double.parseDouble(args[3]);
            if (payment <= 0.0D) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (payment > business.getBalance()) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cant Afford Payment").replace("%value%", Double.toString(payment)).replace("%business%", business.getName()));
                return true;
            }
            this.econ.depositPlayer((OfflinePlayer)target, payment);
            business.removeFromBalance(payment);
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Payment Successful").replace("%value%", Double.toString(payment)).replace("%business%", business.getName()).replace("%player%", target.getName()));
            target.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Payment Recieved").replace("%value%", Double.toString(payment)).replace("%business%", business.getName()));
            return true;
        }
        if (args[1].equalsIgnoreCase("hire")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (!this.businessManager.isBusinessOwner(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Owner").replace("%business%", business.getName()));
                return true;
            }
            if (args.length <= 2) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (Bukkit.getPlayer(args[2]) == null) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Player Not Found").replace("%player%", args[2]));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            this.businessManager.invite(target, business, p);
            return true;
        }
        if (args[1].equalsIgnoreCase("fire")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (!this.businessManager.isBusinessOwner(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Owner").replace("%business%", business.getName()));
                return true;
            }
            if (args.length == 2) {
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (Bukkit.getPlayer(args[2]) == null) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Player Not Found").replace("%player%", args[2]));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            this.businessManager.fire(target, business, p);
            return true;
        }
        if (args[1].equalsIgnoreCase("setdesc")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (!this.businessManager.isBusinessOwner(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Owner").replace("%business%", business.getName()));
                return true;
            }
            StringBuilder desc = new StringBuilder();
            for (int i = 2; i < args.length; i++)
                desc.append(args[i]).append(" ");
            business.setDescription(desc.toString().trim());
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Description Changed").replace("%business%", business.getName()).replace("%description%", desc.toString().trim()));
            return true;
        }
        if (args[1].equalsIgnoreCase("settitle")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (!this.businessManager.isBusinessOwner(business, (OfflinePlayer)p)) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Not Business Owner").replace("%business%", business.getName()));
                return true;
            }
            if (args.length <= 3) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (Bukkit.getPlayer(args[2]) == null) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Player Not Found").replace("%player%", args[2]));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (!this.businessManager.isBusinessEmployee(business, (OfflinePlayer)target)) {
                p.sendMessage(this.msgManager.getMessage("Player Not An Employee").replace("%player%", target.getName()));
                return true;
            }
            Employee employee = business.getEmployee(target.getUniqueId());
            if (args[3].equalsIgnoreCase("reset") || args[3].equalsIgnoreCase("none")) {
                employee.setTitle("");
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Employee Title Changed").replace("%business%", business.getName()).replace("%employee%", target.getName()).replace("%title%", "NONE"));
                target.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Title Changed").replace("%business%", business.getName()).replace("%title%", "NONE"));
                return true;
            }
            StringBuilder title = new StringBuilder();
            for (int i = 3; i < args.length; i++)
                title.append(args[i]).append(" ");
            employee.setTitle(title.toString().trim());
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Employee Title Changed").replace("%business%", business.getName()).replace("%employee%", target.getName()).replace("%title%", title.toString().trim()));
            target.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Title Changed").replace("%business%", business.getName()).replace("%title%", title.toString().trim()));
        } else {
            sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
        }
        return true;
    }
}