package org.crewco.commands;

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
import org.crewco.SCBusinesses.SCBusinesses;
import org.crewco.file.MessageManager;
import org.crewco.util.NumberUtils;

public class BusinessAdminCommand implements CommandExecutor {
    private MessageManager msgManager;

    private BusinessManager businessManager;

    public BusinessAdminCommand(SCBusinesses instance) {
        this.msgManager = instance.getMessageManager();
        this.businessManager = instance.getBusinessManager();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("vcbusiness.admin") && !sender.hasPermission("vcbusiness.dev")) {
            sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("No Permission"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Command Reference:");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/businessadmin" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Shows command reference.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/businessadmin list <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "List all businesses the specified player is an employee or owner of.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/businessadmin shutdown <business>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Forcibly shutdown a business with the provided name.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/businessadmin <business> transfer <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Forcibly transfer the specified business to the specified player.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/businessadmin <business> setdesc <description>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Forcibly set the description of the specified business.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/businessadmin <business> settitle <employee> <title>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Forcibly set the title of the specified employee in the specified business.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/businessadmin <business> setbal <business> <balance>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Forcibly set the balance of the specified business.");
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            if (args.length < 2) {
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (Bukkit.getOfflinePlayer(args[1]) == null) {
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Player Not Found").replace("%player%", args[2]));
                return true;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            int businesses = 0;
            sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + player.getName() + "'s businesses:");
            sender.sendMessage("");
            for (Business business1 : this.businessManager.getBusinesses()) {
                if (this.businessManager.isBusinessOwner(business1, player)) {
                    sender.sendMessage(ChatColor.GRAY + business1.getName() + " - " + ChatColor.LIGHT_PURPLE + "(OWNER)");
                    businesses++;
                    continue;
                }
                if (this.businessManager.isBusinessEmployee(business1, player)) {
                    sender.sendMessage(ChatColor.GRAY + business1.getName() + " - " + ChatColor.LIGHT_PURPLE + "(Employee)");
                    businesses++;
                }
            }
            if (businesses == 0)
                sender.sendMessage(ChatColor.GRAY + "No businesses.");
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
            this.businessManager.deleteBusiness(business1);
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
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Account: " + ChatColor.GRAY + "$" + business.getBalance());
            if (business.getEmployees().size() == 0) {
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
            sender.sendMessage(ChatColor.GREEN + "Employees: " + ChatColor.GRAY + employeeNames);
            return true;
        }
        if (args[1].equalsIgnoreCase("transfer")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
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
            return true;
        }
        if (args[1].equalsIgnoreCase("setdesc")) {
            StringBuilder desc = new StringBuilder();
            for (int i = 2; i < args.length; i++)
                desc.append(args[i]).append(" ");
            business.setDescription(desc.toString().trim());
            sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Business Description Changed").replace("%business%", business.getName()).replace("%description%", desc.toString().trim()));
            return true;
        }
        if (args[1].equalsIgnoreCase("settitle")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (args.length <= 3) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (Bukkit.getPlayer(args[2]) == null) {
                p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Player Not Found").replace("%player%", args[2]));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            assert target != null;
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
            return true;
        }
        if (args[1].equalsIgnoreCase("setbal") || args[1].equalsIgnoreCase("setbalance")) {
            if (args.length <= 2) {
                System.out.println(1);
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            if (!NumberUtils.isDouble(args[2])) {
                System.out.println(2);
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            double balance = Double.parseDouble(args[2]);
            if (balance < 0.0D) {
                System.out.println(3);
                sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
                return true;
            }
            business.setBalance(balance);
            sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Balance Set").replace("%value%", args[2]).replace("%business%", business.getName()));
            return true;
        }
        sender.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
        return true;
    }
}
