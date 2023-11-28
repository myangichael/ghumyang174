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
            System.out.println("Submitted Login Info is Invalid :("); // if invalid login info jump back
            Global.awaitConfirmation();
            return;
        }
        // ManagerAccountPage(customer);
        Manager manager = new Manager(loginInfo[0], loginInfo[1]); // if valid login info continue to account page
        ManagerAccountPage(manager);
    }

    static void ManagerAccountPage(Manager manager) {

    }

}
