package presentation.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.util.Callback;
import presentation.data.DiskItem;
import presentation.data.MemoryItem;
import presentation.data.PageTableItem;
import simulator.*;
import simulator.Observer;
import simulator.util.Logarithm;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
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
    private Label tlbLabel;

    @FXML
    private Label pageTableLabel;

    @FXML
    private Label mainMemoryLabel;

    @FXML
    private Label diskLabel;

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

    private ObservableList<PageTableItem> tlbObs;
    private ObservableList<PageTableItem> pageTableObs;
    private ObservableList<MemoryItem> memoryObs;
    private ObservableList<DiskItem> diskObs;

    private ObservableList<Integer> tlbHitRow;
    private ObservableList<Integer> tlbUpdateRow;
    private ObservableList<Integer> pageTableHitRow;
    private ObservableList<Integer> pageTableUpdateRow;
    private ObservableList<Integer> memoryHitRow;
    private ObservableList<Integer> memoryUpdateRow;
    private ObservableList<Integer> diskHitRow;
    private ObservableList<Integer> diskUpdateRow;

    private int offsetNrBits;
    private int virtualAddressNrBits;
    private int physicalAddressNrBits;

    private int loadResult;

    public void update(Queue<OperationStep> operationSteps) {
        for (OperationStep o: operationSteps) {
            System.out.println(o);
        }
        this.operationSteps = operationSteps;
    }

    public void initData(int virtualMemorySize, int mainMemorySize, int pageSize, int tlbSize) {
        this.virtualMemorySize = virtualMemorySize;
        this.mainMemorySize = mainMemorySize;
        this.pageSize = pageSize;
        this.tlbSize = tlbSize;

        offsetNrBits = (int) Logarithm.log2(pageSize);
        virtualAddressNrBits = (int) Logarithm.log2(virtualMemorySize);
        physicalAddressNrBits = (int)Logarithm.log2(mainMemorySize);

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
        errorLabel.setText("");
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
        Parent root = FXMLLoader.load(getClass().getResource("../resources/view/SetupWindow.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Virtual memory simulator");
        stage.setScene(new Scene(root, 350, 270));
        stage.show();

        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    private void resetStyles() {
        tlbLabel.getStyleClass().removeAll(Collections.singleton("red-label"));
        pageTableLabel.getStyleClass().removeAll(Collections.singleton("red-label"));
        mainMemoryLabel.getStyleClass().removeAll(Collections.singleton("red-label"));
        diskLabel.getStyleClass().removeAll(Collections.singleton("red-label"));
        tlbHitRow.clear();
        tlbUpdateRow.clear();
        pageTableHitRow.clear();
        pageTableUpdateRow.clear();
        memoryHitRow.clear();
        memoryUpdateRow.clear();
        diskHitRow.clear();
        diskUpdateRow.clear();
    }

    @FXML
    private void clickNextButton(ActionEvent event) {
        System.out.println("Operations steps (#): " + operationSteps.size());
        if (operationSteps.size() == 0) {
            resetStyles();
            operationsStatus.setText("Finished operation!");
            nextButton.setVisible(false);
            executeButton.setVisible(true);
            if (command.equals("LOAD")) {
                errorLabel.setText("Load result is: " + loadResult);
            }

        } else {
            OperationStep step = operationSteps.remove();
            resetStyles();
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

    private int getTLBTableIndex(int virtualPageNumber) {
        int i = 0;
        for (PageTableItem p: tlbObs) {
            if (p.getVirtualPageNumber() == virtualPageNumber)
                 return i;
            i++;
        }
        return -1;
    }

    private void updateTLB(OperationType type, int virtualPageNumber) {
        System.out.println("Update TLB");
        System.out.println(type);
        System.out.println(virtualPageNumber);
        switch (type) {
            case TLB_HIT:   operationsStatus.setText("TLB hit! Found TLB entry for virtual page number " + virtualPageNumber);
                Map<Integer, PageTableEntry> tlbContents = simulator.getTLBContents();
                tlbObs = extractPageTableEntries(tlbContents);
                tlbTable.setItems(tlbObs);
                int highlightedRow = getTLBTableIndex(virtualPageNumber);
                if (highlightedRow != -1) {
                    tlbHitRow.clear();
                    tlbHitRow.add(highlightedRow);
                }
                break;
            case TLB_MISS:  operationsStatus.setText("TLB miss!");
                if (! tlbLabel.getStyleClass().contains("red-label")) {
                    tlbLabel.getStyleClass().add("red-label");
                }
                break;
            case TLB_UPDATE: operationsStatus.setText("Updating TLB entry for virtual page number " + virtualPageNumber);
                tlbContents = simulator.getTLBContents();
                tlbObs = extractPageTableEntries(tlbContents);
                tlbTable.setItems(tlbObs);
                highlightedRow = getTLBTableIndex(virtualPageNumber);
                if (highlightedRow != -1) {
                    tlbUpdateRow.clear();
                    tlbUpdateRow.add(highlightedRow);
                }
                break;
        }
    }

    private int getPageTableIndex(int virtualPageNumber) {
        int i = 0;
        for (PageTableItem p: pageTableObs) {
            if (p.getVirtualPageNumber() == virtualPageNumber)
                return i;
            i++;
        }
        return -1;
    }

    private void updatePageTable(OperationType type, int virtualPageNumber) {
        System.out.println("Update PT");
        System.out.println(type);
        System.out.println(virtualPageNumber);
        switch (type) {
            case PAGE_TABLE_HIT: operationsStatus.setText("Page table hit! Found entry for virtual page number " + virtualPageNumber);
                Map<Integer, PageTableEntry> pageTableContents = simulator.getPageTableContents();
                pageTableObs = extractPageTableEntries(pageTableContents);
                pageTable.setItems(pageTableObs);
                int highlightedRow = getPageTableIndex(virtualPageNumber);
                if (highlightedRow != -1) {
                    pageTableHitRow.clear();
                    pageTableHitRow.add(highlightedRow);
                }
                break;
            case PAGE_TABLE_MISS: operationsStatus.setText("Page table miss!");
                if (! pageTableLabel.getStyleClass().contains("red-label")) {
                    pageTableLabel.getStyleClass().add("red-label");
                }
                break;
            case PAGE_TABLE_UPDATE: operationsStatus.setText("Updating page table entry for virtual page number " + virtualPageNumber);
                pageTableContents = simulator.getPageTableContents();
                pageTableObs = extractPageTableEntries(pageTableContents);
                pageTable.setItems(pageTableObs);
                highlightedRow = getPageTableIndex(virtualPageNumber);
                if (highlightedRow != -1) {
                    pageTableUpdateRow.clear();
                    pageTableUpdateRow.add(highlightedRow);
                }
                break;
        }
    }

    private List<Integer> getDiskIndex(int virtualPageNumber) {
        int i = 0;
        List<Integer> indices = new ArrayList<>();
        for (DiskItem d: diskObs) {
            if (d.getVirtualPageNumber() == virtualPageNumber)
                indices.add(i);
            i++;
        }
        return indices;
    }

    private void updateDisk(OperationType type, int virtualPageNumber) {
        System.out.println("Update disk");
        System.out.println(type);
        System.out.println(virtualPageNumber);

        switch (type) {
            case DISK_LOAD: operationsStatus.setText("Loading page " + virtualPageNumber + " from disk");
                List<Integer> highlightedRows = getDiskIndex(virtualPageNumber);
                if (!highlightedRows.isEmpty()) {
                    diskHitRow.clear();
                    for (Integer i: highlightedRows) {
                        diskHitRow.add(i);
                    }
                }
                break;
            case WRITE_DIRTY_PAGE: operationsStatus.setText("Writing dirty page " + virtualPageNumber + " to disk");
                Map<Integer, Map<Integer, Integer>> diskContents = simulator.getDiskContents();
                diskObs = extractDiskItems(diskContents);
                diskTable.setItems(diskObs);
                highlightedRows = getDiskIndex(virtualPageNumber);
                if (!highlightedRows.isEmpty()) {
                    diskUpdateRow.clear();
                    for (Integer i: highlightedRows) {
                        diskUpdateRow.add(i);
                    }
                }
                break;
        }
    }

    private List<Integer> getMemoryIndex(int frameNumber) {
        int i = 0;
        List<Integer> indices = new ArrayList<>();
        for (MemoryItem m: memoryObs) {
            if (m.getFrameNumber() == frameNumber)
                indices.add(i);
            i++;
        }
        return indices;
    }

    private PhysicalAddress constructPhysicalAddress(int address) {
        int mask = (1 << offsetNrBits) - 1;
        int offset = address & mask;
        int frameNumberBits = physicalAddressNrBits - offsetNrBits;
        address = address >> offsetNrBits;
        mask = (1 << frameNumberBits) - 1;
        int frameNumber = address & mask;
        return new PhysicalAddress(frameNumber, offset);
    }

    private Integer getMemoryIndex(int frameNumber, int physicalAddress) {
        int i = 0;
        for (MemoryItem m: memoryObs) {
            if (m.getFrameNumber() == frameNumber && m.getAddress() == physicalAddress) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private void updateMemory(OperationType type, int physicalAddress) {
        System.out.println("Update Mem");
        System.out.println(type);
        PhysicalAddress address = constructPhysicalAddress(physicalAddress);
        System.out.println(address.getFrameNumber());

        Map<Integer, Map<Integer, Integer>> memoryContents;
        switch (type) {
            case BRING_TO_MEMORY: operationsStatus.setText("Bringing page to memory at frame number " + address.getFrameNumber());
                memoryContents = simulator.getMainMemoryContents();
                memoryObs = extractMemoryItems(memoryContents);
                memoryTable.setItems(memoryObs);
                List<Integer> highlightedRows = getMemoryIndex(address.getFrameNumber());
                if (!highlightedRows.isEmpty()) {
                    memoryUpdateRow.clear();
                    for (Integer i: highlightedRows) {
                        memoryUpdateRow.add(i);
                    }
                }
                break;
            case REPLACE_IN_MEMORY: operationsStatus.setText("Replacing frame " + address.getFrameNumber());
                memoryContents = simulator.getMainMemoryContents();
                memoryObs = extractMemoryItems(memoryContents);
                memoryTable.setItems(memoryObs);
                highlightedRows = getMemoryIndex(address.getFrameNumber());
                if (!highlightedRows.isEmpty()) {
                    memoryUpdateRow.clear();
                    for (Integer i: highlightedRows) {
                        memoryUpdateRow.add(i);
                    }
                }
                break;
            case LOAD_FROM_MEMORY: operationsStatus.setText("Loading from memory (from frame " + address.getFrameNumber() + ")");
                setPhysicalAddress(address.getFrameNumber());
                int highlightedRow = getMemoryIndex(address.getFrameNumber(), physicalAddress);
                if (highlightedRow != -1) {
                    memoryHitRow.clear();
                    memoryHitRow.add(highlightedRow);
                }
                break;
            case STORE_IN_MEMORY: operationsStatus.setText("Storing in memory");
                memoryContents = simulator.getMainMemoryContents();
                memoryObs = extractMemoryItems(memoryContents);
                memoryTable.setItems(memoryObs);
                setPhysicalAddress(address.getFrameNumber());
                highlightedRow = getMemoryIndex(address.getFrameNumber(), physicalAddress);
                if (highlightedRow != -1) {
                    memoryHitRow.clear();
                    memoryHitRow.add(highlightedRow);
                }
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
        int vpnNrBits = virtualAddressNrBits - offsetNrBits;
        while (virtualPageNumberString.length() < vpnNrBits) {
            virtualPageNumberString = "0" + virtualPageNumberString;
        }
        String offsetString  = Integer.toBinaryString(virtualAddress.getOffset());
        while (offsetString.length() < offsetNrBits) {
            offsetString = "0" + offsetString;
        }
        virtualAddressPageNumberField.setText(virtualPageNumberString);
        virtualAddressOffsetField.setText(offsetString);
    }

    private void setPhysicalAddress(int frameNumber) {
        String frameNumberString = Integer.toBinaryString(frameNumber);
        int frameNrBits = physicalAddressNrBits - offsetNrBits;
        while (frameNumberString.length() < frameNrBits) {
            frameNumberString = "0" + frameNumberString;
        }
        physicalAddressFrameNumberField.setText(frameNumberString);
        physicalAddressOffsetField.setText(virtualAddressOffsetField.getText());
    }

    private void executeLoad() {
        try {
            int address = parseAddressTextField();
            errorLabel.setText("");
            loadResult = simulator.loadData(address);
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
        diskObs = extractDiskItems(diskContents);
        diskTable.setItems(diskObs);

        diskHitRow = FXCollections.observableArrayList();
        diskUpdateRow = FXCollections.observableArrayList();

        diskTable.setRowFactory(new Callback<TableView<DiskItem>, TableRow<DiskItem>>() {
            @Override
            public TableRow<DiskItem> call(TableView<DiskItem> tableView) {
                final TableRow<DiskItem> row = new TableRow<DiskItem>() {
                    @Override
                    protected void updateItem(DiskItem diskItem, boolean empty){
                        super.updateItem(diskItem, empty);
                        if (diskHitRow.contains(getIndex())) {
                            if (! getStyleClass().contains("green-highlighted-row")) {
                                getStyleClass().add("green-highlighted-row");
                            }
                        } else {
                            getStyleClass().removeAll(Collections.singleton("green-highlighted-row"));
                            if (diskUpdateRow.contains(getIndex())) {
                                if (! getStyleClass().contains("blue-highlighted-row")) {
                                    getStyleClass().add("blue-highlighted-row");
                                }
                            } else {
                                getStyleClass().removeAll(Collections.singleton("blue-highlighted-row"));
                            }
                        }
                    }
                };
                diskHitRow.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (diskHitRow.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("green-highlighted-row")) {
                                row.getStyleClass().add("green-highlighted-row");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("green-highlighted-row"));
                        }
                    }
                });
                diskUpdateRow.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (diskUpdateRow.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("blue-highlighted-row")) {
                                row.getStyleClass().add("blue-highlighted-row");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("blue-highlighted-row"));
                        }
                    }
                });
                return row;
            }
        });
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
        memoryObs = extractMemoryItems(memoryContents);
        memoryTable.setItems(memoryObs);

        memoryHitRow = FXCollections.observableArrayList();
        memoryUpdateRow = FXCollections.observableArrayList();

        memoryTable.setRowFactory(new Callback<TableView<MemoryItem>, TableRow<MemoryItem>>() {
            @Override
            public TableRow<MemoryItem> call(TableView<MemoryItem> tableView) {
                final TableRow<MemoryItem> row = new TableRow<MemoryItem>() {
                    @Override
                    protected void updateItem(MemoryItem memoryItem, boolean empty){
                        super.updateItem(memoryItem, empty);
                        if (memoryHitRow.contains(getIndex())) {
                            if (! getStyleClass().contains("green-highlighted-row")) {
                                getStyleClass().add("green-highlighted-row");
                            }
                        } else {
                            getStyleClass().removeAll(Collections.singleton("green-highlighted-row"));
                            if (memoryUpdateRow.contains(getIndex())) {
                                if (! getStyleClass().contains("blue-highlighted-row")) {
                                    getStyleClass().add("blue-highlighted-row");
                                }
                            } else {
                                getStyleClass().removeAll(Collections.singleton("blue-highlighted-row"));
                            }
                        }
                    }
                };
                memoryHitRow.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (memoryHitRow.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("green-highlighted-row")) {
                                row.getStyleClass().add("green-highlighted-row");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("green-highlighted-row"));
                        }
                    }
                });
                memoryUpdateRow.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (memoryUpdateRow.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("blue-highlighted-row")) {
                                row.getStyleClass().add("blue-highlighted-row");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("blue-highlighted-row"));
                        }
                    }
                });
                return row;
            }
        });
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
        pageTableObs = extractPageTableEntries(pageTableContents);
        pageTable.setItems(pageTableObs);

        pageTableHitRow = FXCollections.observableArrayList();
        pageTableUpdateRow = FXCollections.observableArrayList();

        pageTable.setRowFactory(new Callback<TableView<PageTableItem>, TableRow<PageTableItem>>() {
            @Override
            public TableRow<PageTableItem> call(TableView<PageTableItem> tableView) {
                final TableRow<PageTableItem> row = new TableRow<PageTableItem>() {
                    @Override
                    protected void updateItem(PageTableItem pageTableItem, boolean empty){
                        super.updateItem(pageTableItem, empty);
                        if (pageTableHitRow.contains(getIndex())) {
                            if (! getStyleClass().contains("green-highlighted-row")) {
                                getStyleClass().add("green-highlighted-row");
                            }
                        } else {
                            getStyleClass().removeAll(Collections.singleton("green-highlighted-row"));
                            if (pageTableUpdateRow.contains(getIndex())) {
                                if (! getStyleClass().contains("blue-highlighted-row")) {
                                    getStyleClass().add("blue-highlighted-row");
                                }
                            } else {
                                getStyleClass().removeAll(Collections.singleton("blue-highlighted-row"));
                            }
                        }
                    }
                };
                pageTableHitRow.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (pageTableHitRow.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("green-highlighted-row")) {
                                row.getStyleClass().add("green-highlighted-row");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("green-highlighted-row"));
                        }
                    }
                });
                pageTableUpdateRow.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (pageTableUpdateRow.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("blue-highlighted-row")) {
                                row.getStyleClass().add("blue-highlighted-row");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("blue-highlighted-row"));
                        }
                    }
                });
                return row;
            }
        });
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


        tlbHitRow = FXCollections.observableArrayList();
        tlbUpdateRow = FXCollections.observableArrayList();

        tlbTable.setRowFactory(new Callback<TableView<PageTableItem>, TableRow<PageTableItem>>() {
            @Override
            public TableRow<PageTableItem> call(TableView<PageTableItem> tableView) {
                final TableRow<PageTableItem> row = new TableRow<PageTableItem>() {
                    @Override
                    protected void updateItem(PageTableItem pageTableItem, boolean empty){
                        super.updateItem(pageTableItem, empty);
                        if (tlbHitRow.contains(getIndex())) {
                            if (! getStyleClass().contains("green-highlighted-row")) {
                                getStyleClass().add("green-highlighted-row");
                            }
                        } else {
                            getStyleClass().removeAll(Collections.singleton("green-highlighted-row"));
                            if (tlbUpdateRow.contains(getIndex())) {
                                if (! getStyleClass().contains("blue-highlighted-row")) {
                                    getStyleClass().add("blue-highlighted-row");
                                }
                            } else {
                                getStyleClass().removeAll(Collections.singleton("blue-highlighted-row"));
                            }
                        }

                    }
                };
                tlbHitRow.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (tlbHitRow.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("green-highlighted-row")) {
                                row.getStyleClass().add("green-highlighted-row");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("green-highlighted-row"));
                        }
                    }
                });

                tlbUpdateRow.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (tlbUpdateRow.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("blue-highlighted-row")) {
                                row.getStyleClass().add("blue-highlighted-row");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("blue-highlighted-row"));
                        }
                    }
                });
                return row;
            }
        });



    }

}
