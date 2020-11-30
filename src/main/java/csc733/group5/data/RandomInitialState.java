package csc733.group5.data;

import com.codepoetics.protonpack.StreamUtils;
import csc733.group5.App;
import csc733.group5.RandomDataGenerator;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomInitialState {

    public static final int NUM_CUST_PER_DIST =  3000;
//            10;
    public static final int NUM_ITEMS =  100000;
//            200;
    public static final int NUM_ORDERS =  30000;
//            200;

    private static final String CREATE_WH_TMPL = "create (w:Warehouse %s)";
    private static final String CREATE_D_TMPL = "create (w)-[:W_SVC_D]->(d_%d:District %s)";
    private static final String CREATE_I_S_TMPL = "create (w)-[:WH_STOCK]->(:Stock %s)<-[:IN_STOCK]-(i_%d:Item %s)";
    private static final String CREATE_C_TMPL = "create (d_%d)-[:D_SVC_C]->(c_%d:Customer %s)";
    private static final String CREATE_O_TMPL = "create (c_%d)-[:PLACED_ORDER]->(o_%d:Order %s)";
    private static final String CREATE_OL_TMPL = "create (o_%d)-[:ORDER_LINE]->(:OrderLine %s)-[:OL_ITEM]->(i_%d)";
    private static final String CREATE_H_TMPL = "create (c_%d)-[:PREV_ORDER]->(:History %s)";

    private final Warehouse warehouse;
    private final List<District> districts = new ArrayList<>(10);
    private final Map<District, List<Customer>> customers = new HashMap<>();
    private final List<Pair<Item, Stock>> itemStock = new ArrayList<>(NUM_ITEMS);
    private final List<Quartet<Customer, Order, List<Pair<OrderLine,Item>>, History>> orders = new ArrayList<>(NUM_ORDERS);

    private RandomInitialState(final RandomDataGenerator rdg) {
        warehouse = Warehouse.from(1, rdg);
        for (int di = 0; di < 10; di++) {
            final District d = District.from(di, rdg);
            districts.add(d);
            customers.put(d, IntStream.range(0, NUM_CUST_PER_DIST)
                    .mapToObj(ci -> Customer.from(ci, d, rdg))
                    .collect(Collectors.toList()));
        }
        for (int i = 0; i < NUM_ITEMS; i++) {
            final Item item = Item.from(i, rdg);
            final Stock stock = Stock.from(rdg);
            itemStock.add(Pair.with(item, stock));
        }
        for (int i = 0; i < NUM_ORDERS; i++) {
            final District dist = districts.get(rdg.rand().nextInt(10));
            final Customer cust = customers.get(dist).get(rdg.rand().nextInt(NUM_CUST_PER_DIST));
            final int oId = dist.getNextOIdAndIncrement() * 10 + dist.getId();
            final Pair<Order, List<Pair<OrderLine, Item>>> order = Order.from(oId, itemStock, dist.getId(), rdg);
            final double amt = order.getValue1().stream()
                    .map(Pair::getValue0)
                    .mapToDouble(OrderLine::getAmount)
                    .sum();
            cust.setBalance(cust.getBalance() + amt);
            orders.add(Quartet.with(cust, order.getValue0(), order.getValue1(), History.from(amt, rdg)));
        }
    }

    public static Pair<Stream<String>,Long> newCypherCreateQueryChunked(final RandomDataGenerator rdg) {
        final RandomInitialState ris = new RandomInitialState(rdg);
        final Stream.Builder<String> queryChunks = Stream.builder();
        long totalChunkCount = 0;
        {
            final Stream.Builder<String> chunk = Stream.builder();
            chunk.add(String.format(
                    "create (w:Warehouse %s)",
                    ris.warehouse.toCypherCreateString()));
            for (int i = 0; i < ris.districts.size(); i++) {
                chunk.add(String.format(
                        "create (w)-[:W_SVC_D]->(d_%d:District %s)",
                        i, ris.districts.get(i).toCypherCreateString()));
            }
            queryChunks.add(chunk.build().collect(Collectors.joining("\n")));
            totalChunkCount++;
        }
        queryChunks.add("create index if not exists for (w:Warehouse) on (w.w_id)");
        queryChunks.add("create index if not exists for (d:District) on (d.d_id)");
        totalChunkCount += 2;
        for (int di = 0; di < ris.districts.size(); di++) {
            final List<Customer> dCustomers = ris.customers.get(ris.districts.get(di));
            final String chunkPrefix = String.format("match (d:District { d_id : %d })", di);
            Stream.Builder<String> chunk = Stream.<String>builder().add(chunkPrefix);
            int currentChunkCount = 0;
            for (int ci = 0; ci < NUM_CUST_PER_DIST; ci++) {
                if (currentChunkCount == 250) {
                    queryChunks.add(chunk.build().collect(Collectors.joining("\n")));
                    totalChunkCount++;
                    currentChunkCount = 0;
                    chunk = Stream.<String>builder().add(chunkPrefix);
                }
                chunk.add(String.format(
                        "create (d)-[:D_SVC_C]->(:Customer %s)",
                        dCustomers.get(ci).toCypherCreateString()));
                currentChunkCount++;
            }
            queryChunks.add(chunk.build().collect(Collectors.joining("\n")));
            totalChunkCount++;
        }
        queryChunks.add("create index if not exists for (c:Customer) on (c.c_id)");
        totalChunkCount++;
        {
            final String chunkPrefix = "match (w:Warehouse { w_id : 1 })";
            Stream.Builder<String> chunk = Stream.<String>builder().add(chunkPrefix);
            int currentChunkCount = 0;
            for (final Pair<Item, Stock> itemStockPair : ris.itemStock) {
                if (currentChunkCount == 250) {
                    queryChunks.add(chunk.build().collect(Collectors.joining("\n")));
                    totalChunkCount++;
                    currentChunkCount = 0;
                    chunk = Stream.<String>builder().add(chunkPrefix);
                }
                final Item item = itemStockPair.getValue0();
                final Stock stock = itemStockPair.getValue1();
                chunk.add(String.format(
                        "create (w)-[:WH_STOCK]->(:Stock %s)<-[:IN_STOCK]-(:Item %s)",
                        stock.toCypherCreateString(), item.toCypherCreateString()));
                currentChunkCount++;
            }
            queryChunks.add(chunk.build().collect(Collectors.joining("\n")));
            totalChunkCount++;
        }
        queryChunks.add("create index if not exists for (i:Item) on (i.i_id)");
        totalChunkCount++;
        for (final Quartet<Customer, Order, List<Pair<OrderLine,Item>>,History> custQuartet : ris.orders) {
            final Stream.Builder<String> chunk = Stream.builder();
            final Customer cust = custQuartet.getValue0();
            final District dist = cust.getDistrict();
            final Order order = custQuartet.getValue1();
            final List<Pair<OrderLine, Item>> orderLines = custQuartet.getValue2();
            final History hist = custQuartet.getValue3();
            chunk.add(String.format(
                    "match (:Warehouse {w_id : %d})-[:W_SVC_D]->(:District { d_id : %d})-[:D_SVC_C]->(c:Customer { c_id : %d }) ",
                    1, dist.getId(), cust.getId()));
            chunk.add(String.format(
                    "create (c)-[:PLACED_ORDER]->(o:Order %s)",
                    order.toCypherCreateString()));
            chunk.add(String.format(
                    "create (c)-[:CUST_HIST]->(:History %s)", hist.toCypherCreateString()));
            for (final Pair<OrderLine, Item> olItemPair : orderLines) {
                final OrderLine ol = olItemPair.getValue0();
                final Item item = olItemPair.getValue1();
                chunk.add(String.format(
                        "with o match (i:Item { i_id : %d }) create (o)-[:ORDER_LINE]->(:OrderLine %s)-[:OL_ITEM]->(i)",
                        item.getId(), ol.toCypherCreateString()));
            }
            queryChunks.add(chunk.build().collect(Collectors.joining("\n")));
            totalChunkCount++;
        }
        return Pair.with(queryChunks.build(), totalChunkCount);
    }

    public static void main(String[] args) {
        System.out.println("Hello CSC733 World");
        final long startTime = System.currentTimeMillis();
        final RandomDataGenerator rdg = new RandomDataGenerator(42);

        try (final Driver driver = App.startDriver()) {
            try (final Session session = driver.session()) {
                try (final Transaction tx = session.beginTransaction()) {
                    // Clear everything already in the database
                    System.out.println("Clearing the existing data");
                    tx.run("match (n) detach delete n");

                    tx.commit();
                }

                System.out.println("Creating new DB content");
                final Pair<Stream<String>,Long> chunkCountPair = RandomInitialState.newCypherCreateQueryChunked(rdg);
                final Stream<String> txChunks = chunkCountPair.getValue0();
                final long totalChunks = chunkCountPair.getValue1();

                StreamUtils.zipWithIndex(txChunks).forEach(indexedChunk -> {
                    final String query = indexedChunk.getValue();
                    final long chunkIndex = indexedChunk.getIndex();
                    System.out.format("Executing query chunk:\n%s\nCompleted %d of %d (%f%%).\n",
                            query, chunkIndex, totalChunks, ((double)chunkIndex/totalChunks)*100);
                    try (final Transaction tx = session.beginTransaction()) {
                        tx.run(query);
                        System.out.println("Committing query chunk");
                        tx.commit();
                    } catch (final Exception x) {
                        System.err.println("Caught exception executing query:\n" + query);
                        throw x;
                    }
                });

                System.out.println("Closing session");
            }
            System.out.println("Closing driver");
        }
        final long endTime = System.currentTimeMillis();
        System.out.format("Driver closed.  Ingest took %f seconds.\n", (endTime - startTime)*1000.0);
    }
}
