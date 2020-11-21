package csc733.group5.data;

import csc733.group5.RandomDataGenerator;

public interface Stock {
    int getQuantity();
    String getDist_01();
    String getDist_02();
    String getDist_03();
    String getDist_04();
    String getDist_05();
    String getDist_06();
    String getDist_07();
    String getDist_08();
    String getDist_09();
    String getDist_10();
    double getYtd();
    int getOrderCnt();
    int getRemoteCnt();
    String getData();

    static final String S_TMPL =
            "{ s_quantity : '%d', " +
                    "s_dist_01 : '%s', " +
                    "s_dist_02 : '%s', " +
                    "s_dist_03 : '%s', " +
                    "s_dist_04 : '%s', " +
                    "s_dist_05 : '%s', " +
                    "s_dist_06 : '%s', " +
                    "s_dist_07 : '%s', " +
                    "s_dist_08 : '%s', " +
                    "s_dist_09 : '%s', " +
                    "s_dist_10 : '%s', " +
                    "s_ytd : '%s' " +
                    "s_order_cnt : %d " +
                    "s_remote_cnt : %d " +
                    "s_data : '%s' }";

    static Stock from(final RandomDataGenerator rdg) {
        final int quantity = rdg.rand().nextInt(99)+1;
        final String dist_01 = rdg.randomWord(24);
        final String dist_02 = rdg.randomWord(24);
        final String dist_03 = rdg.randomWord(24);
        final String dist_04 = rdg.randomWord(24);
        final String dist_05 = rdg.randomWord(24);
        final String dist_06 = rdg.randomWord(24);
        final String dist_07 = rdg.randomWord(24);
        final String dist_08 = rdg.randomWord(24);
        final String dist_09 = rdg.randomWord(24);
        final String dist_10 = rdg.randomWord(24);
        final double ytd = rdg.rand().nextDouble()*10000;
        final int orderCnt = rdg.rand().nextInt();
        final int remoteCnt = rdg.rand().nextInt();
        final String data = rdg.randomWord(26,50);
        return new Stock() {
            @Override public int getQuantity() { return quantity; }
            @Override public String getDist_01() { return dist_01; }
            @Override public String getDist_02() { return dist_02; }
            @Override public String getDist_03() { return dist_03; }
            @Override public String getDist_04() { return dist_04; }
            @Override public String getDist_05() { return dist_05; }
            @Override public String getDist_06() { return dist_06; }
            @Override public String getDist_07() { return dist_07; }
            @Override public String getDist_08() { return dist_08; }
            @Override public String getDist_09() { return dist_09; }
            @Override public String getDist_10() { return dist_10; }
            @Override public double getYtd() { return ytd; }
            @Override public int getOrderCnt() { return orderCnt; }
            @Override public int getRemoteCnt() { return remoteCnt; }
            @Override public String getData() { return data; }
            @Override public String toString() {
                return String.format(S_TMPL, quantity, dist_01, dist_02, dist_03,
                        dist_04, dist_05, dist_06, dist_07, dist_08, dist_09,
                        dist_10, ytd, orderCnt, remoteCnt, data);
            }
        };
    }
}
