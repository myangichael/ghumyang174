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

    // query has no constraints, we assume all caught in application
    public void deposit(double amount) {
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
    }
    
    // query has no constraints, we assume all caught in application
    public void withdrawal(double amount) {
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
    }

    public void buyStock(String ticker, int count) {

    }

    public void sellStock(String ticker, int count) {

    }

    public void cancelTransaction(int transactionId) {

    }
    
}
