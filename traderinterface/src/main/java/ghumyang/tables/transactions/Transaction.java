package ghumyang.tables.transactions;
import java.sql.Date;

import lombok.Getter;

public class Transaction {
    @Getter int transactionId;
    @Getter Date date;
    @Getter int accountId;
}