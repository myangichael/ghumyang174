package ghumyang.interfaces;

import java.io.IOException;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;

import ghumyang.Global;

public class NewCustomerInterface {

    static HashSet<String> STATES = new HashSet<>(Arrays.asList(new String[] {"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"}));

    public static void Register() throws IOException, SQLException {
        String title = "your New User";
        String[] fieldsList = new String[] {"name","state","phone_number","email_address","tax_id","username","password"};
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList(fieldsList)));

        ArrayList<String> errorMessages = new ArrayList<>();

        String name = fields.get("name");
        String state = fields.get("state");
        String phone_number = fields.get("phone_number");
        String email_address = fields.get("email_address");
        String tax_id = fields.get("tax_id");
        String username = fields.get("username");
        String password = fields.get("password");
        
        if (name.equals("")) {
            errorMessages.add("name should not be empty");
        }
        if (!STATES.contains(state)) {
            errorMessages.add("state should be in 2 capital letter abbreviated form, example: \"CA\"");
        }
        if (!phone_number.matches("^\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d$")) {
            errorMessages.add("phone_number should be a 10 digit string, example: \"1234567890\"");
        }
        // regex for email found at: https://stackoverflow.com/questions/201323/how-can-i-validate-an-email-address-using-a-regular-expression
        if (!email_address.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
            errorMessages.add("email_address should be of form \"a@b.c\"");
        }
        if (!tax_id.matches("^\\d\\d\\d\\d\\d\\d\\d\\d\\d$")) {
            errorMessages.add("tax_id should be a 9 digit string, example: \"123456789\"");
        }
        if (usernameAlreadyExists(username)) {
            errorMessages.add("username has to be unique");
        }
        if (username.equals("")) {
            errorMessages.add("username cannot be empty");
        }
        if (password.equals("")) {
            errorMessages.add("password cannot be empty");
        }

        if (errorMessages.size() > 0) {
            Global.clearScreen();
            System.out.println("Account was NOT successfully created. Errors below:");
            System.out.println();
            for (String message : errorMessages) {
                System.out.println(message);
            }
            Global.awaitConfirmation();
            return;
        } else {
            addUser(username, password, name, state, phone_number, email_address, tax_id);
        }
        System.out.println("user added");
        Global.awaitConfirmation();
    }

    static boolean usernameAlreadyExists(String username) throws SQLException {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format("SELECT * FROM Customers C WHERE C.username = '%s'", username)
                )
            ) {
                if (!resultSet.next()) {
                    // no users with this username
                    return false;
                } else {
                    // at least one user with this username
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: usernameAlreadyExists");
            System.exit(1);
        }
        return true;
    }

    static void addUser(String username, String password, String name, String state, String phone_number, String email_address, String tax_id) throws SQLException {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO Customers (username,password,name,state,phone_number,email_address,tax_id) VALUES ('%s','%s','%s','%s','%s','%s','%s')",
                        username, password, name, state, phone_number, email_address, tax_id
                    )
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: addUser");
            System.exit(1);
        }
    }

}