package simulator;

import simulator.util.Logarithm;

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

    public int loadData(int address) {
        VirtualAddress virtualAddress = constructVirtualAddress(address);
        System.out.println("Virtual address is: " + virtualAddress);
        if (pageTable.isPresent(virtualAddress)) {
            //translate address and load from main memory
            int frameNumber = pageTable.getFrameNumber(virtualAddress);
            PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
            return mainMemory.load(physicalAddress);
        }
        else {
            //load from disk
            Page page = disk.load(virtualAddress);
            int frameNumber = mainMemory.bringPageToMemory(page);
            pageTable.newPageTableEntry(virtualAddress, frameNumber);
            //TODO add page replacement algorithm
            PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
            return mainMemory.load(physicalAddress);
        }
    }

    public void storeData(int address, int data) {
        VirtualAddress virtualAddress = constructVirtualAddress(address);
        if (pageTable.isPresent(virtualAddress)) {
            //translate address and load from main memory
            int frameNumber = pageTable.getFrameNumber(virtualAddress);
            PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);
            mainMemory.store(physicalAddress, data);

            mainMemory.printPageContents(physicalAddress.getFrameNumber());
        }
        else {
            //load page from disk
            Page page = disk.load(virtualAddress);
            //disk.store(virtualAddress, data); //TODO do this only when page is removed from main memory, after checking dirty bit
            int frameNumber = mainMemory.bringPageToMemory(page);
            System.out.println("Frame number is: " + frameNumber);
            pageTable.newPageTableEntry(virtualAddress, frameNumber);   // add new mapping to page table
            PhysicalAddress physicalAddress = translateVirtualAddress(virtualAddress, frameNumber);

            mainMemory.store(physicalAddress, data);
            mainMemory.printPageContents(frameNumber);
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
