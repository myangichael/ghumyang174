package ghumyang.interfaces;

import java.io.IOException;

import ghumyang.Global;
import ghumyang.tables.Manager;

public class ManagerInterface {
    public static void Login() throws IOException {

        String[] loginInfo = Global.getLogin(); // prompt user for login info

        if (!Manager.checkLogin(loginInfo[0], loginInfo[1])) { // check login validity for customer
            Global.clearScreen();
            System.out.println();
            System.out.println("---INVALID LOGIN INFO---"); // if invalid login info jump back
            Global.awaitConfirmation();
            return;
        }
        // ManagerAccountPage(customer);
    }

}
