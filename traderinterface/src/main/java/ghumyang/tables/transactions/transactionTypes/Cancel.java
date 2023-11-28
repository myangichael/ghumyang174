package ghumyang.tables.transactions.transactionTypes;

import ghumyang.tables.transactions.Transaction;
import lombok.Getter;

public class Cancel extends Transaction {
    @Getter int canceledId;
}