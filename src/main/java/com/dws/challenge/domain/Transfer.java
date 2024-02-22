package com.dws.challenge.domain;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class Transfer {

  @NotNull
  @NotEmpty
  private final String accountFrom;

  @NotNull
  @NotEmpty
  private final String accountTo;

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal amount;

  public Transfer(String accountFrom, String accountTo, BigDecimal amount) {
    this.accountFrom = accountFrom;
    this.accountTo = accountTo;
    this.amount = amount;
  }
}
