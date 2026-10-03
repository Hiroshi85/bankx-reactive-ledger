package com.github.hiroshi85.bankx_reactive_ledger.account.domain;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("accounts")
public class Account {
    @Id 
    private String id;

    @Indexed(unique = true) 
    private String accountNumber;
    
    private BigDecimal balance;

    public Account() { }

    public Account(String id, String accountNumber, BigDecimal balance) {
        this.id = id; this.accountNumber = accountNumber; this.balance = balance;
    }

    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
