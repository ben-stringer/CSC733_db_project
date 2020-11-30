package csc733.group5.tx;

import csc733.group5.App;
import csc733.group5.RandomDataGenerator;
import csc733.group5.data.OrderLine;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DeliveryTransaction implements Runnable {

    private static final String MATCH_NEXT_NO_TMPL =
            "match (no:NewOrder)-[:IS_NEW_ORDER]->(o:Order)<-[:PLACED_ORDER]-(c:Customer)<-[:D_SVC_C]-(:District { d_id : %d } )<-[:W_SVC_D]-(:Warehouse {w_id : %d }) " +
                    "return no, o, c order by no.no_o_id limit 1";
    private static final String DROP_NEW_TMPL =
            "match (no:NewOrder { no_o_id : %d})-[:IS_NEW_ORDER]->(o:Order {o_id : %d} ) detach delete no";
    private static final String MATCH_OL_FOR_ORDER =
            "match (ol:OrderLine)<-[:ORDER_LINE]-(o:Order {o_id : %d} )<-[:PLACED_ORDER]-(:Customer)<-[:D_SVC_C]-(:District { d_id : %d } )<-[:W_SVC_D]-(:Warehouse {w_id : %d }) " +
                    "return ol";
    private static final String UPDATE_CUST_BAL_DEL_TMPL =
            "match (c:Customer { c_id : %d })<-[:D_SVC_C]-(:District { d_id : %d } )<-[:W_SVC_D]-(:Warehouse {w_id : %d }) " +
                    "set c.c_balance = %f, c.c_delivery_cnt = %d";
    private static final String UPDATE_OL_DEL_DATE_TMPL =
            "match (ol:OrderLine)<-[:ORDER_LINE]-(ol:Order{ o_id : %d })<-[:PLACED_ORDER]-(:Customer)<-[:D_SVC_C]-(:District { d_id : %d } )<-[:W_SVC_D]-(:Warehouse {w_id : %d }) " +
                    "set ol.ol_delivery_d = '%s'";
    private static final String UPDATE_O_CAR_ID_TMPL =
            "match (o:Order { o_id : %d })<-[:PLACED_ORDER]-(:Customer)<-[:D_SVC_C]-(:District { d_id : %d } )<-[:W_SVC_D]-(:Warehouse {w_id : %d }) " +
                    "set o.o_carrier_id = %d";

    private final Driver driver;
    private final RandomDataGenerator rdg;
    private final int wId = 1;

    public DeliveryTransaction(final Driver _driver, final RandomDataGenerator _rdg) {
        driver = _driver;
        rdg = _rdg;
    }
    @Override
    public void run() {
        int dId = rdg.rand().nextInt(10);
        int processedDeliveries = 0;
        final int startingDId = dId;
        try (final Session session = driver.session()) {
            try (final Transaction tx = session.beginTransaction()) {
                //• A database transaction is started unless a database transaction is already active from being started as
                // part of the delivery of a previous order (i.e., more than one order is delivered within the same database transaction).

                while (processedDeliveries < 10) {
                    //• The row in the NEW-ORDER table with matching NO_W_ID (equals W_ID) and NO_D_ID (equals D_ID) and with the
                    // lowest NO_O_ID value is selected. This is the oldest undelivered order of that district. NO_O_ID, the order
                    // number, is retrieved. If no matching row is found, then the delivery of an order for this district is
                    // skipped. The condition in which no outstanding order is present at a given district must be handled by
                    // skipping the delivery of an order for that district only and resuming the delivery of an order from all
                    // remaining districts of the selected warehouse. If this condition occurs in more than 1%, or in more than
                    // one, whichever is greater, of the business transactions, it must be reported. The result file must be
                    // organized in such a way that the percentage of skipped deliveries and skipped districts can be determined.

                    final Node cust;
                    final int oId;
                    final int cId;
                    {
                        //• The row in the ORDER table with matching O_W_ID (equals W_ ID), O_D_ID (equals D_ID),
                        // and O_ID (equals NO_O_ID) is selected,
                        final Result res = tx.run(String.format(MATCH_NEXT_NO_TMPL, dId, wId));
                        if (!res.hasNext()) {
                            dId = (dId + 1) % 10;
                            System.out.println("No results; dId: " + dId + " starting id: " + startingDId);
                            if (dId == startingDId) return;
                            continue;
                        }
                        // O_C_ID, the customer number, is retrieved, and O_CARRIER_ID is updated.
                        final Record next = res.single();
                        final int no_o_id = next.get("no").asNode().get("no_o_id").asInt();
                        cust = next.get("c").asNode();
                        oId = next.get("o").asNode().get("o_id").asInt();
                        cId = cust.get("c_id").asInt();
                        tx.run(String.format(UPDATE_O_CAR_ID_TMPL, oId, dId, wId, rdg.rand().nextInt(10)));
                        //• The selected row in the NEW-ORDER table is deleted.
                        tx.run(String.format(DROP_NEW_TMPL, no_o_id, oId));
                    }
                    final double orderAmount;
                    {
                        //• All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID), OL_D_ID (equals O_D_ID), and
                        // OL_O_ID (equals O_ID) are selected. All OL_DELIVERY_D, the delivery dates, are updated to the current
                        // system time as returned by the operating system and the sum of all OL_AMOUNT is retrieved.
                        final Result res = tx.run(String.format(MATCH_OL_FOR_ORDER, oId, dId, wId));
                        orderAmount = res.stream()
                                .map(record -> record.get(0).asNode())
                                .map(OrderLine::from)
                                .mapToDouble(OrderLine::getAmount)
                                .sum();
                        tx.run(String.format(UPDATE_OL_DEL_DATE_TMPL, oId, dId, wId, rdg.currentDate()));
                    }
                    {
                        //• The row in the CUSTOMER table with matching C_W_ID (equals W_ID), C_D_ID (equals D_ID), and C_ID (equals
                        // O_C_ID) is selected and C_BALANCE is increased by the sum of all order-line amounts (OL_AMOUNT) previously
                        // retrieved. C_DELIVERY_CNT is incremented by 1.
                        tx.run(String.format(UPDATE_CUST_BAL_DEL_TMPL,
                                cId, dId, wId,
                                cust.get("c_balance").asDouble() + orderAmount,
                                cust.get("c_delivery_cnt").asInt() + 1));
                    }
                    //• The database transaction is committed unless more orders will be delivered within this database transaction.
                    processedDeliveries += 1;
                }
                tx.commit();
            }
        }
    }

    public static void main(final String[] args) {
        try (final Driver driver = App.startDriver()) {
            final RandomDataGenerator rdg = new RandomDataGenerator(42);
            new DeliveryTransaction(driver, rdg).run();
        }
    }
}
