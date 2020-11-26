package csc733.group5.data;

import csc733.group5.RandomDataGenerator;
import org.javatuples.Pair;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Order {
    int getId();
    String getEntryDate();
    String getCarrierId();
    int getOrderLineCount();
    int getAllLocal();

    default void setId(int oId) { throw new IllegalStateException("This method is not implemented for this implementation of Order."); }

    String toCypherCreateString();

    static final String O_TMPL =
            "{ o_id : %d, " +
                    "o_entry_d : '%s', " +
                    "o_carrier_id : '%s', " +
                    "o_ol_cnt : %d, " +
                    "o_all_local : %d }";

    static Pair<Order, List<Pair<OrderLine,Item>>> from(final int id, final List<Pair<Item, Stock>> itemstock, final int distNum, final RandomDataGenerator rdg) {
        final String entryDate = rdg.randomDate();
        final String carrierId = rdg.randomWord(10);
        final int olCount = rdg.rand().nextInt(10) + 5;
        final int totalItems =  itemstock.size();
        final List<Pair<OrderLine, Item>> ol = IntStream.range(0,olCount).mapToObj(i -> {
            final Pair<Item, Stock> rndItem = itemstock.get(rdg.rand().nextInt(totalItems));
            final Item item = rndItem.getValue0();
            final Stock stock = rndItem.getValue1();
            return Pair.with(
                    OrderLine.from(item, rdg.randomDate(), stock.getDist(distNum), rdg.rand().nextInt(10)+1),
                    item);
        }).collect(Collectors.toList());
        return Pair.with(new Order() {
            @Override public int getId() { return id; }
            @Override public String getEntryDate() { return entryDate; }
            @Override public String getCarrierId() { return carrierId; }
            @Override public int getOrderLineCount() { return olCount; }
            @Override public int getAllLocal() { return 1; }
            @Override public String toCypherCreateString() {
                return String.format(O_TMPL, id, entryDate, carrierId, olCount, 1);
            }
        }, ol);
    }

    class OrderPojo implements Order {
        int id;
        final String entryDate;
        final int olCount;
        OrderPojo(final RandomDataGenerator rdg, final int _olCount) {
            id = Integer.MAX_VALUE;
            olCount = _olCount;
            entryDate = rdg.currentDate();
        }

        @Override public int getId() { return id; }
        @Override public void setId(final int _id) {id = _id;}
        @Override public String getEntryDate() { return entryDate; }
        @Override public String getCarrierId() { return null; }
        @Override public int getOrderLineCount() { return olCount; }
        @Override public int getAllLocal() { return 1; }
        @Override public String toCypherCreateString() {
            return String.format(O_TMPL, id, entryDate, null, olCount, 1);
        }
    }

    static Order from(final RandomDataGenerator rdg, final int olCount) {
        return new OrderPojo(rdg, olCount);
    }
}
