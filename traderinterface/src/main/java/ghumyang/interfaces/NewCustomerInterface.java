package ghumyang.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import ghumyang.Global;

public class NewCustomerInterface {
    public static void Register() throws IOException {
        String title = "your New User";
        String[] fieldsList = new String[] {"Name","State","Phone Number","Email","TaxID","Username","Password"};
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList(fieldsList)));
        Global.confirmInfo(title, fields);
        Global.awaitConfirmation();

        ArrayList<String> errorMessages = new ArrayList<>();
        boolean placeHolder = false;
        
        if (!fields.get("Name").matches("[^\\s-]")) {
            errorMessages.add("Name should not be empty");
        }
        if (!fields.get("State").matches("^[A-Z][A-Z]$")) {
            errorMessages.add("State should be in 2 letter abbreviated form, example: \"CA\"");
        }
        if (!fields.get("Phone Number").matches("^\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d$")) {
            errorMessages.add("Phone Number should be a 10 digit string, example: \"1234567890\"");
        }
        // regex for email found at: https://stackoverflow.com/questions/201323/how-can-i-validate-an-email-address-using-a-regular-expression
        if (!fields.get("Email").matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
            errorMessages.add("Email should be of form \"a@b.c\"");
        }
        if (!fields.get("TaxID").matches("^\\d\\d\\d\\d\\d\\d\\d\\d\\d$")) {
            errorMessages.add("TaxID should be a 9 digit number, example: \"123456789\"");
        }
        if (!placeHolder) {
            errorMessages.add("this username is not unique");
        }
        if (!fields.get("Password").matches("[^\\s-]")) {
            errorMessages.add("Password should not be empty");
        }
        
        if (errorMessages.size() > 0) {
            Global.clearScreen();
            System.out.println("Account was not successfully created. Errors below:");
            System.out.println();
            for (String message : errorMessages) {
                System.out.println(message);
            }
        }

        Global.awaitConfirmation();
    }
}