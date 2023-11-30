package ghumyang.interfaces;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
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

            Global.loadMarketInfo();

             // hard coded options
            Global.clearScreen();
            System.out.println("Welcome to the Market Update Page!");
            System.out.println("From here you can update general info about the DB Market");
            System.out.println();
            Global.printMarketInfo();
            System.out.println("Options:");
            System.out.println("   (0) Open/Close Market");
            System.out.println("   (1) Set a Stock Price");
            System.out.println("   (2) Set New Date");
            System.out.println("   (e) Exit to main menu / Log Out");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","e"))); // get input

            switch (input) {
                case "0":
                    openCloseMarket();
                    break;
                case "1":
                    setStockPriceWithSymbol();
                    break;
                case "2":
                    setNewDate();
                    break;
            }
        }
    }

static void openCloseMarket() throws IOException {

        // uses existing is_open status to switch
        int switchedValue = -1;
        String message = "";
        if (Global.MARKET_IS_OPEN) {
            message = "Market is now CLOSED";
            switchedValue = 0;

            // update all stocks closing prices to be their current price
            updateClosingPricesForAllStocks();

        } else {
            message = "Market is now OPEN";
            switchedValue = 1;
        }

        // sql query replaces value with 0 or 1
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE openclose O SET is_open = %s", 
                        String.valueOf(switchedValue)
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: openCloseMarket");
            System.exit(1);
        }

        // message displays info based on new status, but global java var will only update when function returns
        Global.messageWithConfirm(message);
    }

    static void updateClosingPricesForAllStocks() {

        // uses current price and records it as the closing price for the current market date.
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "MERGE INTO stockclosingprices C\n" + //
                            "USING (SELECT symbol,current_price FROM stocks) S\n" + //
                            "ON (C.symbol=S.symbol AND C.record_date = (SELECT market_date FROM marketdate))\n" + //
                            "WHEN MATCHED THEN UPDATE SET C.price = S.current_price\n" + //
                            "WHEN NOT MATCHED THEN INSERT (C.symbol, C.price, C.record_date)\n" + //
                            "VALUES (S.symbol, S.current_price, (SELECT market_date FROM marketdate))"
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: updateClosingPricesForAllStocks");
            System.exit(1);
        }
    }

    static void setStockPriceWithSymbol() throws IOException {
        String title = "new Stock Price";

        // prompt for stock symbol
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Symbol","New Price")));
        
        // input validation
        if (!Global.isDouble(fields.get("New Price"))) {
            Global.messageWithConfirm("ERROR: inputted price is invalid, should be a DECIMAL NUMBER");
            return;
        }
        if (fields.get("Symbol").equals("")) {
            Global.messageWithConfirm("ERROR: symbol is empty");
            return;
        }

        String symbol = fields.get("Symbol");
        Double newPrice = Double.parseDouble(fields.get("New Price"));

        // stock price cannot be negative or 0
        if (newPrice <= 0) {
            Global.messageWithConfirm("ERROR: stock price cannot be negative or 0");
            return;
        }

        // make sure that the stock exists
        if (!Global.theStockExists(symbol)) {
            Global.messageWithConfirm("ERROR: there is no stock with the symbol " + symbol);
            return;
        }

        // only update if it exists
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE stocks S SET S.current_price = %s WHERE S.symbol = '%s'", 
                        String.valueOf(newPrice), symbol
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: setStockPriceWithSymbol 2");
            System.exit(1);
        }

        Global.messageWithConfirm(String.format("Price of " + symbol + " has been updated to %1.2f", newPrice));
    }

    static void setNewDate() throws IOException {
        String title = "your new Date";

        // prompt for new date values
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Year (yyyy)","Month (mm)","Day (dd)")));

        // date validation from https://stackoverflow.com/questions/226910/how-to-sanity-check-a-date-in-java
        String dateString = fields.get("Day (dd)")+"/"+fields.get("Month (mm)")+"/"+fields.get("Year (yyyy)");
        String message = "";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern ( "dd/MM/uuuu" );
        try {
            LocalDate parsedDate = LocalDate.parse (dateString, dateTimeFormatter);
            message = "Date updated to " + parsedDate.toString();
        } catch ( DateTimeParseException e ) {
            Global.messageWithConfirm("ERROR: inputted date is invalid");
            return;
        }

        // ensure we can never return to the past
        LocalDate parsedDate = LocalDate.parse (dateString, dateTimeFormatter);
        if (Global.CURRENT_DATE.after(Date.valueOf(parsedDate))) {
            Global.messageWithConfirm("ERROR: can't travel to the past unfortunately");
            return;
        }

        // upload date as string and use sql to parse to date
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE marketdate SET market_date = TO_DATE ('%s', 'DD/MM/YYYY')", 
                        dateString
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: setNewDate");
            System.exit(1);
        }

        // message displays info based on new status, but global java var will only update when function returns
        Global.messageWithConfirm(message);

    }
    
}