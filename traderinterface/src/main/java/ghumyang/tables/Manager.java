package ghumyang.tables;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;

import ghumyang.Global;
import lombok.Getter;

public class Manager {

    @Getter String name;
    @Getter String state;
    @Getter String phone_number;
    @Getter String email_address;
    @Getter String tax_id;
    @Getter String username;
    @Getter String password;
    @Getter int manager_id;
    @Getter double balance;

    public Manager(String username, String password) {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM managers C WHERE C.username = '%s' AND C.password = '%s'", 
                        username, password
                    )
                )
            ) {
                resultSet.next();
                this.name = resultSet.getString("name");
                this.state = resultSet.getString("state");
                this.phone_number = resultSet.getString("phone_number");
                this.email_address = resultSet.getString("email_address");
                this.tax_id = resultSet.getString("tax_id");
                this.username = resultSet.getString("username");
                this.password = resultSet.getString("password");
                this.manager_id = Integer.parseInt(resultSet.getString("manager_id"));
                this.balance = Double.parseDouble(resultSet.getString("balance"));
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: declare Manager");
            System.exit(1);
        }
    }

    // copied from customer
    public static boolean isThereManagerWithThisLogin(String username, String password) throws IOException {
            try (Statement statement = Global.SQL.createStatement()) {
                try (
                    ResultSet resultSet = statement.executeQuery(
                        String.format(
                            "SELECT * FROM managers C WHERE C.username = '%s' AND C.password = '%s'", 
                            username, password
                        )
                    )
                ) {
                    if (!resultSet.next()) {
                        // no managers with this information
                        return false;
                    } else {
                        // at least one manager with this username and password
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println("FAILED QUERY: isThereManagerWithThisLoginInfo");
                System.exit(1);
            }
            return true;
        }
}
