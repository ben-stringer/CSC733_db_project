package csc733.group5.data;

import csc733.group5.RandomDataGenerator;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;

public class RandomInitialState {

    private static final int NUM_CUST_PER_DIST = // 3000;
            10;
    private static final int NUM_ITEMS = // 10000;
            200;
    private static final int NUM_ORDERS = // 30000;
            200;
    private static final int NUM_CUSTOMERS = 10*NUM_CUST_PER_DIST;

    private static final String CREATE_WH_TMPL = "create (w:Warehouse %s)\n";
    private static final String CREATE_D_TMPL = "create (w)-[:W_SVC_D]->(d_%d:District %s)\n";
    private static final String CREATE_I_S_TMPL = "create (w)-[:WH_STOCK]->(:Stock %s)<-[:I_STOCK]-(i_%d:Item %s)\n";
    private static final String CREATE_C_TMPL = "create (d_%d)-[:D_SVC_C]->(c_%d:Customer %s)\n";
    private static final String CREATE_O_TMPL = "create (c_%d)-[:PLACED_ORDER]->(o_%d:Order %s)\n";
    private static final String CREATE_OL_TMPL = "create (o_%d)-[:ORDER_LINE]->(ol_%d:OrderLine %s)\n";

    private final Warehouse warehouse;
    private final List<District> districts = new ArrayList<>(10);
    private final List<Pair<Customer, Integer>> customers = new ArrayList<>(NUM_CUSTOMERS);
    final List<Pair<Item, Stock>> itemStock = new ArrayList<>(NUM_ITEMS);
    final List<Triplet<Customer, Order, List<OrderLine>>> orders = new ArrayList<>(NUM_ORDERS);

    public RandomInitialState(final RandomDataGenerator rdg) {
        warehouse = Warehouse.from(1, rdg);
        for (int i = 0; i < 10; i++) {
            districts.add(District.from(i, rdg));
            final int ik = i * 10000;
            for (int j = 0; j < NUM_CUST_PER_DIST; j++) {
                final int cid = ik + j;
                final Customer cust = Customer.from(cid, rdg);
                customers.add(Pair.with(cust, i));
            }
        }
        for (int i = 0; i < NUM_ITEMS; i++) {
            final Item item = Item.from(i, rdg);
            final Stock stock = Stock.from(rdg);
            itemStock.add(Pair.with(item, stock));
        }
        for (int i = 0; i < NUM_ORDERS; i++) {
            final Pair<Customer, Integer> cust = customers.get(rdg.rand().nextInt(NUM_CUSTOMERS));
            final Pair<Order, List<OrderLine>> order = Order.from(i, itemStock, cust.getValue1(), rdg);
            double amt = 0;
            for (final OrderLine ol : order.getValue1()) {
                amt += ol.getAmount();
            }
            cust.getValue0().setBalance(cust.getValue0().getBalance() + amt);
            orders.add(Triplet.with(cust.getValue0(), order.getValue0(), order.getValue1()));
        }
    }

    public String toCypherCreate() {
        final StringBuilder cypher = new StringBuilder();
        cypher.append(String.format(CREATE_WH_TMPL, warehouse));
        for (final District d : districts) {
            cypher.append(String.format(CREATE_D_TMPL, d.getId(), d));
        }
        for (final Pair<Customer, Integer> custDistPair : customers) {
            final int dId = custDistPair.getValue1();
            final Customer cust = custDistPair.getValue0();
            cypher.append(String.format(CREATE_C_TMPL, dId, cust.getId(), cust));
        }
        for (final Pair<Item, Stock> itemStockPair : itemStock) {
            final Item item = itemStockPair.getValue0();
            final Stock stock = itemStockPair.getValue1();
            cypher.append(String.format(CREATE_I_S_TMPL, stock, item.getId(), item));
        }
        for (final Triplet<Customer, Order, List<OrderLine>> orderTriplet : orders) {
            final Customer cust = orderTriplet.getValue0();
            final Order order = orderTriplet.getValue1();
            final List<OrderLine> orderLines = orderTriplet.getValue2();
            cypher.append(String.format(CREATE_O_TMPL, cust.getId(), order.getId(), order));
            for (final OrderLine ol : orderLines) {
                cypher.append(String.format(CREATE_OL_TMPL, order.getId(), ol.getId(), ol));
            }
        }
        return cypher.toString();
    }
}
