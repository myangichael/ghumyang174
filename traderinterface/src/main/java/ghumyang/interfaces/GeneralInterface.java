package ghumyang.interfaces;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
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
            System.out.println("   (0) Get info about a stock");
            System.out.println("   (1) Get info about a movie");
            System.out.println("   (2) Get reviews for a specific movie");
            System.out.println("   (3) Get a list of top rated movies");
            System.out.println("   (e) Exit to main menu");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","e"))); // get input

            switch (input) {
                case "0":
                    getStockInfoWithSymbol();
                    break;
                case "1":
                    getMovieInfo();
                    break;
                case "2":
                    getMovieReviews();
                    break;
                case "3":
                    getTopMovies();
                    break;
            }
        }
    }

    // TODO: NEED FIX
    static void getStockInfoWithSymbol() throws IOException {
        String title = "Stock Ticker Query";

        // prompt for stock symbol
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Symbol")));
        
        // input validation
        if (fields.get("Symbol").equals("")) {
            Global.messageWithConfirm("ERROR: inputted symbol is empty");
            return;
        }

        String symbol = fields.get("Symbol");

        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM stocks S WHERE S.symbol = '%s'", 
                        symbol
                    )
                )
            ) {
                if (!resultSet.next()) {
                    // no stock ticker with this information
                    Global.messageWithConfirm("ERROR: there is no stock with the ticker " + symbol);
                } else {
                    // stock exists
                    String[] messages = new String[] {
                        "Stock Found!",
                        "",
                        "symbol        | " + resultSet.getString("symbol"),
                        "actor name    | " + resultSet.getString("actor_name"),
                        "actor dob     | " + resultSet.getString("dob").substring(0,10),
                        "current price | " + resultSet.getString("current_price")
                    };
                    Global.messageWithConfirm(messages);
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: getStockInfoWithSymbol");
            System.exit(1);
        }
    }

    static void getMovieInfo() throws IOException {
        String title = "Movie Info Query";

        // prompt for unique movie info
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Movie Title","Year")));

        // input validation
        if (!Global.isInteger(fields.get("Year"))) {
            Global.messageWithConfirm("ERROR: inputted year is invalid, should be an INTEGER");
            return;
        }
        if (fields.get("Movie Title").equals("")) {
            Global.messageWithConfirm("ERROR: inputted movie title is empty");
            return;
        }

        String movieTitle = fields.get("Movie Title");
        String movieYear = fields.get("Year");

        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM movies M WHERE M.title = '%s' AND M.year = %s", 
                        movieTitle, movieYear
                    )
                )
            ) {
                if (!resultSet.next()) {
                    // no movies with this information
                    Global.messageWithConfirm("ERROR: there is no movie with this information");
                    return;
                } else {
                    // movie exists
                    String[] messages = new String[] {
                        "Movie Found!",
                        "",
                        "title  | " + resultSet.getString("title"),
                        "year   | " + resultSet.getString("year"),
                        "rating | " + resultSet.getString("rating")
                    };
                    Global.messageWithConfirm(messages);
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: getMovieInfo 1");
            System.exit(1);
        }

        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT C.symbol, S.actor_name, C.total_value, C.contract_role FROM movies M INNER JOIN moviecontracts C ON M.movie_id=C.movie_id INNER JOIN stocks S ON C.symbol=S.symbol WHERE M.title = '%s' AND M.year = %s", 
                        movieTitle, movieYear
                    )
                )
            ) {
            // store headers for output
                ArrayList<String> headers = new ArrayList<>();
                headers.add("Actor Name");
                headers.add("Symbol");
                headers.add("Total Value");
                headers.add("Role");

                ArrayList<String> aName = new ArrayList<>();
                ArrayList<String> symbol = new ArrayList<>();
                ArrayList<String> tVal = new ArrayList<>();
                ArrayList<String> role = new ArrayList<>();

                // adding results to array for output process
                while (resultSet.next()) {
                    aName.add(resultSet.getString("actor_name"));
                    symbol.add(resultSet.getString("symbol"));
                    tVal.add(resultSet.getString("total_value"));
                    role.add(resultSet.getString("contract_role"));
                }

                if (aName.size() == 0) {
                    Global.messageWithConfirm("No contracts associated with "+ movieTitle + " released in " + movieYear);
                    return;
                }

                // create large array for output process
                ArrayList<ArrayList<String>> values = new ArrayList<>();
                values.add(aName);
                values.add(symbol);
                values.add(tVal);
                values.add(role);

                String[] output = Global.tableToString(headers, values);

                Global.messageWithConfirm("Contracts for " + movieTitle + " released in " + movieYear, output);
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: getMovieInfo 2");
            System.exit(1);
        }

        // TODO: ADD ALL CONTRACTS ASSOCIATED WITH THIS MOVIE

    }

    static void getMovieReviews() throws IOException {
        String title = "Movie Review Query";

        // prompt for unique movie info
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Movie Title","Year")));

        // input validation
        if (!Global.isInteger(fields.get("Year"))) {
            Global.messageWithConfirm("ERROR: inputted year is invalid, should be an INTEGER");
            return;
        }
        if (fields.get("Movie Title").equals("")) {
            Global.messageWithConfirm("ERROR: inputted movie title is empty");
            return;
        }

        String movieTitle = fields.get("Movie Title");
        String movieYear = fields.get("Year");

        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT R.review FROM movies M INNER JOIN moviereviews R ON M.movie_id = R.movie_id WHERE M.title = '%s' AND M.year = %s", 
                        movieTitle, movieYear
                    )
                )
            ) {
                // store reviews in list (don't know length)
                ArrayList<String> reviews = new ArrayList<>();

                reviews.add("Movie Found!");
                reviews.add("Reviews listed below:");
                reviews.add("");

                while (resultSet.next()) {
                    reviews.add(resultSet.getString("review"));
                }

                if (reviews.size() == 3) {
                    Global.messageWithConfirm("ERROR: there is no movie with this information, or no reviews associated with this movie");
                    return;
                }
                    
                // convert list to array
                String[] messages = new String[reviews.size()];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = reviews.get(i);
                }

                System.out.println("c");

                Global.messageWithConfirm(messages);
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: getMovieReviews");
            System.exit(1);
        }

    }

    static void getTopMovies() throws IOException {
        String title = "Top Movies Query";

        // prompt for time period
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Begin Year","End Year")));

        // input validation
        if (!Global.isInteger(fields.get("Begin Year"))) {
            Global.messageWithConfirm("ERROR: inputted begin year is invalid, should be an INTEGER");
            return;
        }
        if (!Global.isInteger(fields.get("End Year"))) {
            Global.messageWithConfirm("ERROR: inputted end year is invalid, should be an INTEGER");
            return;
        }

        int beginYear = Integer.parseInt(fields.get("Begin Year"));
        int endYear = Integer.parseInt(fields.get("End Year"));

        // make sure year period is valid
        if (beginYear > endYear) {
            Global.messageWithConfirm("ERROR: begin year cannot be after end year");
            return;
        }

        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT M.title, M.year, M.rating FROM movies M WHERE year >= %s AND year <= %s AND rating=10", 
                        beginYear, endYear
                    )
                )
            ) {

                // store headers for output
                ArrayList<String> headers = new ArrayList<>();
                headers.add("Title");
                headers.add("Year");
                headers.add("Rating");

                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> years = new ArrayList<>();
                ArrayList<String> ratings = new ArrayList<>();

                // adding results to array for output process
                while (resultSet.next()) {
                    titles.add(resultSet.getString("title"));
                    years.add(resultSet.getString("year"));
                    ratings.add(resultSet.getString("rating"));
                }

                if (titles.size() == 0) {
                    Global.messageWithConfirm("No movies found from "+beginYear + " to " + endYear);
                    return;
                }

                // create large array for output process
                ArrayList<ArrayList<String>> values = new ArrayList<>();
                values.add(titles);
                values.add(years);
                values.add(ratings);

                String[] output = Global.tableToString(headers, values);

                Global.messageWithConfirm("Top Movies from " + beginYear + " to " + endYear, output);

            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: getTopMovies");
            System.exit(1);
        }

    }

}
