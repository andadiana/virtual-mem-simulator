package simulator;

import java.util.HashMap;
import java.util.Map;

public class TLB {

    private Map<Integer, PageTableEntry> tlb;
    private int size;

    public TLB(int size) {
        this.size = size;
        //initialize TLB
        tlb = new HashMap<>();

    }

    public int getFrameNumber(int virtualPageNumber) {
        return tlb.get(virtualPageNumber).getFrameNumber();
    }

    public boolean isDirty(int virtualPageNumber) {
        return tlb.get(virtualPageNumber).isDirty();
    }

    public boolean isPresent(int virtualPageNumber) {
        return tlb.get(virtualPageNumber).isPresent();
    }

    public void setPresent(int virtualPageNumber, boolean present) {
        tlb.get(virtualPageNumber).setPresent(present);
        if (present == false) {
            tlb.get(virtualPageNumber).setFrameNumber(-1);
        }
    }

    public void setDirty(int virtualPageNumber, boolean dirty) {
        tlb.get(virtualPageNumber).setDirty(dirty);
    }

    public int getCorrespondingVPN(int frameNr) {
        for (Map.Entry<Integer, PageTableEntry> e: tlb.entrySet()) {
            if (e.getValue().getFrameNumber() == frameNr) {
                return e.getKey();
            }
        }
        return 0;
    }

    public boolean containsEntry(int virtualPageNumber) {
        if (tlb.containsKey(virtualPageNumber)) {
            return true;
        }
        return false;
    }

    public PageTableEntry getTLBEntry(int virtualPageNumber) {
        return tlb.get(virtualPageNumber);
    }

    public void addTLBEntry(int virtualPageNumber, PageTableEntry entry) {
        tlb.put(virtualPageNumber, new PageTableEntry(entry.getFrameNumber(), entry.isPresent(), entry.isDirty()));
    }

    public void removeTLBEntry(int virtualPageNumber) {
        tlb.remove(virtualPageNumber);
    }

    public boolean isFull() {
        if (tlb.size() == size) {
            return true;
        }
        return false;
    }

    public void printContents() {
        System.out.println("TLB contents:\n" +
                "----------------------");
        for (Map.Entry<Integer, PageTableEntry> e: tlb.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        System.out.println("----------------------");
    }
}
