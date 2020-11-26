package csc733.group5.data;

import csc733.group5.RandomDataGenerator;

public interface Warehouse {
    int getId();
    String getName();
    String getStreet1();
    String getStreet2();
    String getCity();
    String getState();
    int getZip();
    double getTax();
    double getYtd();

    String toCypherCreateString();

    static final String WH_TMPL =
            "{ w_id : %d, " +
                    "w_name : '%s', " +
                    "w_street_1 : '%s', " +
                    "w_street_2 : '%s', " +
                    "w_city : '%s', " +
                    "w_state : '%s', " +
                    "w_zip : %d, " +
                    "w_tax : %f, " +
                    "w_ytd : %f }";

    static Warehouse from(final int id, final RandomDataGenerator rdg) {
        final String name = rdg.randomWord(6,16);
        final String street1 = rdg.randomStreet();
        final String street2 = rdg.rand().nextBoolean() ? rdg.randomStreet() : "";
        final String city = rdg.randomWord();
        final String state = rdg.randomWord(2);
        final int zip = rdg.randomZip();
        final double tax = rdg.rand().nextDouble() / 10;
        final double ytd = rdg.rand().nextDouble() * 1000000000;
        return new Warehouse() {
            @Override public int getId() { return id; }
            @Override public String getName() { return name; }
            @Override public String getStreet1() { return street1; }
            @Override public String getStreet2() { return street2; }
            @Override public String getCity() { return city; }
            @Override public String getState() { return state; }
            @Override public int getZip() { return zip; }
            @Override public double getTax() { return tax; }
            @Override public double getYtd() { return ytd; }
            @Override public String toCypherCreateString() {
                return String.format(WH_TMPL, id, name, street1, street2,
                        city, state, zip, tax, ytd);
            }
        };
    }
}
