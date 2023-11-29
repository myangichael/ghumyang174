package ghumyang.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

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

            switch (input) {
                case "0":
                    getStockInfoWithTicker();
                    break;
                case "1":
                    getStockInfoWithActorName();
                    break;
                case "2":
                    getMovieInfo();
                    break;
                case "3":
                    getMovieReviews();
                    break;
                case "4":
                    getTopMovies();
                    break;
            }
        }
    }

    static void getStockInfoWithTicker() throws IOException {
        String title = "Stock Ticker Query";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Ticker")));
        if (fields.get("Ticker").equals("")) {
            Global.messageWithConfirm("ERROR: Ticker is empty");
            return;
        }
    }

    static void getStockInfoWithActorName() throws IOException {
        String title = "Stock Actor Name Query";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Actor Name")));
        if (fields.get("Actor Name").equals("")) {
            Global.messageWithConfirm("ERROR: Actor Name is empty");
            return;
        }
    }

    static void getMovieInfo() throws IOException {
        String title = "Movie Info Query";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Movie Title","Year")));
        if (!Global.isInteger(fields.get("Year"))) {
            Global.messageWithConfirm("ERROR: inputted year is invalid, should be an INTEGER");
            return;
        }
        if (fields.get("Movie Title").equals("")) {
            Global.messageWithConfirm("ERROR: Movie Title is empty");
            return;
        }
        // TODO
    }

    static void getMovieReviews() throws IOException {
        String title = "Movie Review Query";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Movie Title","Year")));
        if (!Global.isInteger(fields.get("Year"))) {
            Global.messageWithConfirm("ERROR: inputted year is invalid, should be an INTEGER");
            return;
        }
        if (fields.get("Movie Title").equals("")) {
            Global.messageWithConfirm("ERROR: Movie Title is empty");
            return;
        }
        // TODO
    }

    static void getTopMovies() throws IOException {

    }

}
