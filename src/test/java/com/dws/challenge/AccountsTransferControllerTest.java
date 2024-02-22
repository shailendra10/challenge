package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transfer;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransferService;
import com.dws.challenge.web.AccountsTransferController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsTransferControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  AccountsTransferController accountsTransferController;

  @Autowired
  TransferService transferService;

  @Autowired
  private AccountsService accountsService;

  @Mock
  NotificationService notificationService;

  @BeforeEach
  void prepareMockMvc_Setup() {
    MockitoAnnotations.openMocks(this);
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
    accountsTransferController = new AccountsTransferController(transferService, notificationService, accountsService);

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
    Account account1 = new Account("Account123", new BigDecimal(1000.0));
    Account account2 = new Account("Account124", new BigDecimal(2000.0));

    accountsService.createAccount(account1);
    accountsService.createAccount(account2);

  }

  @Test
  void invalidFromAccountTest() throws Exception {
    Transfer transfer = new Transfer("", "Account124", new BigDecimal(1000));

    doNothing().when(notificationService).notifyAboutTransfer(any(), any());
    ResponseEntity responseEntity =  accountsTransferController.transferAmount(transfer);
    assertEquals(new ResponseEntity<>("Invalid Account IDs", HttpStatus.BAD_REQUEST), responseEntity);
  }

  @Test
  void invalidToAccountTest() throws Exception {

    Transfer transfer = new Transfer("Account123", "", new BigDecimal(1000));

    doNothing().when(notificationService).notifyAboutTransfer(any(), any());
    ResponseEntity responseEntity =  accountsTransferController.transferAmount(transfer);
    assertEquals(new ResponseEntity<>("Invalid Account IDs", HttpStatus.BAD_REQUEST), responseEntity);
  }

  @Test
  void zeroAmountTest() throws Exception {

    Transfer transfer = new Transfer("Account123", "Account124", new BigDecimal(0));

    doNothing().when(notificationService).notifyAboutTransfer(any(), any());
    ResponseEntity responseEntity =  accountsTransferController.transferAmount(transfer);
    assertEquals(new ResponseEntity<>("amount should be more than 0.0", HttpStatus.BAD_REQUEST), responseEntity);

  }

  @Test
  void negativeAmountTest() throws Exception {

    Transfer transfer = new Transfer("Account123", "Account124", new BigDecimal(-100));
    doNothing().when(notificationService).notifyAboutTransfer(any(), any());
    ResponseEntity responseEntity =  accountsTransferController.transferAmount(transfer);
    assertEquals(new ResponseEntity<>("amount should be more than 0.0", HttpStatus.BAD_REQUEST), responseEntity);

  }

  @Test
  void selfTransferTest() throws Exception {
    Transfer transfer = new Transfer("Account123", "Account123", new BigDecimal(100));
    doNothing().when(notificationService).notifyAboutTransfer(any(), any());
    ResponseEntity responseEntity =  accountsTransferController.transferAmount(transfer);
    assertEquals(new ResponseEntity<>("FromAccount and ToAccount can not be same", HttpStatus.BAD_REQUEST), responseEntity);

  }

  @Test
  void transferAmount_SingleThreadTest() throws Exception {
    this.mockMvc.perform(post("/v1/account/transfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountFrom\":\"Account123\", \"accountTo\":\"Account124\",\"amount\":1000}"))
            .andExpect(status().isOk());

    Account accountTo = accountsService.getAccount("Account124");
    assertThat(accountTo.getAccountId()).isEqualTo("Account124");
    assertThat(accountTo.getBalance()).isEqualByComparingTo("3000");

    Account accountFrom = accountsService.getAccount("Account123");
    assertThat(accountFrom.getAccountId()).isEqualTo("Account123");
    assertThat(accountFrom.getBalance()).isEqualByComparingTo("0.0");
  }
}
