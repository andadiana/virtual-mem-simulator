package simulator;

import simulator.util.Logarithm;

import java.util.HashMap;
import java.util.Map;

public class Disk {

    private Map<Integer, Page> disk;

    public Disk(int maxVirtualPageNumber, int pageSize) {
        disk = new HashMap<>();
        //initialize disk
        for (int i = 0; i < maxVirtualPageNumber; i++) {
            Page page = new Page(pageSize);
            disk.put(i, page);
        }
    }

    public void store(int virtualPageNumber, Page page) {
        disk.put(virtualPageNumber, page);
    }

    public Page load(int virtualPageNumber) {
        return disk.get(virtualPageNumber);
    }

    public void printContents() {
        System.out.println("Disk contents:\n" +
                "----------------------");
        for (Map.Entry<Integer, Page> e: disk.entrySet()) {
            System.out.println(e);
            e.getValue().printContents();
        }
        System.out.println("----------------------");
    }
}
