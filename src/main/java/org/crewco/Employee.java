package org.crewco;

import org.bukkit.OfflinePlayer;

public class Employee {
    private OfflinePlayer player;

    private String title;

    private double salary;

    public Employee(OfflinePlayer player, String title, double salary) {
        this.player = player;
        this.title = title;
        this.salary = salary;
    }

    public Employee(OfflinePlayer player) {
        this.player = player;
        this.title = "";
        this.salary = 0.0D;
    }

    public OfflinePlayer getBukkitPlayer() {
        return this.player;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getSalary() {
        return this.salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}

