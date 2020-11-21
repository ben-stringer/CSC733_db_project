package csc733.group5.data;

import csc733.group5.RandomDataGenerator;

public interface Customer {
    int getId();
    String getFirst();
    String getMiddle();
    String getLast();
    String getStreet_1();
    String getStreet_2();
    String getCity();
    String getState();
    int getZip();
    String getPhone();
    String getSince();
    String getCredit();
    int getCreditLim();
    double getDiscount();
    int getBalance();
    int getYtdPayment();
    int getPaymentCnt();
    int getDeliveryCnt();
    String getData();

    static final String C_TMPL =
            "{ c_id : %d, " +
                    "c_first : '%s', " +
                    "c_middle : '%s', " +
                    "c_last : '%s', " +
                    "c_street_1 : '%s', " +
                    "c_street_2 : '%s', " +
                    "c_city : '%s', " +
                    "c_state : '%s', " +
                    "c_zip : %d, " +
                    "c_phone : '%s', " +
                    "c_since : '%s', " +
                    "c_credit : '%s', " +
                    "c_credit_lim : %d, " +
                    "c_discount : %d, " +
                    "c_balance : %d, " +
                    "c_ytd_payment : %d, " +
                    "c_payment_cnt : '%s', " +
                    "c_delivery_cnt : '%s', " +
                    "c_data : '%s' }";

    static Customer from(final int id, final RandomDataGenerator rdg) {
        final String first = rdg.randomWord();
        final String middle = rdg.randomWord();
        final String last = rdg.randomWord();
        final String street1 = rdg.randomStreet();
        final String street2 = rdg.rand().nextBoolean() ? rdg.randomStreet() : "";
        final String city = rdg.randomWord();
        final String state = rdg.randomWord(2);
        final int zip = rdg.randomZip();
        final String phone = rdg.randomPhone();
        final String since = rdg.randomDate();
        final String credit = rdg.rand().nextBoolean() ? "GC": "BC";
        final int creditLim = rdg.rand().nextInt(4000)+1000;
        final double discount = rdg.rand().nextDouble()/10;
        final int balance = rdg.rand().nextInt(1000)+1000;
        final int ytd = rdg.rand().nextInt(5000);
        final int paymentCnt = 0;
        final int deliveryCnt = 0;
        final String data = "";
        return new Customer() {
            @Override public int getId() { return id; }
            @Override public String getFirst() { return first; }
            @Override public String getMiddle() { return middle; }
            @Override public String getLast() { return last; }
            @Override public String getStreet_1() { return street1; }
            @Override public String getStreet_2() { return street2; }
            @Override public String getCity() { return city; }
            @Override public String getState() { return state; }
            @Override public int getZip() { return zip; }
            @Override public String getPhone() { return phone; }
            @Override public String getSince() { return since; }
            @Override public String getCredit() { return credit; }
            @Override public int getCreditLim() { return creditLim; }
            @Override public double getDiscount() { return discount; }
            @Override public int getBalance() { return balance; }
            @Override public int getYtdPayment() { return ytd; }
            @Override public int getPaymentCnt() { return paymentCnt; }
            @Override public int getDeliveryCnt() { return deliveryCnt; }
            @Override public String getData() { return data; }
            @Override public String toString() {
                return String.format(C_TMPL, id, first, middle, last, street1, street2,
                        city, state, zip, phone, since, credit, creditLim, discount,
                        balance, ytd, paymentCnt, deliveryCnt, data);
            }
        };
    }
}
