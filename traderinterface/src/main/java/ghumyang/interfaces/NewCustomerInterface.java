package ghumyang.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ghumyang.Global;

public class NewCustomerInterface {
    public static void Register() throws IOException {
        String title = "Register";
        String[] fieldsList = new String[] {"Name","State","Phone Number","Email","TaxID","Username","Password"};
        HashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList(fieldsList)));
        Global.confirmInfo(title, fields);
        Global.awaitConfirmation();

        ArrayList<String> errorMessages = new ArrayList<>();
        
        if (!fields.get("State").matches("^[A-Z][A-Z]$")) {
            errorMessages.add("enter state as 2 letter initial i.e. \"CA\"");
        }
        if (!fields.get("Phone Number").matches("^\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d$")) {
            errorMessages.add("enter phone number as 10 digits i.e. \"1234567890\"");
        }
        if (!fields.get("Email").matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$")) {
            errorMessages.add("enter email as \"a@b.c\"");
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

/*
    @Getter String name;
    @Getter String state;
    @Getter String phoneNumber;
    @Getter String emailAddress;
    @Getter String taxID;
    @Getter String username;
    @Getter String password;
    @Getter int customerId;
 */