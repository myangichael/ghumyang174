package ghumyang.tables.transactions.transactionTypes;
import ghumyang.tables.transactions.Transaction;
import lombok.Getter;

public class Withdrawal extends Transaction{
    @Getter double amount;
}