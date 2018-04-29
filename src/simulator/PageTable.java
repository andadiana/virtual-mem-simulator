package simulator;

import java.util.HashMap;
import java.util.Map;

public class PageTable {

    private Map<Integer, PageTableEntry> pageTable;
    private int size;

    public PageTable(int size) {
        this.size = size;
        //initialize page table
        pageTable = new HashMap<>();
        for (int i = 0; i < size; i++) {
            PageTableEntry pte = new PageTableEntry(-1, false, false);
            pageTable.put(i, pte);
        }

    }

    public PageTableEntry getPageTableEntry(int virtualPageNumber) {
        return pageTable.get(virtualPageNumber);
    }

    public int getFrameNumber(int virtualPageNumber) {
        return pageTable.get(virtualPageNumber).getFrameNumber();
    }

    public boolean isDirty(int virtualPageNumber) {
        return pageTable.get(virtualPageNumber).isDirty();
    }

    public boolean isPresent(int virtualPageNumber) {
        return pageTable.get(virtualPageNumber).isPresent();
    }

    public void setPresent(int virtualPageNumber, boolean present) {
        pageTable.get(virtualPageNumber).setPresent(present);
        if (present == false) {
            pageTable.get(virtualPageNumber).setFrameNumber(-1);
        }
    }

    public void setDirty(int virtualPageNumber, boolean dirty) {
        pageTable.get(virtualPageNumber).setDirty(dirty);
    }

    public int getCorrespondingVPN(int frameNr) {
        for (Map.Entry<Integer, PageTableEntry> e: pageTable.entrySet()) {
            if (e.getValue().getFrameNumber() == frameNr) {
                return e.getKey();
            }
        }
        return 0;
    }

    public void newPageTableEntry(int virtualPageNumber, int frameNr) {
        PageTableEntry pte = new PageTableEntry(frameNr, true, false);
        pageTable.put(virtualPageNumber, pte);
    }

    public void printContents() {
        System.out.println("Page table contents:\n" +
                           "----------------------");
        for (Map.Entry<Integer, PageTableEntry> e: pageTable.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        System.out.println("----------------------");
    }

}
