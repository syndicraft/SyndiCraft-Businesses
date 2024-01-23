package org.crewco.commands.TabCompletions;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.crewco.Business;
import org.crewco.BusinessManager;
import org.crewco.businesses.SCBusinesses;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BusinessTab implements TabCompleter {
    private final SCBusinesses vcBusiness;
    List<String> arguments = new ArrayList<String>();

    public BusinessTab(SCBusinesses vcBusiness) {
        this.vcBusiness = vcBusiness;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        BusinessManager businessManager = new BusinessManager(vcBusiness);
        if (arguments.isEmpty()){
            arguments.add("me");
            arguments.add("establish");
            arguments.add("shutdown");
            arguments.add("accept");
            arguments.add("decline");
            arguments.add("quit");

            List<String> result = new ArrayList<>();
            List<Business> business = businessManager.getBusinesses();
            if (strings.length == 1){
                for (Business business1 : business) {
                    if (businessManager.isBusinessOwner(business1,(OfflinePlayer) commandSender)){
                        result.add(business1.getName());
                    }
                }
                arguments.add("transfer");
                arguments.add("hire");
                arguments.add("fire");
                arguments.add("deposit");
                arguments.add("withdraw");
                arguments.add("pay");
                arguments.add("setdesc");
                arguments.add("settitle");
                return result;
            }
        }
        return arguments;
    }
}
