package csc733.group5.tx;

import csc733.group5.App;
import csc733.group5.RandomDataGenerator;
import csc733.group5.data.History;
import csc733.group5.data.RandomInitialState;
import org.javatuples.Quintet;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PaymentTransaction implements Runnable {

    private final Driver driver;
    private final RandomDataGenerator rdg;

    public PaymentTransaction(final Driver _driver, final RandomDataGenerator _rdg) {
        driver = _driver;
        rdg = _rdg;
    }

    @Override
    public void run() {
        final int wId = 1;
        final int dId = rdg.rand().nextInt(10);
        final double hAmount = rdg.rand().nextDouble() * 5000;
        final Function<Transaction, Node> customerQuery;
        {
            if (rdg.rand().nextDouble() > 0.4) {
                //• Case 1, the customer is selected based on customer number:
                // the row in the CUSTOMER table with matching C_W_ID, C_D_ID and C_ID is selected.
                // C_FIRST, C_MIDDLE, C_LAST, C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT,
                // C_CREDIT_LIM, C_DISCOUNT, and C_BALANCE are retrieved. C_BALANCE is decreased by H_AMOUNT. C_YTD_PAYMENT
                // is increased by H_AMOUNT. C_PAYMENT_CNT is incremented by 1.
                customerQuery = (tx) -> {
                    final int cId = rdg.rand().nextInt(RandomInitialState.NUM_CUST_PER_DIST);
                    final Result res = tx.run(String.format(
                            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_id : %d }) " +
                                    "set c.c_balance = c.c_balance - %f, c.c_ytd_payment = c.c_ytd_payment + %f, c.c_payment_cnt = c.c_payment_cnt + 1 " +
                                    "return c",
                            wId, dId, cId, hAmount, hAmount));
                    return res.single().get("c").asNode();
                };
            } else {
                //• Case 2, the customer is selected based on customer last name:
                // all rows in the CUSTOMER table with matching C_W_ID, C_D_ID and C_LAST are selected sorted by C_FIRST
                // in ascending order. Let n be the number of rows selected. C_ID, C_FIRST, C_MIDDLE, C_STREET_1, C_STREET_2,
                // C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT, C_CREDIT_LIM, C_DISCOUNT, and C_BALANCE are retrieved
                // from the row at position (n/ 2 rounded up to the next integer) in the sorted set of selected rows from the
                // CUSTOMER table. C_BALANCE is decreased by H_AMOUNT. C_YTD_PAYMENT is increased by H_AMOUNT.
                // C_PAYMENT_CNT is incremented by 1.
                customerQuery = (tx) -> {
                    Record rec = null;
                    while (rec == null) {
                        final String cLast = rdg.randomLastName();
                        final Result res = tx.run(String.format(
                                "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_last : '%s' }) " +
                                        "with c order by c.c_first " +
                                        "return c",
                                wId, dId, cLast));
                        final List<Record> records = res.list();
                        final int n = records.size();
                        if (n > 0) {
                            rec = records.get(n / 2);
                        }
                    }
                    final Node cust = rec.get("c").asNode();
                    tx.run(String.format(
                            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_id : %d }) " +
                                    "set c.c_balance = c.c_balance - %f, c.c_ytd_payment = c.c_ytd_payment + %f, c.c_payment_cnt = c.c_payment_cnt + 1 ",
                            wId, dId, cust.get("c_id").asInt(), hAmount, hAmount));
                    return cust;
                };
            }

            //• A database transaction is started.
            try (final Session session = driver.session()) {
                try (final Transaction tx = session.beginTransaction()) {

                    //• The row in the WAREHOUSE table with matching W_ID is selected.
                    // W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, and W_ZIP are retrieved and W_YTD,
                    // the warehouse's year-to-date balance, is increased by H_ AMOUNT.
                    final Record w = tx.run(String.format(
                            "match (w:Warehouse {w_id : %d}) " +
                                    "set w.w_ytd = w.w_ytd + %f " +
                                    "return w.w_name, w.w_street_1, w.w_street_2, w.w_city, w.w_state, w.w_zip",
                            wId, hAmount)).single();
                    final String wName = w.get("w.w_name").asString();
                    final String wStreet1 = w.get("w.w_street_1").asString();
                    final String wStreet2 = w.get("w.w_street_2").asString("");
                    final String wCity = w.get("w.w_city").asString();
                    final String wState = w.get("w.w_state").asString();
                    final int wZip = w.get("w.w_zip").asInt();

                    //• The row in the DISTRICT table with matching D_W_ID and D_ID is selected.
                    // D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, and D_ZIP are retrieved and D_YTD,
                    // the district's year-to-date balance, is increased by H_AMOUNT.
                    final Record d = tx.run(String.format(
                            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(d:District { d_id : %d}) " +
                                    "set d.d_ytd = d.d_ytd + %f " +
                                    "return d.d_name, d.d_street_1, d.d_street_2, d.d_city, d.d_state, d.d_zip",
                            wId, dId, hAmount)).single();
                    final String dName = d.get("d.d_name").asString();
                    final String dStreet1 = d.get("d.d_street_1").asString();
                    final String dStreet2 = d.get("d.d_street_2").asString("");
                    final String dCity = d.get("d.d_city").asString();
                    final String dState = d.get("d.d_state").asString();
                    final int dZip = d.get("d.d_zip").asInt();

                    // C_FIRST, C_MIDDLE, C_LAST, C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT,
                    // C_CREDIT_LIM, C_DISCOUNT, and C_BALANCE are retrieved. C_BALANCE is decreased by H_AMOUNT. C_YTD_PAYMENT
                    // is increased by H_AMOUNT. C_PAYMENT_CNT is incremented by 1.
                    final Node cust = customerQuery.apply(tx);
                    final int cId = cust.get("c_id").asInt();
                    final String cFirst = cust.get("c_first").asString();
                    final String cMiddle = cust.get("c_middle").asString();
                    final String cLast = cust.get("c_last").asString();
                    final String cStreet1 = cust.get("c_street_1").asString();
                    final String cStreet2 = cust.get("c_street_2").asString("");
                    final String cCity = cust.get("c_city").asString();
                    final String cState = cust.get("c_state").asString();
                    final int cZip = cust.get("c_zip").asInt();
                    final String cPhone = cust.get("c_phone").asString();
                    final String cSince = cust.get("c_since").asString();
                    final String cCredit = cust.get("c_credit").asString();
                    final int cCreditLim = cust.get("c_credit_lim").asInt();
                    final double cDiscount = cust.get("c_discount").asDouble();
                    final double cBalance = cust.get("c_balance").asDouble();

                    //• If the value of C_CREDIT is equal to "BC", then C_DATA is also retrieved from the selected customer and
                    // the following history information: C_ID, C_D_ID, C_W_ID, D_ID, W_ID, and H_AMOUNT, are inserted at the left
                    // of the C_DATA field by shifting the existing content of C_DATA to the right by an equal number of bytes and
                    // by discarding the bytes that are shifted out of the right side of the C_DATA field.
                    // The content of the C_DATA field never exceeds 500 characters.
                    // The selected customer is updated with the new C_DATA field.
                    if (cCredit.equals("BC")) {
                        final String cData = cust.get("c_data").asString();
                        final String newCData;
                        {
                            final String tmp = String.format("%d,%d,%d,%d,%d,%f,%s",
                                    cId, dId, wId, dId, wId, hAmount, cData);
                            if (tmp.length() < 500) newCData = tmp;
                            else newCData = tmp.substring(0, 500);
                        }
                        tx.run(String.format(
                                "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_id : %d }) " +
                                        "set c.c_data = '%s'",
                                wId, dId, cId, newCData));
                    }

                    //• H_DATA is built by concatenating W_NAME and D_NAME separated by 4 spaces.
                    final String hData = String.format("%s    %s", wName, dName);

                    //• A new row is inserted into the HISTORY table with H_C_ID = C_ID, H_C_D_ID = C_D_ID, H_C_W_ID = C_W_ID,
                    // H_D_ID = D_ID, and H_W_ID = W_ID.
                    tx.run(String.format(
                            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_id : %d }) " +
                                    "create (c)-[:CUST_HIST]->(:History %s)",
                            wId, dId, cId, History.from(rdg.currentDate(), hAmount, hData).toCypherCreateString()));
                    //• The database transaction is committed.
                    tx.commit();
                }
            }
        }
    }

    public static final void main(final String[] args) {
        final RandomDataGenerator rdg = new RandomDataGenerator(42);
            try(final Driver driver = App.startDriver()) {
            for (int i = 0; i < 10; i++) {
                new PaymentTransaction(driver, rdg).run();
            }
        }
    }
}
