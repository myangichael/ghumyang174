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

    // constructor acts as a correct login, uses unique login info to query table for this customer
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
            // treat it as a negative deposit transaction in history
            createDepositWithdrawalTransaction(-1*amount);
        }
    }

    // helper function, only called if a deposit or a withdrawal is an actual transaction
    // goal is to use deposit/withdrawal to as helper methods to update balance, but only actual deposit/withdrawal actions should call this function to add a record
    void createDepositWithdrawalTransaction(double amount) {

        // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to depositwithdraw table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO depositwithdrawal (transaction_id, amount) VALUES (%d, %s)", 
                        Global.createTransactionAndReturnItsID(customer_id), String.valueOf(amount)
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: createDepositWithdrawalTransaction 1");
            System.exit(1);
        }
    }

    /*
     * DESIGN: 
     * query stock for price
     * withdraw that price*shares from the account +20 for commission
     * create transaction in buys
     * create stock account if needed otherwise update existing
     */
    public void buyStock(String symbol, double count) throws IOException {

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

        int transactionId = Global.createTransactionAndReturnItsID(customer_id);

        // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to buys table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO buys (transaction_id, symbol, purchase_price, num_shares) VALUES (%d, '%s', %1.2f, %1.2f)", 
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
                                "WHEN MATCHED THEN UPDATE SET S.num_shares = S.num_shares + %1.2f\n" + //
                                "WHEN NOT MATCHED THEN INSERT (S.customer_id, S.symbol, S.num_shares, S.buy_price) VALUES (C.customer_id, '%s', %1.2f, %1.2f)",
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

    /*
     * DESIGN: 
     * query stock for price
     * check if there is a stockaccount with the specificed buy price, symbol, enough shares
     * update stock account (remove shares), if shares = 0 delete stock account
     * deposit that price*shares from the account -20 commission
     * create transaction in buys
     */
    public void sellStock(String symbol, double count, double purchasedPrice) throws IOException {

        // ensure stock exists
        if (!Global.theStockExists(symbol)) {
            Global.messageWithConfirm("ERROR: there is no stock with the symbol " + symbol);
            return;
        }
        
        // query for the current stockprice
        double stockPrice = Global.getCurrentStockPrice(symbol);
        double gain = stockPrice * count - 20;

        Global.messageWithConfirm(
            new String[] {
                String.format("You are attempting to sell " + count + " shares of " + symbol + " at %1.2f per share", stockPrice),
                String.format("You will receive %1.2f after $20 commission", gain),
                "",
                "To cancel, exit the program"
            }
        );

        // ensure that there is enough money to pay commission
        if (20 > balance) {
            Global.messageWithConfirm("ERROR: not enough balance to pay $20 commission");
            return;
        }

        // checks stock account to make sure you're good for it
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT *\n" + //
                        "FROM stockaccounts A\n" + //
                        "WHERE A.customer_id = %d AND A.symbol = '%s' AND A.buy_price = %1.2f AND A.num_shares >= %1.2f",
                        customer_id, symbol, purchasedPrice, count
                        )
                )
            ) {
                if (!resultSet.next()) {
                    Global.messageWithConfirm(
                        String.format(
                            "ERROR: You don't own " + count + " shares of " + symbol + " purchased at %1.2f",
                            purchasedPrice
                        )
                    );
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock check stockaccount");
            System.exit(1);
        }

        // update stockAccount info
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE stockaccounts S\n" + //
                        "SET S.num_shares = S.num_shares - %1.2f\n" + //
                        "WHERE S.customer_id = %d AND S.symbol = '%s' AND S.buy_price = %1.2f",
                        count, customer_id, symbol, purchasedPrice
                    )
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock update stockaccount");
            System.exit(1);
        }

        // clear all stockAccounts where row has 0 shares bought
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "DELETE FROM stockaccounts WHERE num_shares = 0"
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock delete empty stockaccounts");
            System.exit(1);
        }

        int transactionId = Global.createTransactionAndReturnItsID(customer_id);

        // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to sells table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO sells (transaction_id, symbol, purchase_price, sell_price, num_shares) VALUES (%d, '%s', %1.2f, %1.2f, %1.2f)", 
                        transactionId, symbol, purchasedPrice, stockPrice, count
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock create sell relation");
            System.exit(1);
        }

        // add cash to balance
        deposit(gain, false);

    }

    /*
     * DESIGN:
     * check enough balance for commission
     * gets the most recent transaction, checks to make sure it is a buy/sell
     *     by extension can't cancel two transactions back to back, you can only cancel immediate prior
     * call cancelBuy() or cancelSell() accordingly
     * creates record in transactions (this also means the most recent transaction will be a cancel now, hindering you from canceling again in a row)
     */
    public void cancelTransaction() throws IOException {
        
        // ensure enough to pay commission
        if (20 > balance) {
            Global.messageWithConfirm("ERROR: not enough balance to make this cancellation");
            return;
        }
        
        String lastTransaction = "";
        String canceledTransactionId = "";
        String maxBuyID = "", maxSellID = "";
        try (Statement statement = Global.SQL.createStatement()) {
            // get the latest transaction id
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT MAX(T.transaction_id) AS maxID FROM transactions T"
                    )
                )
            ) {
                if (resultSet.next()) {
                    lastTransaction = resultSet.getString("maxID");

                    // query could return null (there are no transactions in the table), exit
                    if (lastTransaction == null) {
                        Global.messageWithConfirm("ERROR: no recorded transactions");
                        return;
                    }
                    
                } else {

                    // can never be reached unless serious issue
                    Global.messageWithConfirm("ERROR: no recorded transactions");
                    return;
                }
            }

            // get the latest buy transaction id
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT MAX(B.transaction_id) AS maxID FROM buys B"
                    )
                )
            ) {
                if (resultSet.next()) {
                    maxBuyID = resultSet.getString("maxID");
                } else {
                    // Do nothing, since it might be sell/nothing
                }
            }

            // get the latest sell transaction id
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT MAX(S.transaction_id) AS maxID FROM sells S"
                    )
                )
            ) {
                if (resultSet.next()) {
                    maxSellID = resultSet.getString("maxID");
                } else {
                    // Do nothing, since it might be nothing
                }
            }

            // check if our buyid and sellid are null
            if (maxBuyID == null && maxSellID == null) {
                Global.messageWithConfirm("ERROR: no prior recorded buys or sells");
                return;
            }

            // we check whichiever matches
            if (lastTransaction.equals(maxBuyID)) {

                // buy matches
                cancelBuy(lastTransaction);
                canceledTransactionId = maxBuyID;

            } else if (lastTransaction.equals(maxSellID)) {

                // sell matches
                cancelSell(lastTransaction);
                canceledTransactionId = maxSellID;

            } else {

                // no match
                Global.messageWithConfirm("Last transaction was not a buy or sell");
                return;
            }

        } catch (Exception e) {
            System.out.println("FAILED QUERY: cancelTransaction");
            System.exit(1);
        }

        // create cancel transaction entry in that table
        int newTransactionId = Global.createTransactionAndReturnItsID(customer_id);
        // add to cancel table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO cancels (transaction_id, transaction_canceled) VALUES (%d, %d)",
                        newTransactionId, canceledTransactionId
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: cancelBuy insert into cancels table");
            System.exit(1);
        }


    }
    
    /*
     * DESIGN:
     * pulls buy information
     * removes shares in stockAccount (sells the specified number of shares)
     * deletes stockAccount if num_shares = 0
     * deposits back num_shares*purchase_price
     */
    void cancelBuy(String transactionID) throws IOException {
        String symbol = "";
        double purchase_price = 0.0, num_shares = 0.0;

        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM buys B WHERE B.transaction_id=%s",
                        transactionID
                    )
                )
            ) {
                if (resultSet.next()) {
                    symbol = resultSet.getString("symbol");
                    purchase_price = resultSet.getDouble("purchase_price");
                    num_shares = resultSet.getDouble("num_shares");
                } else {
                    // Do nothing, guaranteed to exist when in cancelBuy
                }
            }

        } catch (Exception e) {
            System.out.println("FAILED QUERY: cancelBuy query buy info");
            System.exit(1);
        }

        // update stockAccount info
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE stockaccounts S\n" + //
                        "SET S.num_shares = S.num_shares - %1.2f\n" + //
                        "WHERE S.customer_id = %d AND S.symbol = '%s' AND S.buy_price = %1.2f",
                        num_shares, customer_id, symbol, purchase_price
                    )
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: cancelBuy update stockAccount");
            System.exit(1);
        }

        // clear all stockAccounts where row has 0 shares bought
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "DELETE FROM stockaccounts WHERE num_shares = 0"
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock delete empty stockaccounts");
            System.exit(1);
        }

        // refund money from transaction -20 for cancel transaction
        deposit((num_shares * purchase_price) - 20, false);

        Global.messageWithConfirm("Your most recent buy transaction has been canceled with $20 commission fee");

    }

    /*
     * DESIGN:
     * pulls sell information
     * adds shares back to stockAccount
     *     if needed, re-creates the stockAccount (if it were removed by the initial sell)
     *     otherwise just updates num_shares
     * withdraws num_shares*purchase_price and another 20 for commission
     *     will never go negative, we check that there is $20 in the balance before even allowing a cancel
     */
    void cancelSell(String transactionID) throws IOException {
        String symbol = "";
        double purchase_price = 0.0, sell_price = 0.0, num_shares = 0.0;

        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM sells S WHERE S.transaction_id=%s",
                        transactionID
                    )
                )
            ) {
                if (resultSet.next()) {
                    symbol = resultSet.getString("symbol");
                    purchase_price = resultSet.getDouble("purchase_price");
                    sell_price = resultSet.getDouble("sell_price");
                    num_shares = resultSet.getDouble("num_shares");
                } else {
                    // Do nothing, guaranteed to exist when in cancelSell
                }
            }

        } catch (Exception e) {
            System.out.println("FAILED QUERY: cancelSell query buy info");
            System.exit(1);
        }

        // update stockAccount info // use purchase_price to update the stock account
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "MERGE INTO stockaccounts S\n" + //
                                "USING (SELECT customer_id FROM customers WHERE customer_id = %d) C\n" + //
                                "ON (S.customer_id = C.customer_id AND S.symbol = '%s' AND S.buy_price = %1.2f)\n" + //
                                "WHEN MATCHED THEN UPDATE SET S.num_shares = S.num_shares + %1.2f\n" + //
                                "WHEN NOT MATCHED THEN INSERT (S.customer_id, S.symbol, S.num_shares, S.buy_price) VALUES (C.customer_id, '%s', %1.2f, %1.2f)",
                        customer_id, symbol, purchase_price, num_shares, symbol, num_shares, purchase_price
                    )
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: cancelBuy update stockAccount");
            System.exit(1);
        }

        // takes money gained from the canceled sell + 20 for commission
        withdrawal((sell_price * num_shares) + 20, false);
        
        Global.messageWithConfirm("Your most recent sell transaction has been canceled with $20 commission fee");
    }
}
