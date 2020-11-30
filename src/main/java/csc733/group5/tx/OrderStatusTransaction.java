package csc733.group5.tx;

import csc733.group5.RandomDataGenerator;
import org.neo4j.driver.Driver;

public class OrderStatusTransaction implements Runnable {

    private final Driver driver;
    private final RandomDataGenerator rdg;

    public OrderStatusTransaction(final Driver _driver, final RandomDataGenerator _rdg) {
        driver = _driver;
        rdg = _rdg;
    }

    @Override
    public void run() {
        //• A database transaction is started.

        
        //• Case 1, the customer is selected based on customer number:
        // the row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is selected and C_BALANCE, C_FIRST,
        // C_MIDDLE, and C_LAST are retrieved.
        //• Case 2, the customer is selected based on customer last name:
        // all rows in the CUSTOMER table with matching C_W_ID, C_D_ID and C_LAST are selected
        // sorted by C_FIRST in ascending order. Let n be the number of rows selected.
        // C_BALANCE, C_FIRST, C_MIDDLE, and C_LAST are retrieved from the row at position n/ 2 rounded up
        // in the sorted set of selected rows from the CUSTOMER table.

        //• The row in the ORDER table with matching O_W_ID (equals C_W_ID), O_D_ID (equals C_D_ID), O_C_ID (equals C_ID),
        // and with the largest existing O_ID, is selected. This is the most recent order placed by that customer.
        // O_ID, O_ENTRY_D, and O_CARRIER_ID are retrieved.

        //• All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID)
        // are selected and the corresponding sets of OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.

        //• The database transaction is committed.
    }
}
