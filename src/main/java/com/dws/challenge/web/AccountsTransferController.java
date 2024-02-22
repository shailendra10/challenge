package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transfer;
import com.dws.challenge.exception.InvalidAmountValueException;
import com.dws.challenge.exception.NotificationServiceException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransferService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/account/transfer")
@Slf4j
public class AccountsTransferController {
  private final TransferService transferService;
  private final NotificationService notificationService;
  private final AccountsService accountsService;

  @Autowired
  public AccountsTransferController(TransferService transferService, NotificationService notificationService, AccountsService accountsService) {

    this.transferService = transferService;
    this.notificationService = notificationService;
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transferAmount(@RequestBody @Valid Transfer transfer) {
    log.info("Transfer Amount  {}", transfer);
    String message = "";
    try {
      synchronized (this) {
      Account accountFrom = accountsService.getAccount(transfer.getAccountFrom());
      Account accountTo = accountsService.getAccount(transfer.getAccountTo());
      BigDecimal amount = transfer.getAmount();

      // Validations
      if(accountFrom == null || accountTo == null || accountFrom.getAccountId().isEmpty() || accountTo.getAccountId().isEmpty()) {
        throw new IllegalArgumentException("Invalid Account IDs");
      }

      if(accountFrom.getAccountId().equals(accountTo.getAccountId())) {
        throw new IllegalArgumentException("FromAccount and ToAccount can not be same");
      }

      if(amount == null) {
        throw new InvalidAmountValueException("Invalid amount");
      }

      if(amount.doubleValue() <= 0.0) {
        throw new InvalidAmountValueException("amount should be more than 0.0");
      }
          synchronized (transferService) {
            message = this.transferService.transferAmount(accountFrom, accountTo, amount);
            sendMailNotification(accountFrom, accountTo, amount);
          }
      }


    } catch (Exception exception) {
      log.error("Exception occurs", exception);
      return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(message, HttpStatus.OK);
  }

  private synchronized void sendMailNotification(Account accountFrom, Account accountTo, BigDecimal amount) throws NotificationServiceException {

    try {

      String fromAccountMessage = amount + " has been debited and transferred to account " + accountTo.getAccountId();
      String toAccountMessage = amount + " has been credited to your account from account " + accountFrom.getAccountId();

      notificationService.notifyAboutTransfer(accountFrom, fromAccountMessage);
      notificationService.notifyAboutTransfer(accountTo, toAccountMessage);

    } catch (Exception exception) {
      log.error("Error while calling notification service", exception);
      throw new NotificationServiceException("Error while calling notification service" + exception.getMessage());
    }

    log.info("notification sent successfully");

  }
}
