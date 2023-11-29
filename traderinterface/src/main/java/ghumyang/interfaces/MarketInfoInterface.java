package ghumyang.interfaces;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import ghumyang.Global;

public class MarketInfoInterface {

    public static void MarketUpdatePage() throws IOException {

        String input = "start";

        while (!input.equals("e")) {

             // hard coded options
            Global.clearScreen();
            System.out.println("Welcome to the Market Update Page!");
            System.out.println("From here you can update general info about the DB Market");
            System.out.println();
            System.out.println("Options:");
            System.out.println("   (0) Open/Close Market");
            System.out.println("   (1) Set Stock Price with Stock Ticker");
            System.out.println("   (2) Set Stock Price with Actor Name");
            System.out.println("   (3) Set New Date");
            System.out.println("   (e) Exit to main menu / Log Out");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","e"))); // get input

            switch (input) {
                case "0":
                    openCloseMarket();
                    break;
                case "1":
                    setStockPriceWithTicker();
                    break;
                case "2":
                    setStockPriceWithActorName();
                    break;
                case "3":
                    setNewDate();
                    break;
            }
        }
    }

    static void openCloseMarket() throws IOException {
        if (Global.MARKET_IS_OPEN) {
            Global.messageWithConfirm("Market is now CLOSED");
            // TODO: set false in DB
        } else {
            Global.messageWithConfirm("Market is now OPEN");
            // TOOD: set true in DB
        }
    }

    static void setStockPriceWithTicker() throws IOException {
        String title = "new Stock Price";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Ticker","New Price")));
        if (!Global.isDouble(fields.get("New Price"))) {
            Global.messageWithConfirm("ERROR: inputted price is invalid, should be a DECIMAL NUMBER");
            return;
        }
        if (fields.get("Ticker").equals("")) {
            Global.messageWithConfirm("Ticker is empty");
            return;
        }
    }

    static void setStockPriceWithActorName() throws IOException {
        String title = "new Stock Price";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Actor Name","New Price")));
        if (!Global.isDouble(fields.get("New Price"))) {
            Global.messageWithConfirm("ERROR: inputted price is invalid, should be a DECIMAL NUMBER");
            return;
        }
        if (fields.get("Actor Name").equals("")) {
            Global.messageWithConfirm("ERROR: Actor Name is empty");
            return;
        }
    }

    static void setNewDate() throws IOException {
        String title = "your new Date";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Year (yyyy)","Month (mm)","Day (dd)")));

        // Date validation from https://stackoverflow.com/questions/226910/how-to-sanity-check-a-date-in-java
        String dateString = fields.get("Day (dd)")+"/"+fields.get("Month (mm)")+"/"+fields.get("Year (yyyy)");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern ( "dd/MM/uuuu" );
        try {
            LocalDate parsedDate = LocalDate.parse (dateString, dateTimeFormatter);
            Global.messageWithConfirm("Date updated to " + parsedDate.toString());
        } catch ( DateTimeParseException e ) {
            Global.messageWithConfirm("ERROR: inputted date is invalid");
            return;
        }

        Date.valueOf(LocalDate.parse(dateString, dateTimeFormatter));

    }
    

}
