package org.crewco.util;

import org.bukkit.entity.Player;

public class EconUtils {
    public static boolean canAfford(Player p, double value, double playerBalance) {
        return (value <= playerBalance);
    }
}
