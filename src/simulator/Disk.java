package simulator;

import simulator.util.Logarithm;

import java.util.HashMap;
import java.util.Map;

public class Disk {

    private Map<Integer, Page> disk;

    private int pageSize;

    public Disk(int maxVirtualPageNumber, int pageSize) {
        disk = new HashMap<>();
        //initialize disk
        for (int i = 0; i < maxVirtualPageNumber; i++) {
            Page page = new Page(pageSize);
            disk.put(i, page);
        }
        this.pageSize = pageSize;
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

    public Map<Integer, Map<Integer, Integer>> getDiskContents() {
        Map<Integer, Map<Integer, Integer>> diskCopy = new HashMap<>();
        for (Map.Entry<Integer, Page> e: disk.entrySet()) {
            Map<Integer, Integer> pageCopy = e.getValue().getPageContents();
            //addresses with offset
            int virtualPageNumber = e.getKey();
            Map<Integer, Integer> pageCopyWithOffsets = new HashMap<>();
            for(Map.Entry<Integer, Integer> pe: pageCopy.entrySet()) {
                int offset = pe.getKey();
                int address = virtualPageNumber * pageSize + offset;
                pageCopyWithOffsets.put(address, pe.getValue());
            }
            diskCopy.put(e.getKey(), pageCopyWithOffsets);
        }
        return diskCopy;
    }
}
