package csc733.group5.data;

import csc733.group5.RandomDataGenerator;

public interface District {

    int getId();
    String getName();
    String getStreet1();
    String getStreet2();
    String getCity();
    String getState();
    int getZip();
    double getTax();
    double getYtd();
    int getNextOId();

    static final String D_TMPL =
            "{ d_id : 'd_%s', " +
                    "d_name : '%s', " +
                    "d_street_1 : '%s', " +
                    "d_street_2 : '%s', " +
                    "d_city : '%s', " +
                    "d_state : '%s', " +
                    "d_zip : %d, " +
                    "d_tax : %f, " +
                    "d_ytd : %f, " +
                    "d_next_o_id : %d }";

    static District from(final int id, final RandomDataGenerator rdg) {
        final String name = rdg.randomWord(6,16);
        final String street1 = rdg.randomStreet();
        final String street2 = rdg.rand().nextBoolean() ?
                rdg.randomStreet() : "";
        final String city = rdg.randomWord();
        final String state = rdg.randomWord(2);
        final int zip = rdg.randomZip();
        final double tax = rdg.rand().nextDouble() * 1000;
        final double ytd = rdg.rand().nextDouble() * 1000000000;
        return new District() {
            @Override public int getId() { return id; }
            @Override public String getName() { return name; }
            @Override public String getStreet1() { return street1; }
            @Override public String getStreet2() { return street2; }
            @Override public String getCity() { return city; }
            @Override public String getState() { return state; }
            @Override public int getZip() { return zip; }
            @Override public double getTax() { return tax; }
            @Override public double getYtd() { return ytd; }
            @Override public int getNextOId() { return 0; }
            @Override public String toString() {
                return String.format(D_TMPL, id, name, street1, street2,
                        city, state, zip, tax, ytd, 0);
            }
        };
    }
}
