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
    int getNextOIdAndIncrement();

    String toCypherCreateString();

    static final String D_TMPL =
            "{ d_id : %d, " +
                    "d_name : '%s', " +
                    "d_street_1 : '%s', " +
                    "d_street_2 : '%s', " +
                    "d_city : '%s', " +
                    "d_state : '%s', " +
                    "d_zip : %d, " +
                    "d_tax : %f, " +
                    "d_ytd : %f, " +
                    "d_next_o_id : %d }";

    static final class DistrictPojo implements District {
        private final int id;
        private final String name;
        private final String street1;
        private final String street2;
        private final String city;
        private final String state;
        private final int zip;
        private final double tax;
        private final double ytd;
        private int nextOId = 0;

        DistrictPojo(final int _id, final RandomDataGenerator rdg) {
            id = _id;
            name = rdg.randomWord(6, 16);
            street1 = rdg.randomStreet();
            street2 = rdg.rand().nextBoolean() ? rdg.randomStreet() : "";
            city = rdg.randomWord();
            state = rdg.randomWord(2);
            zip = rdg.randomZip();
            tax = rdg.rand().nextDouble() / 10;
            ytd = rdg.rand().nextDouble() * 1000000000;
        }
        @Override public int getId() { return id; }
        @Override public String getName() { return name; }
        @Override public String getStreet1() { return street1; }
        @Override public String getStreet2() { return street2; }
        @Override public String getCity() { return city; }
        @Override public String getState() { return state; }
        @Override public int getZip() { return zip; }
        @Override public double getTax() { return tax; }
        @Override public double getYtd() { return ytd; }
        @Override public int getNextOId() { return nextOId; }
        @Override public int getNextOIdAndIncrement() { return nextOId++; }
        @Override public String toCypherCreateString() {
            return String.format(D_TMPL, id, name, street1, street2,
                    city, state, zip, tax, ytd, nextOId);
        }
    }

    static District from(final int id, final RandomDataGenerator rdg) {
        return new DistrictPojo(id, rdg);
    }
}
