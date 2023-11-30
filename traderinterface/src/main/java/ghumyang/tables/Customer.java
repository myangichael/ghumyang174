package ghumyang.tables;
import lombok.Getter;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;

import ghumyang.Global;

public class Customer {

    @Getter String name;
    @Getter String state;
    @Getter String phone_number;
    @Getter String email_address;
    @Getter String tax_id;
    @Getter String username;
    @Getter String password;
    @Getter int customer_id;
    @Getter double balance;

    public Customer(String username, String password) {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM customers C WHERE C.username = '%s' AND C.password = '%s'", 
                        username, password
                    )
                )
            ) {
                resultSet.next();
                this.name = resultSet.getString("name");
                this.state = resultSet.getString("state");
                this.phone_number = resultSet.getString("phone_number");
                this.email_address = resultSet.getString("email_address");
                this.tax_id = resultSet.getString("tax_id");
                this.username = resultSet.getString("username");
                this.password = resultSet.getString("password");
                this.customer_id = Integer.parseInt(resultSet.getString("customer_id"));
                this.balance = Double.parseDouble(resultSet.getString("balance"));
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: declare Customer");
            System.exit(1);
        }
    }

    public static boolean isThereUserWithThisLogin(String username, String password) throws IOException {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM customers C WHERE C.username = '%s' AND C.password = '%s'", 
                        username, password
                    )
                )
            ) {
                if (!resultSet.next()) {
                    // no users with this information
                    return false;
                } else {
                    // at least one user with this username and password
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: isThereUserWithThisLogin");
            System.exit(1);
        }
        return true;
    }

    public void updateBalanceHistory() throws IOException {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "MERGE INTO balancehistories B USING (SELECT customer_id, balance FROM customers WHERE customer_id = %s) S ON (B.customer_id = S.customer_id AND B.record_date=TO_DATE('%s', 'YYYY-MM-DD')) WHEN MATCHED THEN UPDATE SET B.balance = S.balance WHEN NOT MATCHED THEN INSERT (B.customer_id,B.balance,B.record_date) VALUES (S.customer_id,S.balance,TO_DATE('%s', 'YYYY-MM-DD'))", 
                        customer_id, Global.CURRENT_DATE.toString(),Global.CURRENT_DATE.toString()
                    )
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: updateBalanceHistory");
            System.exit(1);
        }
    }

    // query has no constraints, we assume all caught in application
    public void deposit(double amount, boolean shouldCreateTransaction) {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE customers C SET balance = balance + %s WHERE C.username = '%s'", 
                        String.valueOf(amount), username
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: deposit");
            System.exit(1);
        }
        if (shouldCreateTransaction) {
            createDepositWithdrawalTransaction(amount);
        }
    }
    
    // query has no constraints, we assume all caught in application
    public void withdrawal(double amount, boolean shouldCreateTransaction) {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE customers C SET balance = balance - %s WHERE C.username = '%s'", 
                        String.valueOf(amount), username
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: withdrawal");
            System.exit(1);
        }
        if (shouldCreateTransaction) {
            // treat it as a negative deposit
            createDepositWithdrawalTransaction(-1*amount);
        }
    }

    // helper function, only called if a deposit or a withdrawal is an actual transaction
    void createDepositWithdrawalTransaction(double amount) {

        // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to depositwithdraw table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO depositwithdrawal (transaction_id, amount) VALUES (%d, %s)", 
                        createTransactionAndReturnItsID(), String.valueOf(amount)
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: createDepositWithdrawalTransaction 1");
            System.exit(1);
        }
    }
    
    // helper function used for every transaction
    int createTransactionAndReturnItsID() {

        // adds a new transaction into transactions table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO transactions (customer_id,transaction_date) VALUES (%s, TO_DATE('%s', 'YYYY-MM-DD'))", 
                        String.valueOf(customer_id), Global.CURRENT_DATE.toString()
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: createTransactionAndReturnItsID 1");
            System.exit(1);
        }

        // queries for and returns its ID
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                "SELECT MAX(C.transaction_id) AS id FROM transactions C"
                )
            ) {
                if (resultSet.next()) {
                    return Integer.parseInt(resultSet.getString("id"));
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: createTransactionAndReturnItsID 2");
            System.exit(1);
        }

        return -1;
    }

    /*
     * DESIGN: 
     * query stock for price
     * withdraw that price*shares from the account
     * create transaction in buys
     * create stock account if needed otherwise update
     */
    public void buyStock(String symbol, int count) throws IOException {

        // ensure stock exists
        if (!Global.theStockExists(symbol)) {
            Global.messageWithConfirm("ERROR: there is no stock with the symbol " + symbol);
            return;
        }
        
        // query for the current stockprice
        double stockPrice = Global.getCurrentStockPrice(symbol);
        double cost = stockPrice * count + 20;

        Global.messageWithConfirm(
            new String[] {
                String.format("You are attempting to buy " + count + " shares of " + symbol + " at %1.2f per share", stockPrice),
                String.format("Total price will be %1.2f after $20 commission", cost),
                "",
                "To cancel, exit the program"
            }
        );

        // ensure that there is enough money to make this purchase
        if (cost > balance) {
            Global.messageWithConfirm("ERROR: not enough balance to make this purchase");
            return;
        }

        int transactionId = createTransactionAndReturnItsID();

        // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to buys table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO buys (transaction_id, symbol, purchase_price, num_shares) VALUES (%d, '%s', %1.2f, %d)", 
                        transactionId, symbol, stockPrice, count
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: buyStock create buy relation");
            System.exit(1);
        }

        // create stock account
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "MERGE INTO stockaccounts S\n" + //
                                "USING (SELECT customer_id FROM customers WHERE customer_id = %d) C\n" + //
                                "ON (S.customer_id = C.customer_id AND S.symbol = '%s' AND S.buy_price = %1.2f)\n" + //
                                "WHEN MATCHED THEN UPDATE SET S.num_shares = S.num_shares + %d\n" + //
                                "WHEN NOT MATCHED THEN INSERT (S.customer_id, S.symbol, S.num_shares, S.buy_price) VALUES (C.customer_id, '%s', %d, %1.2f)",
                        customer_id, symbol, stockPrice, count, symbol, count, stockPrice
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: buyStock upsert stock account");
            System.exit(1);
        }

        // remove cost from account
        withdrawal(cost, false);

    }

    public void sellStock(String ticker, int count) {

    }

    public void cancelTransaction(int transactionId) {

    }
    
}
