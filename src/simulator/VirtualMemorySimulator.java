package simulator;

import simulator.util.Logarithm;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class VirtualMemorySimulator {

    private int virtualMemorySize;
    private int mainMemorySize;
    private int pageSize;
    private int virtualAddressNrBits;
    private int physicalAddressNrBits;
    private int offsetNrBits;

    private MainMemory mainMemory;
    private PageTable pageTable;
    private Disk disk;
    private TLB tlb;

    //table to store last used times for each frame number in the main memory
    //use this for the Least Recently Used page replacement algorithm, to decide which page to replace
    private Map<Integer, Timestamp> lruTable;

    //least recently used table for TLB
    private Map<Integer, Timestamp> tlbLRUTable;

    //Constructors
    public VirtualMemorySimulator(int virtualMemorySize, int mainMemorySize, int pageSize, int tlbSize){
        this.virtualMemorySize = virtualMemorySize;
        this.mainMemorySize = mainMemorySize;
        this.pageSize = pageSize;
        virtualAddressNrBits = (int) Logarithm.log2(virtualMemorySize);
        physicalAddressNrBits = (int)Logarithm.log2(mainMemorySize);
        offsetNrBits = (int)Logarithm.log2(pageSize);

        int maxFrameNumber = (int)Math.pow(2, physicalAddressNrBits - offsetNrBits);
        System.out.println("Max frame nr: " + maxFrameNumber);
        mainMemory = new MainMemory(maxFrameNumber, pageSize);
        int maxVirtualPageNumber = (int)Math.pow(2, virtualAddressNrBits - offsetNrBits);
        pageTable = new PageTable(maxVirtualPageNumber);
        disk = new Disk(maxVirtualPageNumber, pageSize);
        tlb = new TLB(tlbSize);

        // initialize lruTable
        lruTable = new HashMap<>();
        for (int i = 0; i < maxFrameNumber; i++) {
            lruTable.put(i, null);
        }

        //initialize LRU table for TLB
        tlbLRUTable = new HashMap<>();
        for (int i = 0; i < tlbSize; i++) {
            tlbLRUTable.put(i, null);
        }
    }

    //Getters and setters
    public int getVirtualMemorySize() {
        return virtualMemorySize;
    }

    public void setVirtualMemorySize(int virtualMemorySize) {
        this.virtualMemorySize = virtualMemorySize;
    }

    public int getMainMemorySize() {
        return mainMemorySize;
    }

    public void setMainMemorySize(int mainMemorySize) {
        this.mainMemorySize = mainMemorySize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getVirtualAddressNrBits() {
        return virtualAddressNrBits;
    }

    public void setVirtualAddressNrBits(int virtualAddressNrBits) {
        this.virtualAddressNrBits = virtualAddressNrBits;
    }

    public int getPhysicalAddressNrBits() {
        return physicalAddressNrBits;
    }

    public void setPhysicalAddressNrBits(int physicalAddressNrBits) {
        this.physicalAddressNrBits = physicalAddressNrBits;
    }

    public int getOffsetNrBits() {
        return offsetNrBits;
    }

    public void setOffsetNrBits(int offsetNrBits) {
        this.offsetNrBits = offsetNrBits;
    }

    private int getLeastRecentlyUsedEntry(Map<Integer, Timestamp> table) {
        Map.Entry<Integer, Timestamp> firstEntry = table.entrySet().stream().findFirst().get();
        int frameNr = firstEntry.getKey();
        Timestamp time = firstEntry.getValue();
        for (Map.Entry<Integer, Timestamp> e: table.entrySet()) {
            if (time.after(e.getValue())) {
                frameNr = e.getKey();
                time = e.getValue();
            }
        }
        return frameNr;
    }

    private void updateTLB(int virtualPageNumber) {
        PageTableEntry pte = pageTable.getPageTableEntry(virtualPageNumber);
        if (tlb.isFull()) {
            int lruVirtualPageNumber = getLeastRecentlyUsedEntry(tlbLRUTable);
            PageTableEntry entryToReplace = tlb.getTLBEntry(lruVirtualPageNumber);
            tlb.removeTLBEntry(lruVirtualPageNumber);
            tlbLRUTable.remove(lruVirtualPageNumber);
            pageTable.setDirty(lruVirtualPageNumber, entryToReplace.isDirty());

            tlb.addTLBEntry(virtualPageNumber, pte);
        }
        else {
            tlb.addTLBEntry(virtualPageNumber, pte);
        }
        tlbLRUTable.put(virtualPageNumber, new Timestamp(System.currentTimeMillis())); //update LRU table for TLB
    }

    public Map<Integer, Map<Integer, Integer>> getDiskContents() {
        return disk.getDiskContents();
    }

    public Map<Integer, Map<Integer, Integer>> getMainMemoryContents() {
        return mainMemory.getMemoryContents();
    }

    public Map<Integer, PageTableEntry> getPageTableContents() {
        return pageTable.getPageTableContents();
    }

    public Map<Integer, PageTableEntry> getTLBContents() {
        return tlb.getTLBContents();
    }

    public int loadData(int address) {
        //TODO maybe add delay between different steps so that the user can visualize each step?

        VirtualAddress virtualAddress = constructVirtualAddress(address);
        System.out.println("Virtual address is: " + virtualAddress);
        int loadData = 0;
        if (tlb.containsEntry(virtualAddress.getVirtualPageNumber())) {
            // get frame number from TLB
            int frameNumber = tlb.getFrameNumber(virtualAddress.getVirtualPageNumber());
            PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
            loadData = mainMemory.load(physicalAddress);

            tlbLRUTable.put(virtualAddress.getVirtualPageNumber(), new Timestamp(System.currentTimeMillis())); //update LRU table for TLB
            lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis()));
            mainMemory.printPageContents(physicalAddress.getFrameNumber());
            System.out.println("Got physical address from TLB!");
            tlb.printContents();
        }
        else {
            if (pageTable.isPresent(virtualAddress.getVirtualPageNumber())) {
                //translate address and load from main memory
                int frameNumber = pageTable.getFrameNumber(virtualAddress.getVirtualPageNumber());
                PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
                lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis())); //update LRU table
                System.out.println("updated timestamp: " + lruTable.get(frameNumber));
                loadData = mainMemory.load(physicalAddress);

                System.out.println("Got physical address from page table!");
            } else {
                //load from disk
                Page page = disk.load(virtualAddress.getVirtualPageNumber());

                //if memory is full, apply page replacement algorithm to evict least recently used page
                if (mainMemory.isFull()) {
                    int lruFrameNr = getLeastRecentlyUsedEntry(lruTable);
                    int lruVirtualPageNumber = pageTable.getCorrespondingVPN(lruFrameNr);

                    //if page is dirty, write to disk
                    if (pageTable.isDirty(lruVirtualPageNumber)) {
                        Page evictedPage = mainMemory.getPage(lruFrameNr);
                        disk.store(lruVirtualPageNumber, evictedPage);
                        pageTable.setDirty(lruVirtualPageNumber, false);
                    }

                    pageTable.setPresent(lruVirtualPageNumber, false);

                    mainMemory.bringPageToMemory(page, lruFrameNr);
                    pageTable.newPageTableEntry(virtualAddress.getVirtualPageNumber(), lruFrameNr);
                    lruTable.put(lruFrameNr, new Timestamp(System.currentTimeMillis())); //update LRU table
                    System.out.println("updated timestamp: " + lruTable.get(lruFrameNr));
                    PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, lruFrameNr);
                    loadData = mainMemory.load(physicalAddress);
                    System.out.println("Had to bring page from disk and replace another one in memory");
                } else {
                    int frameNumber = mainMemory.bringPageToMemory(page);
                    pageTable.newPageTableEntry(virtualAddress.getVirtualPageNumber(), frameNumber);
                    lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis())); //update LRU table
                    System.out.println("updated timestamp: " + lruTable.get(frameNumber));
                    PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
                    loadData = mainMemory.load(physicalAddress);
                    System.out.println("Had to bring page from disk and there was enough space in memory");
                }
            }
            //bring updated page table entry to TLB
            updateTLB(virtualAddress.getVirtualPageNumber());
            System.out.println("Updated TLB");
            tlb.printContents();
        }
        return loadData;
    }

    public void storeData(int address, int data) {

        VirtualAddress virtualAddress = constructVirtualAddress(address);
        if (tlb.containsEntry(virtualAddress.getVirtualPageNumber())) {
            // get frame number from TLB
            int frameNumber = tlb.getFrameNumber(virtualAddress.getVirtualPageNumber());
            PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
            mainMemory.store(physicalAddress, data);

            tlb.setDirty(virtualAddress.getVirtualPageNumber(), true);
            tlbLRUTable.put(virtualAddress.getVirtualPageNumber(), new Timestamp(System.currentTimeMillis())); //update LRU table for TLB
            lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis()));
            mainMemory.printPageContents(physicalAddress.getFrameNumber());

            tlb.printContents();
            System.out.println("Got physical address from TLB!");
        }
        else {
            if (pageTable.isPresent(virtualAddress.getVirtualPageNumber())) {
                //translate address and load from main memory
                int frameNumber = pageTable.getFrameNumber(virtualAddress.getVirtualPageNumber());
                PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
                mainMemory.store(physicalAddress, data);

                pageTable.setDirty(virtualAddress.getVirtualPageNumber(), true);
                lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis())); //update LRU table
                mainMemory.printPageContents(physicalAddress.getFrameNumber());
                System.out.println("Got physical address from page table!");
            } else {
                //load page from disk
                Page page = disk.load(virtualAddress.getVirtualPageNumber());

                if (mainMemory.isFull()) {
                    int lruFrameNr = getLeastRecentlyUsedEntry(lruTable);
                    int lruVirtualPageNumber = pageTable.getCorrespondingVPN(lruFrameNr);

                    //if page is dirty, write to disk
                    if (pageTable.isDirty(lruVirtualPageNumber)) {
                        Page evictedPage = mainMemory.getPage(lruFrameNr);
                        disk.store(lruVirtualPageNumber, evictedPage);
                        pageTable.setDirty(lruVirtualPageNumber, false);
                    }

                    pageTable.setPresent(lruVirtualPageNumber, false);

                    mainMemory.bringPageToMemory(page, lruFrameNr);
                    pageTable.newPageTableEntry(virtualAddress.getVirtualPageNumber(), lruFrameNr);
                    lruTable.put(lruFrameNr, new Timestamp(System.currentTimeMillis())); //update LRU table
                    System.out.println("updated timestamp: " + lruTable.get(lruFrameNr));

                    PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, lruFrameNr);
                    mainMemory.store(physicalAddress, data);
                    pageTable.setDirty(virtualAddress.getVirtualPageNumber(), true);

                    System.out.println("Had to bring page from disk and replace another one in memory");

                } else {
                    int frameNumber = mainMemory.bringPageToMemory(page);
                    System.out.println("Frame number is: " + frameNumber);
                    pageTable.newPageTableEntry(virtualAddress.getVirtualPageNumber(), frameNumber);   // add new mapping to page table
                    PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
                    lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis())); //update LRU table
                    pageTable.setDirty(virtualAddress.getVirtualPageNumber(), true);
                    mainMemory.store(physicalAddress, data);
                    mainMemory.printPageContents(frameNumber);
                    System.out.println("Had to bring page from disk and there was enough space in memory");
                }
            }
            //bring updated page table entry to TLB
            updateTLB(virtualAddress.getVirtualPageNumber());
            System.out.println("Updated TLB");
            tlb.printContents();

        }
    }

    public void printVirtualMemorySimulatorStatus() {
        pageTable.printContents();
        mainMemory.printContents();
        disk.printContents();
    }

    public PhysicalAddress translateVirtualAddress(VirtualAddress virtualAddress, int frameNumber) {
        return new PhysicalAddress(frameNumber, virtualAddress.getOffset());
    }

    public VirtualAddress constructVirtualAddress(int address) {
        int mask = (1 << offsetNrBits) - 1;
        int offset = address & mask;
        int virtualPageNumberBits = virtualAddressNrBits - offsetNrBits;
        address = address >> offsetNrBits;
        mask = (1 << virtualPageNumberBits) - 1;
        int virtualPageNumber = address & mask;
        return new VirtualAddress(virtualPageNumber, offset);
    }

    public static void main(String[] args) {
        System.out.println("Start Virtual Memory Simulation!");

        System.out.println("Input virtual memory size:");
        Scanner sc = new Scanner(System.in);
        int virtualMemSize = sc.nextInt();
        while (virtualMemSize % 2 != 0) {
            System.out.println("Memory size must be a multiple of 2! Please input new size");
            virtualMemSize = sc.nextInt();
        }

        System.out.println("Input main memory size:");
        int mainMemSize = sc.nextInt();
        while (mainMemSize % 2 != 0) {
            System.out.println("Memory size must be a multiple of 2! Please input new size");
            mainMemSize = sc.nextInt();
        }

        System.out.println("Input page size:");
        int pageSize = sc.nextInt();
        while (pageSize % 2 != 0) {
            System.out.println("Page size must be a multiple of 2! Please input new size");
            pageSize = sc.nextInt();
        }

        System.out.println("Input TLB size:");
        int tlbSize = sc.nextInt();

        VirtualMemorySimulator simulator = new VirtualMemorySimulator(virtualMemSize, mainMemSize, pageSize, tlbSize);
        System.out.println("virtual address: " + simulator.getVirtualAddressNrBits() + " bits\n" +
                "physical address: " + simulator.getPhysicalAddressNrBits() + " bits\n" +
                "offset: " + simulator.getOffsetNrBits() + " bits");
        System.out.println("Initial simulator status: ");
        simulator.printVirtualMemorySimulatorStatus();

        while (true) {
            System.out.println("Command:");
            sc.nextLine();
            String command = sc.nextLine();
            while (!command.equals("LOAD") && !command.equals("STORE")) {
                System.out.println("Invalid command! Must be one of: LOAD, STORE");
                command = sc.nextLine();
            }

            System.out.println("Address:");
            int addr = sc.nextInt();
            while (addr >= virtualMemSize) {
                System.out.println("Address cannot be greater than the virtual memory size!");
                addr = sc.nextInt();
            }

            switch (command){
                case "LOAD":
                    int data = simulator.loadData(addr);
                    System.out.println("Load data: " + data);
                    simulator.printVirtualMemorySimulatorStatus();
                    break;
                case "STORE":
                    System.out.println("Data:");
                    data = sc.nextInt();
                    simulator.storeData(addr, data);
                    System.out.println("Store data: " + data);
                    simulator.printVirtualMemorySimulatorStatus();
            }

        }
    }
}
