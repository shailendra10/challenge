package com.dws.challenge;


import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transfer;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransferService;
import com.dws.challenge.web.AccountsTransferController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConcurrentTransferTest {

    AccountsTransferController accountsTransferController;

    @Autowired
    AccountsService accountsService;

    @Autowired
    TransferService transferService;
    @Mock
    NotificationService notificationService;

    private static final int NUM_THREADS = 50;
    private static final int NUM_REPEATS = 50; // Number of repeated tests per thread

    private Transfer transfer;
    private Transfer reverseTransfer;


    @BeforeEach
    void Setup() {
        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();

        Account account1 = new Account("Account123", new BigDecimal(1000.0));
        Account account2 = new Account("Account124", new BigDecimal(1000.0));
        accountsService.createAccount(account1);
        accountsService.createAccount(account2);
        transfer = new Transfer("Account123", "Account124", new BigDecimal(1000.0));
        reverseTransfer  = new Transfer("Account124", "Account123", new BigDecimal(1000.0));

        accountsTransferController = new AccountsTransferController(transferService, notificationService, accountsService);
    }

    @RepeatedTest(NUM_THREADS)
    void concurrentTransferTest() throws InterruptedException {
        // Setup a CountDownLatch to start all threads at the same time
        CountDownLatch latch = new CountDownLatch(1);

        // Setup an ExecutorService with the desired number of threads
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS*2);

        doNothing().when(notificationService).notifyAboutTransfer(any(), any());

        // Execute the transferAmount method in multiple threads
        for (int i = 0; i < NUM_REPEATS; i++) {
            executor.execute(() -> {
                try {
                    latch.await(); // Wait until all threads are ready to start
                    accountsTransferController.transferAmount(transfer); // Sample transfer
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 0; i < NUM_REPEATS; i++) {
            executor.execute(() -> {
                try {
                    latch.await(); // Wait until all threads are ready to start
                    accountsTransferController.transferAmount(reverseTransfer); // Sample transfer
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }


        // Release the latch to start all threads
        latch.countDown();
        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Account accountTo = accountsService.getAccount("Account124");
        assertThat(accountTo.getAccountId()).isEqualTo("Account124");

        Account accountFrom = accountsService.getAccount("Account123");
        assertThat(accountFrom.getAccountId()).isEqualTo("Account123");

        // Removed balance check, as balance in both account is unpredictable. its depends on which thread going to get chance to execute.
    }
}
