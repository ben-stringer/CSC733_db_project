package csc733.group5.data;

import csc733.group5.RandomDataGenerator;

public interface Item {

    int getId();
    String getName();
    double getPrice();
    String getData();

    static final String I_TMPL =
            "{ i_id : 'i_%d', " +
                    "i_name : '%s', " +
                    "i_price : %f," +
                    "i_data : '%s' }";

    static Item from(final int id, final RandomDataGenerator rdg) {
        final String name = rdg.randomWord(6,16);
        final int price = Math.abs(rdg.rand().nextInt(10000));
        final String data = rdg.randomWord(16,256);
        return new Item() {
            @Override public int getId() { return id; }
            @Override public String getName() { return name; }
            @Override public double getPrice() { return price; }
            @Override public String getData() { return data; }
            @Override public String toString() { return
                String.format(I_TMPL, id, name, price, data);
            }
        };
    }
}
