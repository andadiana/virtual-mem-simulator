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

    private Page(int pageSize, Map<Integer, Integer> pageContents) {
        this.pageSize = pageSize;
        this.pageContents = new HashMap<>(pageContents);
    }

    public int load(int offset) {
        return pageContents.get(offset);
    }

    public void store(int offset, int data) {
        pageContents.put(offset, data);
    }

    public Page getCopy() {
        return new Page(pageSize, pageContents);
    }

    public void printContents() {
        System.out.println("Page contents: ");
        for (Map.Entry<Integer, Integer> e: pageContents.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    public Map<Integer, Integer> getPageContents() {
        Map<Integer, Integer> pageCopy = new HashMap<>();
        for (Map.Entry<Integer, Integer> e: pageContents.entrySet()) {
            pageCopy.put(e.getKey(), e.getValue());
        }
        return pageCopy;
    }
}
