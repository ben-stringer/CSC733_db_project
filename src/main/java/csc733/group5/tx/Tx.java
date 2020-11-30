package csc733.group5.tx;

import org.neo4j.driver.Transaction;

public interface Tx {

    void run(Transaction tx);
}
