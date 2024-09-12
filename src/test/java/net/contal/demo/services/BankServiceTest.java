package net.contal.demo.services;


import net.contal.demo.DbUtils;
import net.contal.demo.modal.BankTransaction;
import net.contal.demo.modal.CustomerAccount;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.NoResultException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BankServiceTest {

    @Mock
    private DbUtils dbUtils;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @InjectMocks
    BankService bankService;

    private final int MOCK_ACCOUNT_NUMBER = 12345678;
    private CustomerAccount mockAccount;

    @BeforeEach
    void setUp() {
        when(dbUtils.openASession()).thenReturn(session);
        when(session.getTransaction()).thenReturn(transaction);

        mockAccount = new CustomerAccount();
        mockAccount.setAccountNumber(MOCK_ACCOUNT_NUMBER);
    }

    @Test
    public void testCreateAccountSuccess() {
        CustomerAccount newAccount = new CustomerAccount();
        int accountNumber = bankService.createAnAccount(newAccount);
        assertNotNull(accountNumber);
    }


    @Test
    public void testSuccessfulTransaction() {

        org.hibernate.query.Query<CustomerAccount> query = mock(org.hibernate.query.Query.class);
        when(session.createQuery(anyString(), eq(CustomerAccount.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyInt())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(mockAccount);

        boolean result = bankService.addTransactions(MOCK_ACCOUNT_NUMBER, 50.0);

        assertTrue(result);
        verify(session, times(1)).createQuery(anyString(), eq(CustomerAccount.class));
    }

    @Test
    public void testAccountNumberNotFoundTransaction() {

        org.hibernate.query.Query<CustomerAccount> query = mock(org.hibernate.query.Query.class);
        when(session.createQuery(anyString(), eq(CustomerAccount.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyInt())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());
        when(session.getTransaction().isActive()).thenReturn(true);

        boolean result = bankService.addTransactions(12345679, 50.0);

        assertFalse(result);
        verify(transaction, times(1)).rollback();
    }

    @Test
    public void testAmountZeroTransaction() {

        org.hibernate.query.Query<CustomerAccount> query = mock(org.hibernate.query.Query.class);
        when(session.createQuery(anyString(), eq(CustomerAccount.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyInt())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        boolean result = bankService.addTransactions(12345679, 0.0);

        assertFalse(result);
    }

    @Test
    public void testGetBalanceSuccess() {
        org.hibernate.query.Query<Double> query = mock(org.hibernate.query.Query.class);

        Double value = 100.0;
        when(session.createQuery(anyString(), eq(Double.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyInt())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(value);
        double balance = bankService.getBalance(MOCK_ACCOUNT_NUMBER);

        assertEquals(balance, value);

    }

    @Test
    public void testGetDateBalanceSuccess() {

        BankTransaction mockTransaction = new BankTransaction();
        mockTransaction.setTransactionAmount(100.0);
        mockTransaction.setTransactionDate(LocalDate.of(2024, 9, 10));
        mockTransaction.setCustomerAccount(mockAccount);
        List<BankTransaction> mockTransactions = List.of(mockTransaction);

        org.hibernate.query.Query<BankTransaction> query = mock(org.hibernate.query.Query.class);

        when(session.createQuery(anyString(), eq(BankTransaction.class)))
                .thenReturn(query);
        when(query.setParameter(anyString(), anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(mockTransactions);
        Map<LocalDate, Double> dateBalance = bankService.getDateBalance(12345678);

        assertEquals(dateBalance.size(), mockTransactions.size());

    }


    @Test
    public void testAccountDetailsSuccess() {

        org.hibernate.query.Query<CustomerAccount> query = mock(org.hibernate.query.Query.class);

        when(session.createQuery(anyString(), eq(CustomerAccount.class)))
                .thenReturn(query);
        when(query.setParameter(anyString(), anyInt())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(mockAccount);
        CustomerAccount accountDetails = bankService.getAccountDetails(MOCK_ACCOUNT_NUMBER);

        assertEquals(accountDetails.getAccountNumber(), mockAccount.getAccountNumber());

    }


    @Test
    public void testLastTenTransactionsSuccess() {

        BankTransaction mockTransaction = new BankTransaction();
        mockTransaction.setTransactionAmount(100.0);
        mockTransaction.setTransactionDate(LocalDate.of(2024, 9, 10));
        mockTransaction.setCustomerAccount(mockAccount);
        List<BankTransaction> mockTransactions = List.of(mockTransaction);

        org.hibernate.query.Query<BankTransaction> query = mock(org.hibernate.query.Query.class);

        when(session.createQuery(anyString(), eq(BankTransaction.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyInt())).thenReturn(query);
        when(query.setMaxResults(10)).thenReturn(query);
        when(query.getResultList()).thenReturn(mockTransactions);

        List<BankTransaction> last10Transactions = bankService.getLastTenTransactions(MOCK_ACCOUNT_NUMBER);

        assertEquals(last10Transactions.size(), mockTransactions.size());
        assertEquals(last10Transactions.get(0).getCustomerAccount().getAccountNumber(), MOCK_ACCOUNT_NUMBER);

    }
}
