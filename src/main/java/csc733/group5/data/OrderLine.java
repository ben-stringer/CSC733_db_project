package csc733.group5.data;

import csc733.group5.RandomDataGenerator;

public interface OrderLine {

    int getId();
    String getDeliveryDate();
    int getQuantity();
    double getAmount();
    String getDistInfo();

    static final String OL_TMPL =
            "{ ol_id : 'd_%s', " +
                    "ol_delivery_d : '%s', " +
                    "ol_quantity : %d, " +
                    "ol_amount : %f, " +
                    "ol_dist_info : '%s' }";

    static OrderLine from(final int id, final Item item, final String distInfo, final RandomDataGenerator rdg) {
        final String deliveryDate = rdg.randomDate();
        final int quantity = rdg.rand().nextInt(100);
        final double amount = item.getPrice() * quantity;
        return new OrderLine() {
            @Override public int getId() { return id; }
            @Override public String getDeliveryDate() { return deliveryDate; }
            @Override public int getQuantity() { return quantity; }
            @Override public double getAmount() { return amount; }
            @Override public String getDistInfo() { return distInfo; }
            @Override public String toString() {
                return String.format(OL_TMPL, id, deliveryDate, quantity, amount, distInfo);
            }
        };
    }
}
