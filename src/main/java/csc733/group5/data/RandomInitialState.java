package csc733.group5.data;

import csc733.group5.App;
import csc733.group5.RandomDataGenerator;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomInitialState {

    public static final int NUM_CUST_PER_DIST = // 3000;
            10;
    public static final int NUM_ITEMS = // 100000;
            200;
    public static final int NUM_ORDERS = // 30000;
            200;
    public static final int NUM_CUSTOMERS = 10*NUM_CUST_PER_DIST;

    private static final String CREATE_WH_TMPL = "create (w:Warehouse %s)";
    private static final String CREATE_D_TMPL = "create (w)-[:W_SVC_D]->(d_%d:District %s)";
    private static final String CREATE_I_S_TMPL = "create (w)-[:WH_STOCK]->(:Stock %s)<-[:IN_STOCK]-(i_%d:Item %s)";
    private static final String CREATE_C_TMPL = "create (d_%d)-[:D_SVC_C]->(c_%d:Customer %s)";
    private static final String CREATE_O_TMPL = "create (c_%d)-[:PLACED_ORDER]->(o_%d:Order %s)";
    private static final String CREATE_OL_TMPL = "create (o_%d)-[:ORDER_LINE]->(:OrderLine %s)-[:OL_ITEM]->(i_%d)";
    private static final String CREATE_H_TMPL = "create (c_%d)-[:PREV_ORDER]->(:History %s)";

    private final Warehouse warehouse;
    private final List<District> districts = new ArrayList<>(10);
    private final List<Pair<Customer, Integer>> customers = new ArrayList<>(NUM_CUSTOMERS);
    private final List<Pair<Item, Stock>> itemStock = new ArrayList<>(NUM_ITEMS);
    private final List<Triplet<Customer, Order, List<Pair<OrderLine,Item>>>> orders = new ArrayList<>(NUM_ORDERS);
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
            final District dist = districts.get(cust.getValue1());
            final int oId = dist.getNextOIdAndIncrement() * 10 + dist.getId();
            final Pair<Order, List<Pair<OrderLine, Item>>> order = Order.from(oId, itemStock, cust.getValue1(), rdg);
            final double amt = order.getValue1().stream()
                    .map(Pair::getValue0)
                    .mapToDouble(OrderLine::getAmount)
                    .sum();
            cust.getValue0().setBalance(cust.getValue0().getBalance() + amt);
            orders.add(Triplet.with(cust.getValue0(), order.getValue0(), order.getValue1()));
            history.add(Pair.with(cust.getValue0(), History.from(amt, rdg)));
        }
    }

    public static String newCypherCreateString(final RandomDataGenerator rdg) {
        final RandomInitialState ris = new RandomInitialState(rdg);
        return new StringBuilder()
                .append(String.format(CREATE_WH_TMPL, ris.warehouse.toCypherCreateString())).append('\n')
                .append(ris.districts.stream()
                        .map(d -> String.format(CREATE_D_TMPL, d.getId(), d.toCypherCreateString()))
                        .collect(Collectors.joining("\n"))).append('\n')
                .append(ris.customers.stream()
                        .map(custDistPair -> {
                            final int dId = custDistPair.getValue1();
                            final Customer cust = custDistPair.getValue0();
                            return String.format(CREATE_C_TMPL, dId, cust.getId(), cust.toCypherCreateString());
                        }).collect(Collectors.joining("\n"))).append('\n')
                .append(ris.itemStock.stream()
                        .map(itemStockPair -> {
                            final Item item = itemStockPair.getValue0();
                            final Stock stock = itemStockPair.getValue1();
                            return String.format(CREATE_I_S_TMPL,
                                    stock.toCypherCreateString(), item.getId(),
                                    item.toCypherCreateString());
                        }).collect(Collectors.joining("\n"))).append('\n')
                .append(ris.orders.stream()
                        .map(orderTriplet -> {
                            final Customer cust = orderTriplet.getValue0();
                            final Order order = orderTriplet.getValue1();
                            final List<Pair<OrderLine,Item>> pairOlItems = orderTriplet.getValue2();
                            return Stream.of(String.format(CREATE_O_TMPL, cust.getId(), order.getId(), order.toCypherCreateString()),
                                    pairOlItems.stream()
                                            .map(olItem -> {
                                                final OrderLine ol = olItem.getValue0();
                                                final Item item = olItem.getValue1();
                                                return String.format(CREATE_OL_TMPL, order.getId(), ol.toCypherCreateString(), item.getId());
                                            })
                                            .collect(Collectors.joining("\n")))
                                    .collect(Collectors.joining("\n"));
                        }).collect(Collectors.joining("\n"))).append('\n')
                .append(ris.history.stream()
                        .map(custHistPair ->
                                String.format(CREATE_H_TMPL, custHistPair.getValue0().getId(), custHistPair.getValue1().toCypherCreateString()))
                        .collect(Collectors.joining("\n")))
                .toString();
    }

    public static void main(String[] args) {
        System.out.println("Hello CSC733 World");

        final String cypherText = RandomInitialState.newCypherCreateString(new RandomDataGenerator(42));

        System.out.println("Submitting the following cypher:");
        System.out.println("********************************************************************************");
        System.out.println(cypherText);
        System.out.println("********************************************************************************");


        try (final Driver driver = App.startDriver()) {
            try (final Session session = driver.session()) {
                try (final Transaction tx = session.beginTransaction()) {
                    // Clear everything already in the database
                    System.out.println("Clearing the existing data");
                    tx.run("match (n) detach delete n");

                    System.out.println("Executing query");
                    tx.run(cypherText);

                    System.out.println("Query execution completed; attempting to commit");
                    tx.commit();
                    System.out.println("Commit completed");
                }
                System.out.println("Closing session");
            }
            System.out.println("Closing driver");
        }
        System.out.println("Driver closed, application exiting");
    }
}
