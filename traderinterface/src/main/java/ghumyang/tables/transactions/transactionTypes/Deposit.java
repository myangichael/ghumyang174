package ghumyang.tables.transactions.transactionTypes;
import ghumyang.tables.transactions.Transaction;
import lombok.Getter;

public class Deposit extends Transaction{
    @Getter double amount;
}