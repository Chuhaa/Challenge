package net.contal.demo.controllers;

import net.contal.demo.modal.BankTransaction;
import net.contal.demo.modal.CustomerAccount;
import net.contal.demo.services.BankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/banks")
public class BankController {
    private final Logger logger = LoggerFactory.getLogger(BankController.class);
    private final BankService dataService;

    public BankController(BankService dataService) {
        this.dataService = dataService;
    }

    /**
     * Create a new bank account.
     *
     * @param account {firstName:"" , lastName:"" }
     * @return bank account number
     */
    @RequestMapping(method = RequestMethod.POST,value = "/create")
    public long createBankAccount(@RequestBody CustomerAccount account){
        logger.info("Account {}" , account.toString());
        return dataService.createAnAccount(account);
    }

    /**
     * Create a new transaction.
     *
     * @param accountNumber BankAccount number
     * @param amount Amount as Transaction
     */
    @RequestMapping(method = RequestMethod.POST,value = "/transaction")
    public void addTransaction(@RequestParam("accountNumber") int accountNumber, @RequestParam("amount") Double amount){
        logger.info("Bank Account number is :{} , Transaction Amount {}",accountNumber,amount);
        dataService.addTransactions(accountNumber, amount);
    }


    /**
     * Retrieve the account balance.
     *
     * @param  account customer  bank account  number in json format {accountNumber : ""}
     * @return balance
     */
    @RequestMapping(method = RequestMethod.POST,value = "/balance")
    public Double getBalance(@RequestBody Map<String, Integer> account){
        logger.info("Bank Account number is :{}", account.get("accountNumber"));
        return dataService.getBalance(account.get("accountNumber"));
    }

    /**
     * Retrieve account details.
     *
     * @param accountNumber customer  bank account  number
     * @return balance
     */
    @RequestMapping(method = RequestMethod.POST,value = "/account")
    public CustomerAccount getAccount(@RequestParam("accountNumber") int accountNumber){
        logger.info("Bank Account number is :{}", accountNumber);
        return dataService.getAccountDetails(accountNumber);
    }


    /**
     * Retrieve last 10 transactions
     *
     * @param  account customer  bank account  number in json format {accountNumber : ""}
     * @return balance
     */
    @RequestMapping(method = RequestMethod.POST, value = "/transactionsLastTen")
    public List<BankTransaction> getLastTenTransactions(@RequestBody Map<String, Integer> account){
        logger.info("Bank Account number is :{}", account.get("accountNumber"));
        return dataService.getLastTenTransactions(account.get("accountNumber"));
    }


    /**
     * List balances by date
     *
     * @param  account customer  bank account  number in json format {accountNumber : ""}
     * @return balance
     */
    @RequestMapping(method = RequestMethod.POST, value = "/transactions")
    public Map<LocalDate, Double> getTransactions(@RequestBody Map<String, Integer> account){
        logger.info("Bank Account number is :{}", account.get("accountNumber"));
        return dataService.getDateBalance(account.get("accountNumber"));
    }

}
