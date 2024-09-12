package net.contal.demo.services;

import net.contal.demo.AccountNumberUtil;
import net.contal.demo.DbUtils;
import net.contal.demo.exceptions.AccountNumberNotFoundException;
import net.contal.demo.exceptions.InsufficientBalanceException;
import net.contal.demo.exceptions.ServiceException;
import net.contal.demo.modal.BankTransaction;
import net.contal.demo.modal.CustomerAccount;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO complete this service class
 * TODO use BankServiceTest class
 */
@Service
@Transactional
public class BankService {

    //USE this class to access database , you can call openASession to access database
    private final DbUtils dbUtils;

    private final Logger logger = LoggerFactory.getLogger(BankService.class);

    private final String CHECK_ACCOUNT_SQL = "select case when (count(ca) != 0) then true else false end from CustomerAccount ca " +
            "where ca.accountNumber = :accountNumber";

    @Autowired
    public BankService(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }


    /**
     * Save customAccount to database
     * return AccountNumber
     *
     * @param customerAccount populate this (firstName , lastName ) already provided
     * @return accountNumber
     */
    public int createAnAccount(CustomerAccount customerAccount) {
        int accountNumber = AccountNumberUtil.generateAccountNumber();
        customerAccount.setAccountNumber(accountNumber);
        Session session = null;
        try {
            session = dbUtils.openASession();
            session.save(customerAccount);
            session.getTransaction().commit();
            logger.info("Account created {}, {}", customerAccount.getFirstName(), accountNumber);
            return accountNumber;
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


    /**
     * Add transaction
     *
     * @param accountNumber target account number
     * @param amount        amount to register as transaction
     * @return boolean , if added as transaction
     */
    public boolean addTransactions(int accountNumber, Double amount) {

        if (amount == null || amount == 0.0) {
            return false;
        }
        Session session = null;

        String hql = "FROM CustomerAccount ca WHERE ca.accountNumber = :accountNumber";

        try {
            session = dbUtils.openASession();
            CustomerAccount customerAccount = session.createQuery(hql, CustomerAccount.class)
                    .setParameter("accountNumber", accountNumber)
                    .getSingleResult();

            if (amount + customerAccount.getAccountBalance() < 0) {
                throw new InsufficientBalanceException("Insufficient balance to do th transaction");
            }

            customerAccount.setAccountBalance(customerAccount.getAccountBalance() + amount);

            BankTransaction bankTransaction = new BankTransaction();
            bankTransaction.setCustomerAccount(customerAccount);
            bankTransaction.setTransactionAmount(amount);

            session.update(customerAccount);
            session.save(bankTransaction);
            session.getTransaction().commit();
            return true;

        } catch (NoResultException e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            logger.error(e.getMessage());
            return false;
        } catch (InsufficientBalanceException e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            logger.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


    /**
     * Get bank balance
     *
     * @param accountNumber target account
     * @return account balance
     */
    public double getBalance(int accountNumber) {

        String hql = "select sum(bt.transactionAmount) from BankTransaction bt where " +
                "bt.customerAccount.accountNumber = :accountNumber";

        try (Session session = dbUtils.openASession()) {
            Double balance = session.createQuery(hql, Double.class)
                    .setParameter("accountNumber", accountNumber)
                    .getSingleResult();
            if (balance != null) {
                return balance;
            }
            throw new AccountNumberNotFoundException("Account Number Not Found");
        } catch (NoResultException e) {
            logger.error("Account Number Not Found {}", accountNumber);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }


    /**
     * Get balance by date
     * ADVANCE TASK
     *
     * @param accountNumber accountNumber
     * @return HashMap [key: date , value: double]
     */
    public Map<LocalDate, Double> getDateBalance(int accountNumber) {

        String hcl = "select bt from BankTransaction bt where bt.customerAccount.accountNumber = :accountNumber " +
                "order by bt.transactionDate";
        try (Session session = dbUtils.openASession()) {


            List<BankTransaction> transactions = session.createQuery(hcl, BankTransaction.class)
                    .setParameter("accountNumber", accountNumber)
                    .getResultList();

            Map<LocalDate, Double> dateBalance = new LinkedHashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            double sum = 0.0d;
            for (BankTransaction transaction : transactions) {
                LocalDate date = LocalDate.parse(transaction.getTransactionDate().format(formatter), formatter);
                sum += transaction.getTransactionAmount();
                dateBalance.put(date, sum);
            }
            logger.info("Retrieved balance {}", accountNumber);
            return dateBalance;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * Get account details
     *
     * @param accountNumber accountNumber
     * @return account details
     */
    public CustomerAccount getAccountDetails(int accountNumber) {
        String hql = "from CustomerAccount where accountNumber = :accountNumber";

        try (Session session = dbUtils.openASession()) {
            logger.info("Retrieved account details {}", accountNumber);
            return session.createQuery(hql, CustomerAccount.class)
                    .setParameter("accountNumber", accountNumber)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.error("Account Number Not Found {}", accountNumber);
            throw new AccountNumberNotFoundException("Account Number Not Found");
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * Get last 10 transactions
     *
     * @param accountNumber accountNumber
     * @return List of last 10 bank transactions
     */
    public List<BankTransaction> getLastTenTransactions(int accountNumber) {


        String hql = "select bt from BankTransaction bt where bt.customerAccount.accountNumber = :accountNumber" +
                " order by bt.transactionDate desc";

        try (Session session = dbUtils.openASession()) {
            logger.info("Retrieved last 10 transactions {}", accountNumber);
            return session.createQuery(hql, BankTransaction.class)
                    .setParameter("accountNumber", accountNumber)
                    .setMaxResults(10)
                    .getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }
}