package simulator;

import java.util.HashMap;
import java.util.Map;

public class Page {

    private Map<Integer, Integer> pageContents;

    private int pageSize;

    public Page(int pageSize) {
        this.pageSize = pageSize;
        pageContents = new HashMap<>();
        //initialize page with 0
        for (int i = 0; i < pageSize; i++) {
            pageContents.put(i, 0);
        }
    }

    public int load(int offset) {
        return pageContents.get(offset);
    }

    public void store(int offset, int data) {
        pageContents.put(offset, data);
    }

    public void printContents() {
        System.out.println("Page contents: ");
        for (Map.Entry<Integer, Integer> e: pageContents.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }
}
