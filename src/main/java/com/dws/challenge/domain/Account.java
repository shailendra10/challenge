package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Account {

  @NotNull
  @NotEmpty
  private final String accountId;

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }
  public String getAccountId() {
    return accountId;
  }

  public synchronized BigDecimal getBalance() {
     return  balance;
  }

  public synchronized void setBalance(BigDecimal balance) {
    this. balance = balance;
  }


  public synchronized void withdraw (BigDecimal amount) {
    balance = balance.subtract(amount);
  }
  public synchronized void deposit(BigDecimal amount) {
    balance = balance.add(amount);
  }
}
