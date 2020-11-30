package csc733.group5.tx;

import csc733.group5.RandomDataGenerator;
import org.neo4j.driver.Driver;

public class PaymentTransaction implements Runnable {

    private final Driver driver;
    private final RandomDataGenerator rdg;

    public PaymentTransaction(final Driver _driver, final RandomDataGenerator _rdg) {
        driver = _driver;
        rdg = _rdg;
    }

    @Override
    public void run() {
        //• A database transaction is started.
        //• The row in the WAREHOUSE table with matching W_ID is selected.
        // W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, and W_ZIP are retrieved and W_YTD,
        // the warehouse's year-to-date balance, is increased by H_ AMOUNT.

        //• The row in the DISTRICT table with matching D_W_ID and D_ID is selected.
        // D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, and D_ZIP are retrieved and D_YTD,
        // the district's year-to-date balance, is increased by H_AMOUNT.

        //• Case 1, the customer is selected based on customer number:
        // the row in the CUSTOMER table with matching C_W_ID, C_D_ID and C_ID is selected.
        // C_FIRST, C_MIDDLE, C_LAST, C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT,
        // C_CREDIT_LIM, C_DISCOUNT, and C_BALANCE are retrieved. C_BALANCE is decreased by H_AMOUNT. C_YTD_PAYMENT
        // is increased by H_AMOUNT. C_PAYMENT_CNT is incremented by 1.

        //• Case 2, the customer is selected based on customer last name:
        // all rows in the CUSTOMER table with matching C_W_ID, C_D_ID and C_LAST are selected sorted by C_FIRST
        // in ascending order. Let n be the number of rows selected. C_ID, C_FIRST, C_MIDDLE, C_STREET_1, C_STREET_2,
        // C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT, C_CREDIT_LIM, C_DISCOUNT, and C_BALANCE are retrieved
        // from the row at position (n/ 2 rounded up to the next integer) in the sorted set of selected rows from the
        // CUSTOMER table. C_BALANCE is decreased by H_AMOUNT. C_YTD_PAYMENT is increased by H_AMOUNT.
        // C_PAYMENT_CNT is incremented by 1.

        //• If the value of C_CREDIT is equal to "BC", then C_DATA is also retrieved from the selected customer and
        // the following history information: C_ID, C_D_ID, C_W_ID, D_ID, W_ID, and H_AMOUNT, are inserted at the left
        // of the C_DATA field by shifting the existing content of C_DATA to the right by an equal number of bytes and
        // by discarding the bytes that are shifted out of the right side of the C_DATA field.
        // The content of the C_DATA field never exceeds 500 characters.
        // The selected customer is updated with the new C_DATA field.
        // If C_DATA is implemented as two fields (see Clause 1.4.9),
        // they must be treated and operated on as one single field.

        //• H_DATA is built by concatenating W_NAME and D_NAME separated by 4 spaces.

        //• A new row is inserted into the HISTORY table with H_C_ID = C_ID, H_C_D_ID = C_D_ID, H_C_W_ID = C_W_ID,
        // H_D_ID = D_ID, and H_W_ID = W_ID.

        //• The database transaction is committed.
    }
}
