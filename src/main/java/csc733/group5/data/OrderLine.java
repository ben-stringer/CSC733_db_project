package csc733.group5.data;

import org.neo4j.driver.types.Node;

public class OrderLine {

    static final String OL_TMPL =
            "{ ol_delivery_d : '%s', " +
                    "ol_quantity : %d, " +
                    "ol_amount : %f, " +
                    "ol_dist_info : '%s' }";

    public static OrderLine from(final Item item, final String deliveryDate, final String distInfo, final int quantity) {
        //      - The amount for the item in the order (OL_AMOUNT) is computed as: OL_QUANTITY*I_PRICE
        return new OrderLine(deliveryDate, quantity, item.getPrice() * quantity, distInfo);
    }

    public static OrderLine from(final Node node) {
        return new OrderLine(
                node.get("ol_delivery_d").asString(),
                node.get("ol_quantity").asInt(),
                node.get("ol_amount").asDouble(),
                node.get("ol_dist_info").asString());
    };

    private String deliveryDate;
    private int quantity;
    private double amount;
    private String distInfo;

    public OrderLine(final String _deliveryDate, final int _quantity, final double _amount, final String _distInfo) {
        deliveryDate = _deliveryDate;
        quantity = _quantity;
        amount = _amount;
        distInfo = _distInfo;
    }

    public String getDeliveryDate() { return deliveryDate; }
    public int getQuantity() { return quantity; }
    public double getAmount() { return amount; }
    public String getDistInfo() { return distInfo; }

    public OrderLine setDeliveryDate(final String _deliveryDate) { deliveryDate = _deliveryDate; return this; }
    public OrderLine setQuantity(final int _quantity) { quantity = _quantity; return this; }
    public OrderLine setAmount(final double _amount) { amount = _amount; return this; }
    public OrderLine setDistInfo(final String _distInfo) { distInfo = _distInfo; return this; }

    public String toCypherCreateString() {
        return String.format(OL_TMPL, getDeliveryDate(), getQuantity(), getAmount(), getDistInfo());
    }


}
