package csc733.group5.data;

import csc733.group5.RandomDataGenerator;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomInitialState {

    private static final int NUM_CUST_PER_DIST =  3000;
//            10;
    private static final int NUM_ITEMS =  10000;
//            200;
    private static final int NUM_ORDERS =  30000;
//            200;
    private static final int NUM_CUSTOMERS = 10*NUM_CUST_PER_DIST;

    private static final String CREATE_WH_TMPL = "create (w:Warehouse %s)";
    private static final String CREATE_D_TMPL = "create (w)-[:W_SVC_D]->(d_%d:District %s)";
    private static final String CREATE_I_S_TMPL = "create (w)-[:WH_STOCK]->(:Stock %s)<-[:I_STOCK]-(i_%d:Item %s)";
    private static final String CREATE_C_TMPL = "create (d_%d)-[:D_SVC_C]->(c_%d:Customer %s)";
    private static final String CREATE_O_TMPL = "create (c_%d)-[:PLACED_ORDER]->(o_%d:Order %s)";
    private static final String CREATE_OL_TMPL = "create (o_%d)-[:ORDER_LINE]->(:OrderLine %s)";
    private static final String CREATE_H_TMPL = "create (c_%d)-[:PREV_ORDER]->(:History %s)";

    private final Warehouse warehouse;
    private final List<District> districts = new ArrayList<>(10);
    private final List<Pair<Customer, Integer>> customers = new ArrayList<>(NUM_CUSTOMERS);
    private final List<Pair<Item, Stock>> itemStock = new ArrayList<>(NUM_ITEMS);
    private final List<Triplet<Customer, Order, List<OrderLine>>> orders = new ArrayList<>(NUM_ORDERS);
    private final List<Pair<Customer, History>> history = new ArrayList<>(NUM_ORDERS);

    private RandomInitialState(final RandomDataGenerator rdg) {
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
            final double amt = order.getValue1().stream().collect(Collectors.summingDouble(OrderLine::getAmount));
            cust.getValue0().setBalance(cust.getValue0().getBalance() + amt);
            orders.add(Triplet.with(cust.getValue0(), order.getValue0(), order.getValue1()));
            history.add(Pair.with(cust.getValue0(), History.from(amt, rdg)));
        }
    }

    public static String newCypherCreateString(final RandomDataGenerator rdg) {
        final RandomInitialState ris = new RandomInitialState(rdg);
        return new StringBuilder()
                .append(String.format(CREATE_WH_TMPL, ris.warehouse))
                .append(ris.districts.stream()
                        .map(d -> String.format(CREATE_D_TMPL, d.getId(), d))
                        .collect(Collectors.joining("\n")))
                .append(ris.customers.stream()
                        .map(custDistPair -> {
                            final int dId = custDistPair.getValue1();
                            final Customer cust = custDistPair.getValue0();
                            return String.format(CREATE_C_TMPL, dId, cust.getId(), cust);
                        }).collect(Collectors.joining("\n")))
                .append(ris.itemStock.stream()
                        .map(itemStockPair -> {
                            final Item item = itemStockPair.getValue0();
                            final Stock stock = itemStockPair.getValue1();
                            return String.format(CREATE_I_S_TMPL, stock, item.getId(), item);
                        }))
                .append(ris.orders.stream()
                        .map(orderTriplet -> {
                            final Customer cust = orderTriplet.getValue0();
                            final Order order = orderTriplet.getValue1();
                            final List<OrderLine> orderLines = orderTriplet.getValue2();
                            return Stream.of(String.format(CREATE_O_TMPL, cust.getId(), order.getId(), order),
                                    orderLines.stream().map(ol -> String.format(CREATE_OL_TMPL, order.getId(), ol))
                                            .collect(Collectors.joining("\n")));
                        }))
                .append(ris.history.stream()
                        .map(custHistPair ->
                                String.format(CREATE_H_TMPL, custHistPair.getValue0(), custHistPair.getValue1()))
                        .collect(Collectors.joining("\n")))
                .toString();
    }
}
