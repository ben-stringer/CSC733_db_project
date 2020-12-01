package csc733.group5.tx;

import csc733.group5.RandomDataGenerator;
import csc733.group5.data.*;
import org.javatuples.Pair;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NewOrderTransaction implements Tx {

    private static final String MATCH_WH_TAX_TMPL =
            "match (w:Warehouse {w_id : %d}) return w.w_tax";
    private static final String MATCH_D_TAX_NEXTID_TMPL =
            "match (w:Warehouse {w_id : %d})-[:W_SVC_D]->(d:District { d_id : %d}) " +
                    "return d.d_tax, d.d_next_o_id";
    private static final String UPDATE_D_NEXTID_TMPL =
            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(d:District { d_id : %d}) " +
                    "set d.d_next_o_id = d.d_next_o_id + 1";
    private static final String MATCH_CUST_FIELDS_TMPL =
            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_id : %d }) " +
                    "return c.c_discount, c.c_last, c.c_credit";
    private static final String CREATE_NEW_ORDER_TMPL =
            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer {c_id : %d} ) " +
                    "create (c)-[:PLACED_ORDER]->(o:Order %s )<-[:IS_NEW_ORDER]-(no:NewOrder %s)";
    private static final String MATCH_ITEM_TMPL =
            "match (i:Item { i_id : %d }) return i.i_price, i.i_name, i.i_data";
    private static final String MATCH_ITEM_STOCK_TMPL =
            "match (i:Item {i_id : %d})-[:IN_STOCK]->(s:Stock) return s.s_quantity, s.%s, s.s_data";
    private static final String UPDATE_STOCK_QTY_TMPL =
            "match (i:Item {i_id : %d})-[:IN_STOCK]->(s:Stock) set s.s_quantity = %d";
    private static final String CREATE_ORDERLINE_TMPL =
            "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(:Customer)-[:PLACED_ORDER]->(o:Order {o_id : %d}), (i:Item {i_id : %d}) " +
                    "create (o)-[:ORDER_LINE]->(:OrderLine %s)-[:OL_ITEM]->(i)";


    private RandomDataGenerator rdg;
    private Runnable onCompleteCallback;
    private final int wId = 1;
    private final int dId;
    private final int cId;
    private final int olCount;
    private final Order order;
    private final List<Pair<Integer,Integer>> itemNumbers;
    private final boolean isFailure;

    public NewOrderTransaction(final RandomDataGenerator _rdg, final Runnable _onCompleteCallback) {
        rdg = _rdg;
        onCompleteCallback = _onCompleteCallback;
        dId = rdg.rand().nextInt(10);
        cId = rdg.rand().nextInt(RandomInitialState.NUM_CUST_PER_DIST);
        olCount = rdg.rand().nextInt(10) + 5;
        order = Order.from(rdg, olCount);
        itemNumbers = Stream
                .generate(() -> rdg.rand().nextInt(RandomInitialState.NUM_ITEMS))
                .distinct().limit(olCount)
                .map(iId -> Pair.with(iId, rdg.rand().nextInt(10)+1))
                .collect(Collectors.toList());
        isFailure = rdg.rand().nextInt(100) == 0;
        if (isFailure) itemNumbers.set(olCount-1, Pair.with(Integer.MIN_VALUE, 10));
    }
    @Override
    public void run(final Transaction tx) {
        System.out.println("NewOrderTransaction --> Begin");
        //• The row in the WAREHOUSE table with matching W_ID is selected and W_TAX, the warehouse tax rate, is retrieved.
        final double wTax = tx.run(String.format(MATCH_WH_TAX_TMPL, wId))
                .single().get("w.w_tax").asDouble();
        //• The row in the DISTRICT table with matching D_W_ID and D_ ID is selected,
        // D_TAX, the district tax rate, is retrieved, and D_NEXT_O_ID,
        // the next available order number for the district, is retrieved ...
        final double dTax;
        final int nextOId;
        {
            final Result res = tx.run(String.format(MATCH_D_TAX_NEXTID_TMPL, wId, dId));
            final Record next = res.single();
            dTax = next.get("d.d_tax").asDouble();
            nextOId = next.get("d.d_next_o_id").asInt();
        }
        order.setId(nextOId * 10 + dId);
        // ... and incremented by one.
        tx.run(String.format(UPDATE_D_NEXTID_TMPL, wId, dId));

        //• The row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is selected and C_DISCOUNT,
        // the customer's discount rate, C_LAST, the customer's last name,
        // and C_CREDIT, the customer's credit status, are retrieved.
        final double cDiscount;
        final String cLast;
        final String cCredit;
        {
            final Result res = tx.run(String.format(MATCH_CUST_FIELDS_TMPL, wId, dId, cId));
            final Record next = res.single();
            cDiscount = next.get("c.c_discount").asDouble();
            cLast = next.get("c.c_last").asString();
            cCredit = next.get("c.c_credit").asString();
        }
        //• A new row is inserted into both the NEW-ORDER table and the ORDER table to reflect the creation of the new order.
        // O_CARRIER_ID is set to a null value. If the order includes only home order-lines,
        // then O_ALL_LOCAL is set to 1, otherwise O_ALL_LOCAL is set to 0.
        tx.run(String.format(CREATE_NEW_ORDER_TMPL, wId, dId, cId, order.toCypherCreateString(), new NewOrder().toCypherCreateString()));

        //• For each O_OL_CNT item on the order:
        final List<Pair<Item, Integer>> listPairItemCounts = new ArrayList<>(itemNumbers.size());
        for (final Pair<Integer, Integer> pairIdCount : itemNumbers) {
            final int itemId = pairIdCount.getValue0();
            final int itemCount = pairIdCount.getValue1();
            //  - The row in the ITEM table with matching I_ID (equals OL_I_ID) is selected and I_PRICE,
            //  the price of the item, I_NAME, the name of the item, and I_DATA are retrieved.
            //  If I_ID has an unused value (see Clause 2.4.1.5), a "not-found" condition is signaled,
            //  resulting in a rollback of the database transaction (see Clause 2.4.2.3).
            final Result res = tx.run(String.format(MATCH_ITEM_TMPL, itemId));
            if (!res.hasNext()) {
                System.out.format("Item id '%d' does not exist in the database.\n", itemId);
                tx.rollback();
                onCompleteCallback.run();
                return;
            }
            final Record next = res.single();
            listPairItemCounts.add(Pair.with(
                    Item.from(itemId, next.get("i.i_name").asString(), next.get("i.i_price").asDouble(), next.get("i.i_data").asString()),
                    itemCount));
        }
        final double orderAmount = listPairItemCounts.stream()
                .map(pairItemCount -> {
                    final Item item = pairItemCount.getValue0();
                    final int itemCount = pairItemCount.getValue1();
                    //  - The row in the STOCK table with matching S_I_ID (equals OL_I_ID)
                    //  and S_W_ID (equals OL_SUPPLY_W_ID) is selected. S_QUANTITY, the quantity in stock, S_DIST_xx,
                    //  where xx represents the district number, and S_DATA are retrieved.
                    final String s_dist_xx = String.format("s_dist_%02d", dId + 1);
                    final Result res = tx.run(String.format(MATCH_ITEM_STOCK_TMPL, item.getId(), s_dist_xx));
                    final Record next = res.next();
                    final int sQuantity = next.get("s.s_quantity").asInt();
                    final String sDist = next.get("s." + s_dist_xx).asString();
                    final String sData = next.get("s.s_data").asString();
                    //  If the retrieved value for S_QUANTITY exceeds OL_QUANTITY by 10 or more,
                    //  then S_QUANTITY is decreased by OL_QUANTITY; otherwise S_QUANTITY is updated to
                    //  (S_QUANTITY - OL_QUANTITY)+91. S_YTD is increased by OL_QUANTITY and S_ORDER_CNT is incremented by 1.
                    int updatedQuantity = sQuantity - itemCount;
                    if (updatedQuantity <= 10) updatedQuantity += 91;
                    tx.run(String.format(UPDATE_STOCK_QTY_TMPL, item.getId(), updatedQuantity));
                    //  - The strings in I_DATA and S_DATA are examined.
                    //  If they both include the string "ORIGINAL", the brand- generic field for that item is set to "B",
                    //  otherwise, the brand-generic field is set to "G".
                    //  NOTE: where is this "brand generic" field?!

                    //  - A new row is inserted into the ORDER-LINE table to reflect the item on the order.
                    //  OL_DELIVERY_D is set to a null value,
                    //  OL_NUMBER is set to a unique value within all the ORDER-LINE rows that have the same OL_O_ID value,
                    //  and OL_DIST_INFO is set to the content of S_DIST_xx,
                    //  where xx represents the district number (OL_D_ID)
                    final OrderLine ol = OrderLine.from(item, null, sDist, itemCount);
                    tx.run(String.format(CREATE_ORDERLINE_TMPL,
                            wId, dId, order.getId(), item.getId(), ol.toCypherCreateString()));
                    return ol;
                }).mapToDouble(OrderLine::getAmount)
                //• The total-amount for the complete order is computed as:
                //            sum(OL_AMOUNT) *(1 - C_DISCOUNT) *(1 + W_TAX + D_TAX)
                .sum() * (1-cDiscount) * (1+wTax + dTax);
        System.out.format("Placing order; amount is %f.\n", orderAmount);
        //• The database transaction is committed,
        // unless it has been rolled back as a result of an unused value for the last item number (see Clause 2.4.1.5).
        tx.commit();
        onCompleteCallback.run();
        System.out.println("NewOrderTransaction --> Complete");
    }
}
