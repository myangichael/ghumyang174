package ghumyang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;

public class Global {

    // Input Reader for Global use
    public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    // Date and Market Open variables
    public static Date date = Date.valueOf("2000-1-1");
    public static boolean marketOpen = false;

    // Prompts user input, forces user to input one of the Strings listed in validInputs or calls for input again, then returns that input
    public static String getLineSetInputs(ArrayList<String> validInputs) throws IOException {
        String rawInput = br.readLine();
        if (!validInputs.contains(rawInput)) {
            System.out.println("--- INVALID INPUT ---");
            return getLineSetInputs(validInputs);
        }
        return rawInput;
    }

    // Prompts user for username and password, then returns String[] {username, password}
    public static String[] getLogin() throws IOException {
        Global.clearScreen();
        System.out.println();
        System.out.println("enter username:");
        String username = br.readLine();
        System.out.println();
        System.out.println("enter password:");
        String password = br.readLine();
        return new String[]{username, password};
    }

    // Small helper to clear all inputs
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    }
    
    public static void awaitConfirmation() throws IOException {
        System.out.println("enter anything to head back");
        br.readLine();
    }
    
    
}
