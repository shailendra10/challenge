# challenge
creating a account service, which will have end points for creating accounts and fund transfer between accounts.

# Getting Started

### Guides
The following guides illustrate how to use new features:
* AccountTransferController is implemented with exposed api (/v1/account/transfer) and type of api is post
* Sample request body is below
  {
  "accountFrom": "account124",
  "accountTo": "account123",
  "amount": 1000
  }

* New TransferService is implemented under service package for transferring amount from one account to another account

* TestCases Classes are below.
    1. AccountTransferControllerTest
    2. TransferServiceTest
    3. ConcurrentTransferTest


### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
