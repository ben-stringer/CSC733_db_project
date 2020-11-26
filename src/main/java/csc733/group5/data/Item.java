package csc733.group5.data;

import csc733.group5.RandomDataGenerator;
import org.neo4j.driver.types.Node;

public interface Item {

    int getId();
    String getName();
    double getPrice();
    String getData();

    String toCypherCreateString();

    static final String I_TMPL =
            "{ i_id : %d, " +
                    "i_name : '%s', " +
                    "i_price : %f," +
                    "i_data : '%s' }";

    static Item from(final int id, final RandomDataGenerator rdg) {
        final String name = rdg.randomWord(6,16);
        final double price = rdg.rand().nextDouble() * 10000;
        final String data = rdg.randomWord(16,256);
        return new Item() {
            @Override public int getId() { return id; }
            @Override public String getName() { return name; }
            @Override public double getPrice() { return price; }
            @Override public String getData() { return data; }
            @Override public String toCypherCreateString() {
                return String.format(I_TMPL, id, name, price, data);
            }
        };
    }
    static Item from(final int id, final String name, final double price, final String data) {
        return new Item() {
            @Override public int getId() { return id; }
            @Override public String getName() { return name; }
            @Override public double getPrice() { return price; }
            @Override public String getData() { return data; }
            @Override public String toCypherCreateString() {
                return String.format(I_TMPL, id, name, price, data);
            }
        };
    }
    static Item from(final Node node) {
        return from(
                node.get("i_id").asInt(),
                node.get("i_name").asString(),
                node.get("i_price").asDouble(),
                node.get("i_data").asString()
        );
    }
}
