package simulator;

import java.util.HashMap;
import java.util.Map;

public class MainMemory {

    private Map<Integer, Page> memory;
    private int nrFrames;

    //use this index until the memory becomes full
    private int lastFrameNr;
    private int pageSize;

    public MainMemory(int nrFrames, int pageSize) {
        this.nrFrames = nrFrames;
        this.pageSize = pageSize;
        memory = new HashMap<>();
        //initialize memory
        // TODO (at first, it should be empty - no pages)
        for (int i = 0; i < nrFrames; i++) {
            Page page = new Page(pageSize);
            memory.put(i, page);

            //memory.put(i, null);
        }
        this.lastFrameNr = 0;
    }

    public int load(PhysicalAddress address) {
        return memory.get(address.getFrameNumber()).load(address.getOffset());
    }

    public void store(PhysicalAddress address, int data) {
        System.out.println("Storing at physical address: " + address);
        memory.get(address.getFrameNumber()).store(address.getOffset(), data);
    }

    public int bringPageToMemory(Page page) {
        memory.put(lastFrameNr, page);
        int frameNr = lastFrameNr;
        lastFrameNr++;
        return frameNr;
    }

    public void printContents() {
        System.out.println("Main memory contents:\n" +
                "----------------------");
        System.out.println("size:" + memory.size());
        for (Map.Entry<Integer, Page> e: memory.entrySet()) {
            e.getValue();
            System.out.println(e);
        }
        System.out.println("----------------------");
    }

    public void printPageContents(int frameNumber) {
        memory.get(frameNumber).printContents();
    }
}
