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
    double getBalance();
    void setBalance(double newBalance);
    double getYtdPayment();
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
                    "c_discount : %f, " +
                    "c_balance : %f, " +
                    "c_ytd_payment : %f, " +
                    "c_payment_cnt : '%s', " +
                    "c_delivery_cnt : '%s', " +
                    "c_data : '%s' }";

    static Customer from(final int id, final RandomDataGenerator rdg) {
        return new PojoCustomer(id, rdg);
    }

    class PojoCustomer implements Customer {
        private final int id;
        private final String first;
        private final String middle;
        private final String last;
        private final String street1;
        private final String street2;
        private final String city;
        private final String state;
        private final int zip;
        private final String phone;
        private final String since;
        private final String credit;
        private final int creditLim;
        private final double discount;
        private double balance = 0;
        private final double ytd;
        private final int paymentCnt = 0;
        private final int deliveryCnt = 0;
        private final String data = "";

        PojoCustomer(final int _id, final RandomDataGenerator rdg) {
            id = _id;
            first = rdg.randomWord();
            middle = rdg.randomWord();
            last = rdg.randomWord();
            street1 = rdg.randomStreet();
            street2 = rdg.rand().nextBoolean() ? rdg.randomStreet() : "";
            city = rdg.randomWord();
            state = rdg.randomWord(2);
            zip = rdg.randomZip();
            phone = rdg.randomPhone();
            since = rdg.randomDate();
            credit = rdg.rand().nextBoolean() ? "GC": "BC";
            creditLim = rdg.rand().nextInt(4000)+1000;
            discount = rdg.rand().nextDouble()/10;
            ytd = rdg.rand().nextDouble() * 5000;
        }

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
        @Override public double getBalance() { return balance; }
        @Override public void setBalance(double newBalance) { balance = newBalance; }
        @Override public double getYtdPayment() { return ytd; }
        @Override public int getPaymentCnt() { return paymentCnt; }
        @Override public int getDeliveryCnt() { return deliveryCnt; }
        @Override public String getData() { return data; }
        @Override public String toString() {
            return String.format(C_TMPL, id, first, middle, last, street1, street2,
                    city, state, zip, phone, since, credit, creditLim, discount,
                    balance, ytd, paymentCnt, deliveryCnt, data);
        }
    }
}
