package org.crewco.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.crewco.Business;
import org.crewco.BusinessManager;
import org.crewco.SCBusinesses.SCBusinesses;
import org.crewco.file.MessageManager;
import org.crewco.util.EconUtils;
import org.crewco.util.NumberUtils;

public class RegisterEvents implements Listener {
    private SCBusinesses vcBusiness;

    private MessageManager msgManager;

    private BusinessManager businessManager;

    private Economy econ;

    private HashMap<Player, ArrayList<Object>> makingBusinessPayment;

    public RegisterEvents(SCBusinesses instance) {
        this.makingBusinessPayment = new HashMap<>();
        this.vcBusiness = instance;
        this.msgManager = this.vcBusiness.getMessageManager();
        this.businessManager = this.vcBusiness.getBusinessManager();
        this.econ = this.vcBusiness.getEconomy();
    }

    @EventHandler
    public void registerPlace(SignChangeEvent e) {
        String[] lines = e.getLines();
        if (!lines[0].equalsIgnoreCase("[Cash Register]"))
            return;
        if (!lines[1].equalsIgnoreCase(""))
            return;
        if (lines[2].equalsIgnoreCase(""))
            return;
        if (this.businessManager.getBusiness(lines[2]) == null)
            return;
        Player p = e.getPlayer();
        Business business = this.businessManager.getBusiness(lines[2]);
        if (!this.businessManager.isBusinessEmployee(business, (OfflinePlayer)p))
            return;
        p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cash Register Placed").replace("%business%", business.getName()));
    }

    @EventHandler
    public void registerInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null)
            return;
        if (!(block.getState() instanceof Sign))
            return;
        Sign register = (Sign)block.getState();
        String[] lines = register.getLines();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (!lines[0].equalsIgnoreCase("[Cash Register]"))
            return;
        if (!lines[1].equalsIgnoreCase(""))
            return;
        if (lines[2].equalsIgnoreCase(""))
            return;
        if (this.businessManager.getBusiness(lines[2]) == null)
            return;
        Player p = e.getPlayer();
        Business business = this.businessManager.getBusiness(lines[2]);
        if (this.businessManager.isBusinessEmployee(business, (OfflinePlayer)p)) {
            if (register.getLine(3).equalsIgnoreCase(p.getName())) {
                register.setLine(3, "");
                register.update();
                return;
            }
            register.setLine(3, p.getName());
            register.update();
            return;
        }
        if (lines[3].equalsIgnoreCase("")) {
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cash Register Inactive").replace("%business%", business.getName()));
            return;
        }
        ArrayList<Object> info = new ArrayList();
        info.add(0, business);
        info.add(1, register.getLocation());
        this.makingBusinessPayment.put(p, info);
        p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cash Register Interact").replace("%business%", business.getName()));
    }

    @EventHandler
    public void businessPayment(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!this.makingBusinessPayment.containsKey(p))
            return;
        String message = e.getMessage();
        p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Arguments Error"));
        ArrayList<Object> info = this.makingBusinessPayment.get(p);
        Business business = (Business)info.get(0);
        Location loc = (Location)info.get(1);
        if (!NumberUtils.isDouble(message)) {
            this.makingBusinessPayment.remove(p);
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cash Register Interact Failure").replace("%business%", business.getName()));
            return;
        }
        double payment = Double.parseDouble(message);
        if (payment <= 0.0D) {
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cash Register Interact Failure").replace("%business%", business.getName()));
            return;
        }
        if (!EconUtils.canAfford(p, payment, this.econ.getBalance((OfflinePlayer)p))) {
            this.makingBusinessPayment.remove(p);
            p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cash Register Interact Failure").replace("%business%", business.getName()));
            return;
        }
        this.econ.withdrawPlayer((OfflinePlayer)p, payment);
        business.addToBalance(payment);
        p.sendMessage(this.msgManager.getMessage("Prefix") + " " + this.msgManager.getMessage("Cash Register Interact Success").replace("%business%", business.getName()).replace("%value%", Double.toString(payment)));
        Bukkit.getServer().getScheduler().runTask((Plugin)this.vcBusiness, () -> generateReceipts(business, p, Double.valueOf(payment), loc));
        this.makingBusinessPayment.remove(p);
    }

    public void generateReceipts(Business business, Player customer, Double payment, Location registerLoc) {
        String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
        World world = registerLoc.getWorld();
        ItemStack receiptCustomer = new ItemStack(Material.PAPER, 1);
        ItemMeta receiptCustomerMeta = receiptCustomer.getItemMeta();
        receiptCustomerMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Receipt" + ChatColor.GRAY + " (Customer)");
        ArrayList<String> loreCustomer = new ArrayList<>();
        loreCustomer.add("");
        loreCustomer.add(ChatColor.DARK_GREEN + "Business: " + ChatColor.GRAY + business.getName());
        loreCustomer.add(ChatColor.DARK_GREEN + "Customer: " + ChatColor.GRAY + customer.getName());
        loreCustomer.add(ChatColor.DARK_GREEN + "Cashier: " + ChatColor.GRAY + ((Sign)registerLoc.getBlock().getState()).getLine(3));
        loreCustomer.add(ChatColor.DARK_GREEN + "Amount Paid: " + ChatColor.GRAY + "$" + payment);
        loreCustomer.add("");
        loreCustomer.add(ChatColor.GRAY + date);
        receiptCustomerMeta.setLore(loreCustomer);
        receiptCustomer.setItemMeta(receiptCustomerMeta);
        world.dropItem(registerLoc, receiptCustomer);
        ItemStack receiptBusiness = new ItemStack(Material.PAPER, 1);
        ItemMeta receiptBusinessMeta = receiptBusiness.getItemMeta();
        receiptBusinessMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Receipt" + ChatColor.GRAY + " (Business)");
        ArrayList<String> loreBusiness = new ArrayList<>();
        loreBusiness.add("");
        loreBusiness.add(ChatColor.DARK_GREEN + "Business: " + ChatColor.GRAY + business.getName());
        loreBusiness.add(ChatColor.DARK_GREEN + "Customer: " + ChatColor.GRAY + customer.getName());
        loreBusiness.add(ChatColor.DARK_GREEN + "Cashier: " + ChatColor.GRAY + ((Sign)registerLoc.getBlock().getState()).getLine(3));
        loreBusiness.add(ChatColor.DARK_GREEN + "Amount Recieved: " + ChatColor.GRAY + "$" + payment);
        loreBusiness.add("");
        loreBusiness.add(ChatColor.GRAY + date);
        receiptBusinessMeta.setLore(loreBusiness);
        receiptBusiness.setItemMeta(receiptBusinessMeta);
        world.dropItem(registerLoc, receiptBusiness);
    }
}

