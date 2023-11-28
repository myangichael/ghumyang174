package ghumyang.tables.transactions.transactionTypes;

import ghumyang.tables.transactions.Transaction;
import lombok.Getter;

public class Sell extends Transaction {
    @Getter String ticker;
    @Getter int count;
    @Getter double purchasePrice;
}