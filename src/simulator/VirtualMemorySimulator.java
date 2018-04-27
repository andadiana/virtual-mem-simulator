package simulator;

import simulator.util.Logarithm;

import java.sql.Timestamp;
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

    //table to store last used times for each frame number in the main memory
    //use this for the Least Recently Used page replacement algorithm, to decide which page to replace
    private Map<Integer, Timestamp> lruTable;

    //Constructors
    public VirtualMemorySimulator(int virtualMemorySize, int mainMemorySize, int pageSize){
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

        // initialize lruTable
        lruTable = new HashMap<>();
        for (int i = 0; i < maxFrameNumber; i++) {
            lruTable.put(i, null);
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

    public int getLeastRecentlyUsedFrame() {
        int frameNr = 0;
        Timestamp time = lruTable.get(frameNr);
        for (Map.Entry<Integer, Timestamp> e: lruTable.entrySet()) {
            if (time.after(e.getValue())) {
                frameNr = e.getKey();
                time = e.getValue();
            }
        }
        return frameNr;
    }

    public int loadData(int address) {

        VirtualAddress virtualAddress = constructVirtualAddress(address);
        System.out.println("Virtual address is: " + virtualAddress);
        if (pageTable.isPresent(virtualAddress.getVirtualPageNumber())) {
            //translate address and load from main memory
            int frameNumber = pageTable.getFrameNumber(virtualAddress.getVirtualPageNumber());
            PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
            lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis())); //update LRU table
            System.out.println("updated timestamp: " + lruTable.get(frameNumber));
            return mainMemory.load(physicalAddress);
        }
        else {
            //load from disk
            Page page = disk.load(virtualAddress.getVirtualPageNumber());

            //if memory is full, apply page replacement algorithm to evict least recently used page
            if (mainMemory.isFull()) {
                int lruFrameNr = getLeastRecentlyUsedFrame();
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
                return mainMemory.load(physicalAddress);
            }
            else {
                int frameNumber = mainMemory.bringPageToMemory(page);
                pageTable.newPageTableEntry(virtualAddress.getVirtualPageNumber(), frameNumber);
                lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis())); //update LRU table
                System.out.println("updated timestamp: " + lruTable.get(frameNumber));
                PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
                return mainMemory.load(physicalAddress);
            }
        }
    }

    public void storeData(int address, int data) {

        VirtualAddress virtualAddress = constructVirtualAddress(address);
        if (pageTable.isPresent(virtualAddress.getVirtualPageNumber())) {
            //translate address and load from main memory
            int frameNumber = pageTable.getFrameNumber(virtualAddress.getVirtualPageNumber());
            PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
            mainMemory.store(physicalAddress, data);

            pageTable.setDirty(virtualAddress.getVirtualPageNumber(), true);
            lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis())); //update LRU table
            mainMemory.printPageContents(physicalAddress.getFrameNumber());
        }
        else {
            //load page from disk
            Page page = disk.load(virtualAddress.getVirtualPageNumber());

            if (mainMemory.isFull()) {
                int lruFrameNr = getLeastRecentlyUsedFrame();
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

            }
            else {
                int frameNumber = mainMemory.bringPageToMemory(page);
                System.out.println("Frame number is: " + frameNumber);
                pageTable.newPageTableEntry(virtualAddress.getVirtualPageNumber(), frameNumber);   // add new mapping to page table
                PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
                lruTable.put(frameNumber, new Timestamp(System.currentTimeMillis())); //update LRU table
                pageTable.setDirty(virtualAddress.getVirtualPageNumber(), true);
                mainMemory.store(physicalAddress, data);
                mainMemory.printPageContents(frameNumber);
            }
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
        VirtualMemorySimulator simulator = new VirtualMemorySimulator(virtualMemSize, mainMemSize, pageSize);
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
