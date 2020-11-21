package csc733.group5.data;

import csc733.group5.RandomDataGenerator;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

public interface Order {
    int getId();
    String getEntryDate();
    String getCarrierId();
    int getOrderLineCount();
    int getAllLocal();

    static final String O_TMPL =
            "{ o_id : %d, " +
                    "o_entry_d : '%s', " +
                    "o_carrier_id : '%s', " +
                    "o_ol_cnt : %d, " +
                    "o_all_local : %d }";

    static Pair<Order, List<OrderLine>> from(final int id, final List<Pair<Item, Stock>> itemstock, final int distNum, final RandomDataGenerator rdg) {
        final String entryDate = rdg.randomDate();
        final String carrierId = rdg.randomWord(10);
        final int olCount = rdg.rand().nextInt(10) + 5;
        final List<OrderLine> ol = new ArrayList<>(olCount);
        final int totalItems =  itemstock.size();
        for (int i = 0; i < olCount; i++) {
            final Pair<Item, Stock> rndItem = itemstock.get(rdg.rand().nextInt(totalItems));
            ol.add(OrderLine.from(id*100+i, rndItem.getValue0(), rndItem.getValue1().getDist(distNum), rdg));
        }
        return Pair.with(new Order() {
            @Override public int getId() { return id; }
            @Override public String getEntryDate() { return entryDate; }
            @Override public String getCarrierId() { return carrierId; }
            @Override public int getOrderLineCount() { return olCount; }
            @Override public int getAllLocal() { return 1; }
            @Override public String toString() {
                return String.format(O_TMPL, id, entryDate, carrierId, olCount, 1);
            }
        }, ol);
    }
}
