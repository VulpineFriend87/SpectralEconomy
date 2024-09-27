package dev.vulpine.spectralEconomy.instance;

import dev.vulpine.spectralEconomy.manager.AccountManager;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {

    private final UUID owner;
    private BigDecimal balance;

    public Account(UUID owner, BigDecimal balance) {

        this.owner = owner;
        this.balance = balance;

    }

    public void addMoney(BigDecimal amount) {

        balance = balance.add(amount);

        AccountManager.updateAccount(this);

    }

    public void removeMoney(BigDecimal amount) {

        balance = balance.subtract(amount);

        AccountManager.updateAccount(this);

    }

    public void setBalance(BigDecimal balance) {

        this.balance = balance;

        AccountManager.updateAccount(this);

    }

    public UUID getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

}