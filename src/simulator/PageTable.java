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
            PageTableEntry pte = new PageTableEntry(0, false, false, false);
            pageTable.put(i, pte);
        }

    }

    public int getFrameNumber(VirtualAddress virtualAddress) {
        return pageTable.get(virtualAddress.getVirtualPageNumber()).getFrameNumber();
    }

    public boolean isDirty(VirtualAddress virtualAddress) {
        return pageTable.get(virtualAddress.getVirtualPageNumber()).isDirty();
    }

    public boolean isReferenced(VirtualAddress virtualAddress) {
        return pageTable.get(virtualAddress.getVirtualPageNumber()).isReferenced();
    }

    public boolean isPresent(VirtualAddress virtualAddress) {
        return pageTable.get(virtualAddress.getVirtualPageNumber()).isPresent();
    }

    public void setDirty(VirtualAddress virtualAddress) {
        pageTable.get(virtualAddress).setDirty(true);
    }

    public void newPageTableEntry(VirtualAddress virtualAddress, int frameNr) {
        PageTableEntry pte = new PageTableEntry(frameNr, true, false, true);
        pageTable.put(virtualAddress.getVirtualPageNumber(), pte);
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
