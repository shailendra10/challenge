package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientBalanceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class TransferService {
  public String transferAmount(Account accountFrom, Account accountTo, BigDecimal amount) throws InsufficientBalanceException {

    Account firstAccount;
    Account secondAccount;
    if (accountFrom.getAccountId().compareTo(accountTo.getAccountId()) < 0) {
      firstAccount = accountFrom;
      secondAccount = accountTo;
    } else {
      firstAccount = accountTo;
      secondAccount = accountFrom;
    }

    synchronized (firstAccount) {
      synchronized (secondAccount) {
        int compareBalance = accountFrom.getBalance().compareTo(amount);
        log.info("compareBalance {} ", compareBalance);

        if (compareBalance < 0) {
          throw new InsufficientBalanceException("Insufficient Balance in account " + accountFrom.getAccountId());
        }

        accountFrom.withdraw(amount);
        accountTo.deposit(amount);
        return "Transfer Successful";
      }
    }
  }
}
