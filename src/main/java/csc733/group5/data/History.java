package csc733.group5.data;

import csc733.group5.RandomDataGenerator;

public interface History {

    String getDate();
    double getAmount();
    String getData();

    String toCypherCreateString();

    static final String H_TMPL =
            "{ h_date : '%s', " +
                    "h_amount : %f, " +
                    "h_data : '%s' }";

    static History from(final double amount, final RandomDataGenerator rdg) {
        final String date = rdg.randomDate();
        final String data = rdg.randomWord();
        return new History() {
            @Override public String getDate() { return date; }
            @Override public double getAmount() { return amount; }
            @Override public String getData() { return data; }
            @Override public String toCypherCreateString() { return String.format(H_TMPL, date, amount, data); }
        };
    }
}
