package csc733.group5.tx;

import csc733.group5.App;
import csc733.group5.RandomDataGenerator;
import csc733.group5.data.RandomInitialState;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.neo4j.driver.*;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OrderStatusTransaction implements Runnable {

    private final Driver driver;
    private final RandomDataGenerator rdg;
    private final int wId = 1;
    private final int dId;
    private final Function<Transaction, Quintet<Integer, Double, String, String, String>> customerQuery;

    public OrderStatusTransaction(final Driver _driver, final RandomDataGenerator _rdg) {
        driver = _driver;
        rdg = _rdg;
        dId = rdg.rand().nextInt(10);
        if (rdg.rand().nextDouble() > 0.4) {
            customerQuery = (tx) -> {
                //• Case 1, the customer is selected based on customer number:
                // the row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is selected and C_BALANCE, C_FIRST,
                // C_MIDDLE, and C_LAST are retrieved.
                final int cId = rdg.rand().nextInt(RandomInitialState.NUM_CUST_PER_DIST);
                final Result res = tx.run(String.format(
                        "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_id : %d }) " +
                                "return c.c_balance, c.c_first, c.c_middle, c.c_last",
                        wId, dId, cId));
                final Record rec = res.single();
                return Quintet.with(cId,
                        rec.get("c.c_balance").asDouble(),
                        rec.get("c.c_first").asString(),
                        rec.get("c.c_middle").asString(),
                        rec.get("c.c_last").asString());
            };
        } else {
            customerQuery = (tx) -> {
                //• Case 2, the customer is selected based on customer last name:
                // all rows in the CUSTOMER table with matching C_W_ID, C_D_ID and C_LAST are selected
                // sorted by C_FIRST in ascending order. Let n be the number of rows selected.
                // C_BALANCE, C_FIRST, C_MIDDLE, and C_LAST are retrieved from the row at position n/ 2 rounded up
                // in the sorted set of selected rows from the CUSTOMER table.
                Record rec = null;
                while (rec == null) {
                    final String cLast = rdg.randomLastName();
                    final Result res = tx.run(String.format(
                            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_last : '%s' }) " +
                                    "with c order by c.c_first " +
                                    "return c.c_id, c.c_balance, c.c_first, c.c_middle, c.c_last",
                            wId, dId, cLast));
                    final List<Record> records = res.list();
                    final int n = records.size();
                    if (n > 0) {
                        rec = records.get(n/2);
                    }
                }
                return Quintet.with(rec.get("c.c_id").asInt(),
                        rec.get("c.c_balance").asDouble(),
                        rec.get("c.c_first").asString(),
                        rec.get("c.c_middle").asString(),
                        rec.get("c.c_last").asString());

            };
        }
    }

    @Override
    public void run() {
        System.out.println("OrderStatusTransaction --> Begin");
        //• A database transaction is started.
        try (final Session session = driver.session()) {
            try (final Transaction tx = session.beginTransaction()) {
                final Quintet<Integer, Double, String, String, String> queryResult = customerQuery.apply(tx);
                final int cId = queryResult.getValue0();
                final double balance = queryResult.getValue1();
                final String first = queryResult.getValue2();
                final String middle = queryResult.getValue3();
                final String last = queryResult.getValue4();

                final int oId;
                final String entryD;
                final String oCarrierId;
                {
                    //• The row in the ORDER table with matching O_W_ID (equals C_W_ID), O_D_ID (equals C_D_ID), O_C_ID (equals C_ID),
                    // and with the largest existing O_ID, is selected. This is the most recent order placed by that customer.
                    // O_ID, O_ENTRY_D, and O_CARRIER_ID are retrieved.
                    final Result result = tx.run(String.format(
                            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer {c_id : %d} )-[:PLACED_ORDER]->(o:Order) " +
                                    "with o " +
                                    "order by o.o_id desc " +
                                    "return o.o_id, o.o_entry_d, o.o_carrier_id limit 1",
                            wId, dId, cId));
                    if (!result.hasNext()) {
                        System.out.println("Checking order status for customer who never placed an order.");
                        tx.rollback();
                        return;
                    }
                    final Record rec = result.single();
                    oId = rec.get("o.o_id").asInt();
                    entryD = rec.get("o.o_entry_d").asString();
                    oCarrierId = rec.get("o.o_carrier_id").asString();
                }
                final List<Quintet<Integer, Integer, Integer, Double, String>> orderLineItems;
                {
                    //• All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID)
                    // are selected and the corresponding sets of OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.
                    final Result result = tx.run(String.format(
                            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(:Customer)-[:PLACED_ORDER]->(:Order {o_id : %d})-[:ORDER_LINE]->(ol:OrderLine)-[:OL_ITEM]->(i:Item) " +
                                    "return i.i_id, ol.ol_supply_w_id, ol.ol_quantity, ol.ol_amount, ol.ol_delivery_d",
                            wId, dId, oId));
                    orderLineItems = result.stream().map( rec ->
                            Quintet.with(rec.get("i.i_id").asInt(),
                                    rec.get("ol.ol_supply_w_id").asInt(),
                                    rec.get("ol.ol_quantity").asInt(),
                                    rec.get("ol.ol_amount").asDouble(),
                                    rec.get("ol.ol_delivery_d").asString()))
                            .collect(Collectors.toList());
                }
                //• The database transaction is committed.
                // Not necessary because read-only, but doesn't hurt
                tx.commit();
            }
        }
        System.out.println("OrderStatusTransaction --> Complete");
    }

    public static void main(final String[] args) {
        final RandomDataGenerator rdg = new RandomDataGenerator(42);
        try (final Driver driver = App.startDriver()) {
            for (int i = 0; i < 10; i++) {
                new OrderStatusTransaction(driver, rdg).run();
            }
        }
    }
}
