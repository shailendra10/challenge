package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransferServiceTest {

  @Autowired
  private TransferService transferService;

  @Autowired
  private AccountsService accountsService;

  private Account accountFrom;
  private Account accountTo;

  private BigDecimal amount;

  @BeforeEach
  public void setup() {
    accountsService.getAccountsRepository().clearAccounts();

    accountFrom = new Account("account123", new BigDecimal(1000));
    accountTo = new Account("account124", new BigDecimal(1000));

    accountsService.createAccount(accountFrom);
    accountsService.createAccount(accountTo);

    amount = new BigDecimal(1000);
  }

  @Test
  void transferAmountTest() {
    this.transferService.transferAmount(accountFrom, accountTo, amount);

    assertThat(this.accountsService.getAccount("account123").getBalance()).isEqualByComparingTo("0");
    assertThat(this.accountsService.getAccount("account124").getBalance()).isEqualByComparingTo("2000");
  }

  @Test
  void transferAmount_InsufficientBalance_Test() {
    amount = new BigDecimal(2000);
    assertThrows(InsufficientBalanceException.class, () -> {this.transferService.transferAmount(accountFrom, accountTo, amount);});
  }
}
