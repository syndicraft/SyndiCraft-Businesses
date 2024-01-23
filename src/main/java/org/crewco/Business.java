package org.crewco;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;

public class Business {
    private final String name;

    private OfflinePlayer owner;

    private String description;

    private double balance;

    private final List<Employee> employees;

    public Business(String name, OfflinePlayer owner) {
        this.name = name;
        this.owner = owner;
        this.description = "A new company.";
        this.balance = 0.0D;
        this.employees = new ArrayList<>();
    }

    public Business(String name, OfflinePlayer owner, String description, double balance, List<Employee> employees) {
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.balance = balance;
        this.employees = employees;
    }

    public String getName() {
        return this.name;
    }

    public OfflinePlayer getOwner() {
        return this.owner;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void addToBalance(double amount) {
        this.balance += amount;
    }

    public void removeFromBalance(double amount) {
        this.balance -= amount;
    }

    public List<Employee> getEmployees() {
        return this.employees;
    }

    public Employee getEmployee(String name) {
        Employee employee = null;
        for (Employee loopEmployee : this.employees) {
            if (!loopEmployee.getBukkitPlayer().getName().equals(name))
                continue;
            employee = loopEmployee;
        }
        return employee;
    }

    public Employee getEmployee(UUID uuid) {
        Employee employee = null;
        for (Employee loopEmployee : this.employees) {
            if (!loopEmployee.getBukkitPlayer().getUniqueId().equals(uuid))
                continue;
            employee = loopEmployee;
        }
        return employee;
    }

    public void addEmployee(Employee employee) {
        this.employees.add(employee);
    }

    public void removeEmployee(Employee employee) {
        this.employees.remove(employee);
    }
}
