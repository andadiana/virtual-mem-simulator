package presentation.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import presentation.data.DiskItem;
import presentation.data.MemoryItem;
import presentation.data.PageTableItem;
import simulator.*;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import static simulator.OperationType.*;


public class SimulatorWindow implements Observer{

    @FXML
    private TableView<DiskItem> diskTable;

    @FXML
    private TableColumn<DiskItem, Integer> virtualAddressColumnDiskTable;

    @FXML
    private TableColumn<DiskItem, Integer> addressColumnDiskTable;

    @FXML
    private TableColumn<DiskItem, Integer> dataColumnDiskTable;

    @FXML
    private TableView<MemoryItem> memoryTable;

    @FXML
    private TableColumn<MemoryItem, Integer> frameNumberColumnMemoryTable;

    @FXML
    private TableColumn<MemoryItem, Integer> addressColumnMemoryTable;

    @FXML
    private TableColumn<MemoryItem, Integer> dataColumnMemoryTable;

    @FXML
    private TableView<PageTableItem> pageTable;

    @FXML
    private TableColumn<PageTableItem, Integer> virtualPageNumberColumnPageTable;

    @FXML
    private TableColumn<PageTableItem, Integer> pteFrameNumberColumnPageTable;

    @FXML
    private TableColumn<PageTableItem, Boolean> ptePresentColumnPageTable;

    @FXML
    private TableColumn<PageTableItem, Boolean> pteDirtyColumnPageTable;

    @FXML
    private TableView<PageTableItem> tlbTable;

    @FXML
    private TableColumn<PageTableItem, Integer> virtualPageNumberColumnTLBTable;

    @FXML
    private TableColumn<PageTableItem, Integer> frameNumberColumnTLBTable;

    @FXML
    private TableColumn<PageTableItem, Boolean> presentColumnTLBTable;

    @FXML
    private TableColumn<PageTableItem, Boolean> dirtyColumnTLBTable;

    @FXML
    private ComboBox<String> commandComboBox;

    @FXML
    private TextField addressTextField;

    @FXML
    private TextField dataTextField;

    @FXML
    private Label dataLabel;

    @FXML
    private Button executeButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label simulatorDetailsLabel;

    @FXML
    private TextArea operationsStatus;

    @FXML
    private TextField virtualAddressPageNumberField;

    @FXML
    private TextField virtualAddressOffsetField;

    @FXML
    private TextField physicalAddressFrameNumberField;

    @FXML
    private TextField physicalAddressOffsetField;

    @FXML
    private Button backButton;

    private VirtualMemorySimulator simulator;
    private String command;
    private int maxAddress;
    private Queue<OperationStep> operationSteps;

    private int virtualMemorySize;
    private int mainMemorySize;
    private int pageSize;
    private int tlbSize;

    public void update(Queue<OperationStep> operationSteps) {
        for (OperationStep o: operationSteps) {
            System.out.println(o);
        }
        this.operationSteps = operationSteps;
    }

    public void init() {

        int virtualMemorySize = 16;
        int mainMemorySize = 8;
        int pageSize = 2;
        int tlbSize = 3;

        simulator = new VirtualMemorySimulator(virtualMemorySize, mainMemorySize, pageSize, tlbSize);
        simulator.addObserver(this);
        maxAddress = simulator.getVirtualMemorySize();

        simulatorDetailsLabel.setText("Simulator details: Virtual memory size: " + virtualMemorySize + ", main memory " +
                "size: " + mainMemorySize + ", page size: " + pageSize + ", tlb size: " + tlbSize);

        ObservableList<String> options = FXCollections.observableArrayList("LOAD", "STORE");
        commandComboBox.setItems(options);
        commandComboBox.getSelectionModel().select(0);
        command = commandComboBox.getSelectionModel().getSelectedItem();
        dataTextField.setVisible(false);
        dataLabel.setVisible(false);
        nextButton.setVisible(false);
        operationsStatus.setText("");
        virtualAddressPageNumberField.setText("");
        virtualAddressOffsetField.setText("");
        physicalAddressFrameNumberField.setText("");
        physicalAddressOffsetField.setText("");

        //initialize tables
        initializeDiskTable();
        initializeMemoryTable();
        initializePageTable();
        initializeTLB();
    }

    public void initData(int virtualMemorySize, int mainMemorySize, int pageSize, int tlbSize) {
        this.virtualMemorySize = virtualMemorySize;
        this.mainMemorySize = mainMemorySize;
        this.pageSize = pageSize;
        this.tlbSize = tlbSize;

        simulator = new VirtualMemorySimulator(virtualMemorySize, mainMemorySize, pageSize, tlbSize);
        simulator.addObserver(this);
        maxAddress = simulator.getVirtualMemorySize();

        simulatorDetailsLabel.setText("Simulator details: Virtual memory size: " + virtualMemorySize + ", main memory " +
                "size: " + mainMemorySize + ", page size: " + pageSize + ", tlb size: " + tlbSize);

        ObservableList<String> options = FXCollections.observableArrayList("LOAD", "STORE");
        commandComboBox.setItems(options);
        commandComboBox.getSelectionModel().select(0);
        command = commandComboBox.getSelectionModel().getSelectedItem();
        dataTextField.setVisible(false);
        dataLabel.setVisible(false);
        nextButton.setVisible(false);
        operationsStatus.setText("");
        virtualAddressPageNumberField.setText("");
        virtualAddressOffsetField.setText("");
        physicalAddressFrameNumberField.setText("");
        physicalAddressOffsetField.setText("");

        //initialize tables
        initializeDiskTable();
        initializeMemoryTable();
        initializePageTable();
        initializeTLB();
    }

    @FXML
    public void clickDiskTable(MouseEvent event) {
        System.out.println("Clicked table");
    }

    @FXML
    private void comboBoxSelected(ActionEvent event) {
        System.out.println(commandComboBox.getSelectionModel().getSelectedItem());
        command = commandComboBox.getSelectionModel().getSelectedItem();
        switch (command) {
            case "LOAD": dataTextField.setVisible(false);
                         dataLabel.setVisible(false);
                         break;
            case "STORE": dataTextField.setVisible(true);
                dataLabel.setVisible(true);
                break;
        }
    }

    @FXML
    private void clickExecuteButton(ActionEvent event) {
        operationsStatus.setText("");
        switch (command) {
            case "LOAD": executeLoad();
                break;
            case "STORE": executeStore();
                break;
        }
    }

    @FXML
    private void clickBackButton(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("../SetupWindow.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Virtual memory simulator");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();

        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    private void clickNextButton(ActionEvent event) {
        System.out.println("Operations steps (#): " + operationSteps.size());
        if (operationSteps.size() == 0) {
            operationsStatus.setText("Finished operation!");
            nextButton.setVisible(false);
            executeButton.setVisible(true);

        } else {
            OperationStep step = operationSteps.remove();
            switch (step.getType()) {
                case TLB_HIT:
                    updateTLB(TLB_HIT, step.getAddress());
                    break;
                case TLB_MISS:
                    updateTLB(TLB_MISS, step.getAddress());
                    break;
                case PAGE_TABLE_HIT:
                    updatePageTable(PAGE_TABLE_HIT, step.getAddress());
                    break;
                case PAGE_TABLE_MISS:
                    updatePageTable(PAGE_TABLE_MISS, step.getAddress());
                    break;
                case DISK_LOAD:
                    updateDisk(DISK_LOAD, step.getAddress());
                    break;
                case BRING_TO_MEMORY:
                    updateMemory(BRING_TO_MEMORY, step.getAddress());
                    break;
                case WRITE_DIRTY_PAGE:
                    updateDisk(WRITE_DIRTY_PAGE, step.getAddress());
                    break;
                case REPLACE_IN_MEMORY:
                    updateMemory(REPLACE_IN_MEMORY, step.getAddress());
                    break;
                case PAGE_TABLE_UPDATE:
                    updatePageTable(PAGE_TABLE_UPDATE, step.getAddress());
                    break;
                case TLB_UPDATE:
                    updateTLB(TLB_UPDATE, step.getAddress());
                    break;
                case LOAD_FROM_MEMORY:
                    updateMemory(LOAD_FROM_MEMORY, step.getAddress());
                    break;
                case STORE_IN_MEMORY:
                    updateMemory(STORE_IN_MEMORY, step.getAddress());
                    break;
            }
        }

    }

    private void updateTLB(OperationType type, int virtualPageNumber) {
        System.out.println("Update TLB");
        System.out.println(type);
        System.out.println(virtualPageNumber);
        switch (type) {
            case TLB_HIT:   operationsStatus.setText("TLB hit! Found TLB entry for virtual page number " + virtualPageNumber);
                break;
            case TLB_MISS:  operationsStatus.setText("TLB miss!");
                break;
            case TLB_UPDATE: operationsStatus.setText("Updating TLB entry for virtual page number " + virtualPageNumber);
                Map<Integer, PageTableEntry> tlbContents = simulator.getTLBContents();
                tlbTable.setItems(extractPageTableEntries(tlbContents));
                break;
        }
    }

    private void updatePageTable(OperationType type, int virtualPageNumber) {
        System.out.println("Update PT");
        System.out.println(type);
        System.out.println(virtualPageNumber);
        switch (type) {
            case PAGE_TABLE_HIT: operationsStatus.setText("Page table hit! Found entry for virtual page number " + virtualPageNumber);
                break;
            case PAGE_TABLE_MISS: operationsStatus.setText("Page table miss!");
                break;
            case PAGE_TABLE_UPDATE: operationsStatus.setText("Updating page table entry for virtual page number " + virtualPageNumber);
                Map<Integer, PageTableEntry> pageTableContents = simulator.getPageTableContents();
                pageTable.setItems(extractPageTableEntries(pageTableContents));
                break;
        }
    }

    private void updateDisk(OperationType type, int virtualPageNumber) {
        System.out.println("Update disk");
        System.out.println(type);
        System.out.println(virtualPageNumber);

        switch (type) {
            case DISK_LOAD: operationsStatus.setText("Loading page " + virtualPageNumber + " from disk");
                break;
            case WRITE_DIRTY_PAGE: operationsStatus.setText("Writing dirty page " + virtualPageNumber + " to disk");
                Map<Integer, Map<Integer, Integer>> diskContents = simulator.getDiskContents();
                diskTable.setItems(extractDiskItems(diskContents));
                break;
        }
    }

    private void updateMemory(OperationType type, int frameNumber) {
        System.out.println("Update Mem");
        System.out.println(type);
        System.out.println(frameNumber);

        Map<Integer, Map<Integer, Integer>> memoryContents;
        switch (type) {
            case BRING_TO_MEMORY: operationsStatus.setText("Bringing page to memory at frame number " + frameNumber);
                memoryContents = simulator.getMainMemoryContents();
                memoryTable.setItems(extractMemoryItems(memoryContents));
                break;
            case REPLACE_IN_MEMORY: operationsStatus.setText("Replacing frame " + frameNumber);
                memoryContents = simulator.getMainMemoryContents();
                memoryTable.setItems(extractMemoryItems(memoryContents));
                break;
            case LOAD_FROM_MEMORY: operationsStatus.setText("Loading from memory (from frame " + frameNumber + ")");
                physicalAddressFrameNumberField.setText(Integer.toBinaryString(frameNumber));
                physicalAddressOffsetField.setText(virtualAddressOffsetField.getText());
                break;
            case STORE_IN_MEMORY: operationsStatus.setText("Storing in memory");
                memoryContents = simulator.getMainMemoryContents();
                memoryTable.setItems(extractMemoryItems(memoryContents));
                physicalAddressFrameNumberField.setText(Integer.toBinaryString(frameNumber));
                physicalAddressOffsetField.setText(virtualAddressOffsetField.getText());
                break;
        }
    }

    private int parseAddressTextField() throws Exception {
        if (addressTextField.getText() == null) {
            //empty field
            throw new Exception("Address field cannot be empty!");
        }
        int address = -1;
        try {
            address = Integer.parseInt(addressTextField.getText());
        } catch (Exception e) {
            throw new Exception("Address field must contain a number!");
        }
        if (address < 0 && address > maxAddress - 1) {
            throw new Exception("Incorrect address value! Must be between 0 and " + (maxAddress - 1));
        }
        return address;
    }

    private int parseDataTextField() throws Exception {
        if (dataTextField.getText() == null) {
            //empty field
            throw new Exception("Data field cannot be empty!");
        }
        try {
            int data = Integer.parseInt(dataTextField.getText());
            return data;
        } catch (Exception e) {
            throw new Exception("Data field must contain a number!");
        }
    }

    private void setVirtualAddress(int address) {
        VirtualAddress virtualAddress = simulator.constructVirtualAddress(address);
        String virtualPageNumberString = Integer.toBinaryString(virtualAddress.getVirtualPageNumber());
        String offsetString  = Integer.toBinaryString(virtualAddress.getOffset());
        virtualAddressPageNumberField.setText(virtualPageNumberString);
        virtualAddressOffsetField.setText(offsetString);
    }
    private void executeLoad() {
        try {
            int address = parseAddressTextField();
            errorLabel.setText("");
            simulator.loadData(address);
            nextButton.setVisible(true);
            executeButton.setVisible(false);
            physicalAddressFrameNumberField.setText("");
            physicalAddressOffsetField.setText("");
            setVirtualAddress(address);

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void executeStore() {
        try {
            int address = parseAddressTextField();
            int data = parseDataTextField();
            errorLabel.setText("");
            simulator.storeData(address, data);
            nextButton.setVisible(true);
            executeButton.setVisible(false);
            physicalAddressFrameNumberField.setText("");
            physicalAddressOffsetField.setText("");
            setVirtualAddress(address);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void initializeDiskTable() {
        virtualAddressColumnDiskTable.setCellValueFactory(new PropertyValueFactory<>("virtualPageNumber"));
        addressColumnDiskTable.setCellValueFactory(new PropertyValueFactory<>("address"));
        dataColumnDiskTable.setCellValueFactory(new PropertyValueFactory<>("data"));

        Map<Integer, Map<Integer, Integer>> diskContents = simulator.getDiskContents();
        diskTable.setItems(extractDiskItems(diskContents));
    }

    private ObservableList<DiskItem> extractDiskItems(Map<Integer, Map<Integer, Integer>> allData) {
        return FXCollections.observableArrayList(
                allData.keySet().stream().sorted().flatMap(vpn -> {
                    Map<Integer, Integer> page = allData.get(vpn);
                    return page.keySet().stream().sorted().map(
                            address -> {
                                Integer data = page.get(address);
                                return new DiskItem(vpn, address, data);
                            }
                    );
                }).collect(Collectors.toList())
        );
    }

    private void initializeMemoryTable() {
        frameNumberColumnMemoryTable.setCellValueFactory(new PropertyValueFactory<>("frameNumber"));
        addressColumnMemoryTable.setCellValueFactory(new PropertyValueFactory<>("address"));
        dataColumnMemoryTable.setCellValueFactory(new PropertyValueFactory<>("data"));

        Map<Integer, Map<Integer, Integer>> memoryContents = simulator.getMainMemoryContents();
        memoryTable.setItems(extractMemoryItems(memoryContents));
    }

    private ObservableList<MemoryItem> extractMemoryItems(Map<Integer, Map<Integer, Integer>> allData) {
        return FXCollections.observableArrayList(
                allData.keySet().stream().sorted().flatMap(frameNr -> {
                    Map<Integer, Integer> page = allData.get(frameNr);
                    return page.keySet().stream().sorted().map(
                            address -> {
                                Integer data = page.get(address);
                                return new MemoryItem(frameNr, address, data);
                            }
                    );
                }).collect(Collectors.toList())
        );
    }

    private void initializePageTable() {
        virtualPageNumberColumnPageTable.setCellValueFactory(new PropertyValueFactory<>("virtualPageNumber"));
        pteFrameNumberColumnPageTable.setCellValueFactory(new PropertyValueFactory<>("frameNumber"));
        ptePresentColumnPageTable.setCellValueFactory(new PropertyValueFactory<>("present"));
        pteDirtyColumnPageTable.setCellValueFactory(new PropertyValueFactory<>("dirty"));

        Map<Integer, PageTableEntry> pageTableContents = simulator.getPageTableContents();
        pageTable.setItems(extractPageTableEntries(pageTableContents));
    }

    private ObservableList<PageTableItem> extractPageTableEntries(Map<Integer, PageTableEntry> allData) {
        return FXCollections.observableArrayList(
                allData.entrySet().stream().map(e -> {
                    PageTableEntry pte = allData.get(e.getKey());
                    return new PageTableItem(e.getKey(), pte.getFrameNumber(), pte.isPresent(), pte.isDirty());
                }).collect(Collectors.toList())
        );
    }

    private void initializeTLB() {
        virtualPageNumberColumnTLBTable.setCellValueFactory(new PropertyValueFactory<>("virtualPageNumber"));
        frameNumberColumnTLBTable.setCellValueFactory(new PropertyValueFactory<>("frameNumber"));
        presentColumnTLBTable.setCellValueFactory(new PropertyValueFactory<>("present"));
        dirtyColumnTLBTable.setCellValueFactory(new PropertyValueFactory<>("dirty"));

        Map<Integer, PageTableEntry> tlbContents = simulator.getTLBContents();
        tlbTable.setItems(extractPageTableEntries(tlbContents));
    }

}
