package csc733.group5.data;

import java.util.concurrent.atomic.AtomicInteger;

public class NewOrder {
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;

    public NewOrder() {
        id = nextId.getAndIncrement();
    }

    public String toCypherCreateString() {
        return "{ no_o_id : " + id + "}";
    }
}
