package ghumyang;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.sql.DatabaseMetaData;

public class Global {

    // input Reader for Global use
    public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    // Date and Market Open variables
    public static Date CURRENT_DATE = Date.valueOf("2000-1-1");
    public static boolean MARKET_IS_OPEN = false;

    // login variables for DB connection
    static String DB_URL;
    static String DB_USER;
    static String DB_PASSWORD;
    
    // instantiating connection
    static Properties info;
    static OracleDataSource ods;
    
    // use connection to make all sql queries
    public static OracleConnection SQL;

    // function to pull password from file
    public static void getPassword() throws IOException {
        BufferedReader pReader = new BufferedReader(new FileReader(".login"));
        DB_URL = pReader.readLine();
        DB_USER = pReader.readLine();
        DB_PASSWORD = pReader.readLine();
        pReader.close();
    }

    // function to create connection on program run, copied from course tutorial
    public static void connection() throws SQLException {

        info = new Properties();

        System.out.println("starting connection...");
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");
        System.out.println("creating oracleDataSource...");

        ods = new OracleDataSource();
        ods.setURL(DB_URL);
        ods.setConnectionProperties(info);
        SQL = (OracleConnection) ods.getConnection();

        System.out.println("Connection established!");
        // Get JDBC driver name and version
        DatabaseMetaData dbmd = SQL.getMetaData();
        System.out.println("Driver Name: " + dbmd.getDriverName());
        System.out.println("Driver Version: " + dbmd.getDriverVersion());
        // Print some connection properties
        System.out.println(
            "Default Row Prefetch Value: " + SQL.getDefaultRowPrefetch()
        );
        System.out.println("Database username: " + SQL.getUserName());
        System.out.println();
        // Perform some database operations
    }


    // any time a query is submit to update the date or the market open status, this should be called
    public static void loadMarketInfo() {

        // query for market open status
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT is_open FROM openclose" 
                    )
                )
            ) {
                resultSet.next();
                int isOpen = Integer.parseInt(resultSet.getString("is_open"));
                if (isOpen == 1) {
                    MARKET_IS_OPEN = true;
                } else {
                    MARKET_IS_OPEN = false;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: retrieving market open status");
            System.exit(1);
        }

        // query for date
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT TO_CHAR(M.market_date, 'YYYY-MM-DD') AS market_date FROM marketdate M" 
                    )
                )
            ) {
                resultSet.next();
                CURRENT_DATE = Date.valueOf(resultSet.getString("market_date"));
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: retrieving date");
            System.exit(1);
        }

    }

    public static void printMarketInfo() {
        System.out.println("The current date is: " + Global.CURRENT_DATE.toString());
        if (Global.MARKET_IS_OPEN) System.out.println("The market is currently OPEN");
        else System.out.println("The market is currently CLOSED");
        System.out.println();
    }

    // prompts user input, forces user to input one of the Strings listed in validInputs or calls for input again, then returns that input
    public static String getLineSetInputs(ArrayList<String> validInputs) throws IOException {
        String rawInput = br.readLine();
        if (!validInputs.contains(rawInput)) {
            // Code snippet found on https://stackoverflow.com/questions/7522022/how-to-delete-stuff-printed-to-console-by-system-out-println
            System.out.print(String.format("\033[%dA",1)); // Move up
            System.out.print("\033[2K"); // Erase line content
            System.out.println("ERROR: invalid input");
            return getLineSetInputs(validInputs);
        }
        return rawInput;
    }

    // prompts user for username and password, then returns String[] {username, password}
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

    // small helper to clear page
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();  
    }
    
    // small helper to create confirmation screen
    public static void awaitConfirmation() throws IOException {
        System.out.println();
        System.out.println("Press enter to confirm");
        br.readLine();
    }

    // HELPER FUNCTION, prompts the user for the given list of fields, then returns a HashMap of those values.
    public static LinkedHashMap<String,String> promptValues(String title, ArrayList<String> fields) throws IOException {
        clearScreen();
        System.out.println(String.format("Enter the following info for %s", title));
        LinkedHashMap<String,String> values = new LinkedHashMap<>();
        for (String field : fields) {
            System.out.println();
            System.out.println(String.format("Enter %s", field));
            String value = br.readLine();
            values.put(field, value);
        }
        Global.confirmInfo(title, values);
        Global.awaitConfirmation();
        return values;
    }

    // given HashMap of fields and values, allows user to confirm, try again, or abort.
    public static boolean confirmInfo(String title, LinkedHashMap<String,String> values) {

        // below segment identifies proper spacing for displaying strings
        int maxFieldLen = 1;
        int maxValLen = 1;
        for (Map.Entry<String,String> valueSet : values.entrySet()) {
            maxFieldLen = Math.max(maxFieldLen, valueSet.getKey().length());
            maxValLen = Math.max(maxValLen, valueSet.getValue().length());
        }

        clearScreen();
        System.out.println(String.format("These are your submitted values for %s. To cancel, quit the program.", title));
        System.out.println();
        for (Map.Entry<String,String> valueSet : values.entrySet()) {
            System.out.println(String.format("%"+maxFieldLen+"s | %-"+maxValLen+"s", valueSet.getKey(), valueSet.getValue()));
        }
        return true;
    }

    // clears screen, displays error message, awaits confirm
    public static void messageWithConfirm(String message) throws IOException {
        Global.clearScreen();
        System.out.println(message);
        Global.awaitConfirmation();
    }

    // overloaded version for multiple messages
    public static void messageWithConfirm(String[] messages) throws IOException {
        Global.clearScreen();
        for (String message : messages) {
            System.out.println(message);
        }
        Global.awaitConfirmation();
    }

    // overloaded version for table outputs
    public static void messageWithConfirm(String bonusMessage, String[] messages) throws IOException {
        Global.clearScreen();
        System.out.println(bonusMessage);
        System.out.println();
        for (String message : messages) {
            System.out.println(message);
        }
        Global.awaitConfirmation();
    }

    // regex to check if string is int
    public static boolean isInteger(String str) {
        if (str.equals("")) {
            return false;
        }
        return str.matches("^-?(0|[1-9]\\d*)$");
    }

    // regex to check if string is double
    public static boolean isDouble(String str) {
        if (str.equals("")) {
            return false;
        }
        return str.matches("^(-?)(0|([1-9][0-9]*))(\\.[0-9]+)?$");
    }

    // converts query results to integer
    public static String[] tableToString(ArrayList<String> headers, ArrayList<ArrayList<String>> values) {

        // ensure sizing is the same
        if (headers.size() != values.size()) {
            System.out.println("bad table to string request: column count inconsistent");
            System.exit(1);
        }

        // ensure all columns of equal length
        int rowCount = values.get(0).size();
        for (ArrayList<String> column : values) {
            if (column.size() != rowCount) {
                System.out.println("bad table to string request: column sizes inconsistent");
                System.exit(1);
            }
        }

        // stores max length of items in a given column
        ArrayList<Integer> columnlength = new ArrayList<>();

        // for every column, determine item of maximum length for formatting
        for (int i = 0; i < headers.size(); i++) {
            int maxLength = headers.get(i).length();
            for (String value : values.get(i)) {
                maxLength = Math.max(maxLength, value.length());
            }
            columnlength.add(maxLength);
        }

        // create array of strings where each string represents a row
        String[] finalOutput = new String[values.get(0).size()+2];
        
        // create first header row
        StringBuilder sb = new StringBuilder("");
        sb.append("| ");
        for (int i = 0; i < headers.size(); i++) {
            sb.append(String.format("%-"+columnlength.get(i)+"s | ", headers.get(i)));
        }
        finalOutput[0] = sb.toString();
        finalOutput[1] = "";

        // creating each subsequent row
        for (int i = 0; i < values.get(0).size(); i++) {
            StringBuilder sb2 = new StringBuilder("");
            sb2.append("| ");
            for (int j = 0; j < headers.size(); j++) {
                sb2.append(String.format("%-"+columnlength.get(j)+"s | ", values.get(j).get(i)));
            }
            finalOutput[i+2] = sb2.toString();
        }

        return finalOutput;
    }

    public static boolean theStockExists(String symbol) {
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
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: theStockExists");
            System.exit(1);
        }
        return true;
    }

    // symbol has to exist or this query fails
    public static double getCurrentStockPrice(String symbol) {
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
                    return -1;
                } else {
                    return Double.parseDouble(resultSet.getString("current_price"));
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: getSetStockPriceWithSymbol 1");
            System.exit(1);
        }
        return -1;
    }
    
}
