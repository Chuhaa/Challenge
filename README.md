# Bank Service Application

This application simulates a simple banking system with functionalities like account creation, transactions, and balance management. It is built using Spring Boot and provides a RESTful API interface for interacting with the banking services.

## Key Features:
- Create new customer bank accounts.
- Process transactions on customer accounts.
- Retrieve the current balance of customer accounts.
- Fetch account details
- Retrieve last 10 recent transactions.
- Retrieve historical balances of accounts by date.
- 
## Endpoints

### 1. Create a new bank account
**URL:** `/banks/create`  
**Method:** `POST`  
**Description:** Creates a new bank account using the provided account information.

#### Request Body:
```json
{
  "firstName": "string",
  "lastName": "string"
}
```

#### Response:
- **Success:** Returns the created bank 8 digits account number (`int`).
- **Failure:** Appropriate error message.

#### Example:
```bash
curl -X POST http://localhost:8080/banks/create -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "Doe"}'
```

---

### 2. Add a new transaction
**URL:** `/banks/transaction`  
**Method:** `POST`  
**Description:** Adds a new transaction for the specified bank account.

#### Request Parameters:
- `accountNumber`: The account number to apply the transaction to (type: `int`).
- `amount`: The transaction amount (type: `Double`).

#### Response:
- **Success:** Returns `200 OK` without content.
- **Failure:** Appropriate error message.

#### Example:
```bash
curl -X POST "http://localhost:8080/banks/transaction?accountNumber=123&amount=500.0"
```

---

### 3. Retrieve the account balance
**URL:** `/banks/balance`  
**Method:** `POST`  
**Description:** Retrieves the balance for the specified account.

#### Request Body:
```json
{
  "accountNumber": int
}
```

#### Response:
- **Success:** Returns the balance (`Double`).
- **Failure:** Appropriate error message.

#### Example:
```bash
curl -X POST http://localhost:8080/banks/balance -H "Content-Type: application/json" -d '{"accountNumber": 123}'
```

---

### 4. Retrieve account details
**URL:** `/banks/account`  
**Method:** `POST`  
**Description:** Retrieves details of a specified customer account.

#### Request Parameters:
- `accountNumber`: The account number to retrieve details for (type: `int`).

#### Response:
- **Success:** Returns the customer account details as a `CustomerAccount` object.
- **Failure:** Appropriate error message.

#### Example:
```bash
curl -X POST "http://localhost:8080/banks/account?accountNumber=123"
```

---

### 5. Retrieve the last 10 transactions
**URL:** `/banks/transactionsLastTen`  
**Method:** `POST`  
**Description:** Retrieves the last 10 transactions for the specified account.

#### Request Body:
```json
{
  "accountNumber": int
}
```

#### Response:
- **Success:** Returns a list of the last 10 `BankTransaction` objects.
- **Failure:** Appropriate error message.

#### Example:
```bash
curl -X POST http://localhost:8080/banks/transactionsLastTen -H "Content-Type: application/json" -d '{"accountNumber": 123}'
```

---

### 6. List balances by date
**URL:** `/banks/transactions`  
**Method:** `POST`  
**Description:** Retrieves the account balances by date for the specified account.

#### Request Body:
```json
{
  "accountNumber": int
}
```

#### Response:
- **Success:** Returns a map of `LocalDate` and `Double` representing the balances by date.
- **Failure:** Appropriate error message.

#### Example:
```bash
curl -X POST http://localhost:8080/banks/transactions -H "Content-Type: application/json" -d '{"accountNumber": 123}'
```


## Notes

### Inconsistencies:
- There were inconsistencies with the `accountNumber` data type. I assumed it is `int`.

### Further Improvements:
1. Pre-validation before fetching transactions:
    - Currently, the API returns an empty list when fetching the last 10 transactions for an invalid account number. Implementing a pre-validation step to check if the account exists will provide more meaningful responses or error messages.

2. Test cases for Controller
