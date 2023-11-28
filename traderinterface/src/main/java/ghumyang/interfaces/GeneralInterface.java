package ghumyang.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import ghumyang.Global;

public class GeneralInterface {
    public static void GeneralQueryPage() throws IOException {

        String input = "start";

        while (!input.equals("e")) {

             // hard coded options
            Global.clearScreen();
            System.out.println("Welcome to General Queries");
            System.out.println();
            System.out.println("Options:");
            System.out.println("   (0) Get info about a stock with stock ticker");
            System.out.println("   (1) Get info about a stock with its actor info");
            System.out.println("   (2) Get info about a movie");
            System.out.println("   (3) Get reviews for a specific movie");
            System.out.println("   (4) Get a list of top rated movies");
            System.out.println("   (e) Exit to main menu");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","4","e"))); // get input
        }
        
    }
}
