package test;

import org.junit.Test;
import simulator.PageTableEntry;
import simulator.VirtualAddress;
import simulator.VirtualMemorySimulator;

import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SimulatorTest {

    @Test
    public void tlbMiss_Correct() {
        VirtualMemorySimulator simulator = new VirtualMemorySimulator(16, 8, 2, 1);
        Map<Integer, PageTableEntry> tlbContents = simulator.getTLBContents();
        assertEquals(tlbContents.size(), 0);
        simulator.loadData(2);
        tlbContents = simulator.getTLBContents();
        assertEquals(tlbContents.size(), 1);
        VirtualAddress virtualAddress = simulator.constructVirtualAddress(2);
        assertTrue(tlbContents.containsKey(virtualAddress.getVirtualPageNumber()));
    }

    @Test
    public void virtualAddressConstruct_Success() {
        VirtualMemorySimulator simulator = new VirtualMemorySimulator(16, 8, 2, 1);
        VirtualAddress virtualAddress = simulator.constructVirtualAddress(2);
        assertEquals(virtualAddress.getVirtualPageNumber(), 1);
        assertEquals(virtualAddress.getOffset(), 0);
    }

    @Test
    public void pageReplacementAlgorithm_Success() {
        VirtualMemorySimulator simulator = new VirtualMemorySimulator(16, 4, 2, 1);
        simulator.storeData(4, 104);
        simulator.storeData(5, 105);
        simulator.loadData(0);
        Map<Integer, Map<Integer, Integer>> memoryContents = simulator.getMainMemoryContents();
        assertTrue(memoryContents.get(0).containsKey(0));
        simulator.loadData(2);
        simulator.loadData(4);
        memoryContents = simulator.getMainMemoryContents();
        int val = memoryContents.get(1).get(2);
        assertEquals(val, 104);
        val = memoryContents.get(1).get(3);
        assertEquals(val, 105);
    }

}
