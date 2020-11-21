package csc733.group5.data;

import csc733.group5.RandomDataGenerator;

public interface Stock {
    int getQuantity();
    String getDist(int num);
    double getYtd();
    int getOrderCnt();
    void setOrderCnt(int newCount);
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
                    "s_ytd : %f, " +
                    "s_order_cnt : %d, " +
                    "s_remote_cnt : %d, " +
                    "s_data : '%s' }";

    static Stock from(final RandomDataGenerator rdg) { return new PojoStock(rdg); }

    class PojoStock implements Stock {
        final int quantity;
        final String[] dist;
        final double ytd;
        int orderCnt = 0;
        final int remoteCnt = 0;
        final String data;
        PojoStock(final RandomDataGenerator rdg) {
            quantity = rdg.rand().nextInt(99)+1;
            dist = new String[] {
                    rdg.randomWord(24),
                    rdg.randomWord(24),
                    rdg.randomWord(24),
                    rdg.randomWord(24),
                    rdg.randomWord(24),
                    rdg.randomWord(24),
                    rdg.randomWord(24),
                    rdg.randomWord(24),
                    rdg.randomWord(24),
                    rdg.randomWord(24)};
            ytd = rdg.rand().nextDouble()*10000;
            data = rdg.randomWord(26,50);
        }
        @Override public int getQuantity() { return quantity; }
        @Override public String getDist(final int num) {
            if (num < 0 || num > 9) throw new IndexOutOfBoundsException("Dist num must be [0:9], got " + num);
            return dist[num];
        }
        @Override public double getYtd() { return ytd; }
        @Override public int getOrderCnt() { return orderCnt; }
        @Override public void setOrderCnt(int newOrderCnt) { orderCnt = newOrderCnt; }
        @Override public int getRemoteCnt() { return remoteCnt; }
        @Override public String getData() { return data; }
        @Override public String toString() {
            return String.format(S_TMPL, quantity, dist[0], dist[1],
                    dist[2], dist[3], dist[4], dist[5], dist[6], dist[7],
                    dist[8], dist[9], ytd, orderCnt, remoteCnt, data);
        }
    }
}
