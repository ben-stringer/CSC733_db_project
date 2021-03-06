package csc733.group5.tx;

import csc733.group5.RandomDataGenerator;
import csc733.group5.data.Item;
import csc733.group5.data.OrderLine;
import org.javatuples.Pair;
import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class StockLevelTransaction implements Tx {

    private final RandomDataGenerator rdg;
    private final int wId = 1;
    private final int dId;
    private final int lowStockThreshold;

    public StockLevelTransaction(final RandomDataGenerator _rdg) {
        rdg = _rdg;
        dId = rdg.rand().nextInt(10);
        lowStockThreshold = rdg.rand().nextInt(10) + 10;
    }

    @Override
    public void run(final Transaction tx) {
        System.out.println("StockLevelTransaction --> Begin");
        //• The row in the DISTRICT table with matching D_W_ID and D_ID is selected and D_NEXT_O_ID is retrieved.
        final int nextId;
        {
            final Result res = tx.run(String.format(
                    "match (d:District { d_id : %d })<-[:W_SVC_D]-(:Warehouse {w_id : %d}) " +
                            "return d.d_next_o_id",
                    dId, wId));
            nextId = res.single().get("d.d_next_o_id").asInt();
        }
        //• All rows in the ORDER-LINE table with matching OL_W_ID (equals W_ID), OL_D_ID (equals D_ID),
        // and OL_O_ID (lower than D_NEXT_O_ID and greater than or equal to D_NEXT_O_ID minus 20) are selected.
        // They are the items for 20 recent orders of the district.
        final List<Integer> itemIds;
        {
            final Result res = tx.run(String.format(
                    "match (i:Item)<-[:OL_ITEM]-(ol:OrderLine)<-[:ORDER_LINE]-(o:Order)<-[:PLACED_ORDER]-(:Customer)<-[:D_SVC_C]-(d:District { d_id : %d })<-[:W_SVC_D]-(:Warehouse {w_id : %d}) " +
                            "where o.o_id > %d " +
                            "return ol, i",
                    dId, wId, nextId - 20));
            itemIds = res.stream()
                    .map(rec -> Pair.with(OrderLine.from(rec.get("ol").asNode()), Item.from(rec.get("i").asNode())))
                    .map(pairOlItem -> pairOlItem.getValue1().getId())
                    .distinct()
                    .collect(Collectors.toList());
        }
        //• All rows in the STOCK table with matching S_I_ID (equals OL_I_ID) and S_W_ID (equals W_ID)
        // from the list of distinct item numbers and with S_QUANTITY lower than threshold are counted (giving low_stock).
        final Result res = tx.run(String.format(
                "match (s:Stock)<-[:IN_STOCK]-(i:Item) " +
                        "where i.i_id in [%s] and s.s_quantity < %d " +
                        "return count(s)",
                itemIds.stream().map(String::valueOf).collect(Collectors.joining(", ")),
                lowStockThreshold));
        System.out.format("The number of items low in stock at district %d is %d.\n",
                dId, res.single().get("count(s)").asInt());
        System.out.println("StockLevelTransaction --> Complete");
    }
}
