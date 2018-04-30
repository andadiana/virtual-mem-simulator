package presentation.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.util.Callback;
import presentation.data.DiskItem;
import presentation.data.MemoryItem;
import presentation.data.PageTableItem;
import simulator.PageTableEntry;
import simulator.VirtualMemorySimulator;

import java.util.Map;
import java.util.stream.Collectors;


public class SimulatorWindow {

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
    private Button button;

    @FXML
    private Label errorLabel;

    @FXML
    private Label simulatorDetailsLabel;

    private VirtualMemorySimulator simulator;
    private String command;
    private int maxAddress;

    @FXML
    private void initialize() {

        int virtualMemorySize = 16;
        int mainMemorySize = 8;
        int pageSize = 2;
        int tlbSize = 3;

        simulator = new VirtualMemorySimulator(virtualMemorySize, mainMemorySize, pageSize, tlbSize);
        maxAddress = simulator.getVirtualMemorySize();

        simulatorDetailsLabel.setText("Simulator details: Virtual memory size: " + virtualMemorySize + ", main memory " +
                "size: " + mainMemorySize + ", page size: " + pageSize + ", tlb size: " + tlbSize);

        ObservableList<String> options = FXCollections.observableArrayList("LOAD", "STORE");
        commandComboBox.setItems(options);
        commandComboBox.getSelectionModel().select(0);
        command = commandComboBox.getSelectionModel().getSelectedItem();
        dataTextField.setVisible(false);
        dataLabel.setVisible(false);

        //initialize tables
        initializeDiskTable();
        initializeMemoryTable();
        initializePageTable();
        initializeTLB();
    }

    @FXML
    public void clickDiskTable(MouseEvent event) {
        System.out.println("Clicked table");
        //System.out.println("Key: " + diskTable.getSelectionModel().getSelectedItem().getVirtualPageNumber());
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
    private void executeCommand(ActionEvent event) {
        switch (command) {
            case "LOAD": executeLoad();
                break;
            case "STORE": executeStore();
                break;
        }
    }

    private void updateTLBTable() {
        Map<Integer, PageTableEntry> tlbContents = simulator.getTLBContents();
        tlbTable.setItems(extractPageTableEntries(tlbContents));
    }

    private void updatePageTable() {
        Map<Integer, PageTableEntry> pageTableContents = simulator.getPageTableContents();
        pageTable.setItems(extractPageTableEntries(pageTableContents));
    }

    private void updateDiskTable() {
        Map<Integer, Map<Integer, Integer>> diskContents = simulator.getDiskContents();
        diskTable.setItems(extractDiskItems(diskContents));
    }

    private void updateMemoryTable() {
        Map<Integer, Map<Integer, Integer>> memoryContents = simulator.getMainMemoryContents();
        memoryTable.setItems(extractMemoryItems(memoryContents));
    }

    private void updateTables() {
        updateTLBTable();
        updatePageTable();
        updateDiskTable();
        updateMemoryTable();
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

    private void executeLoad() {
        try {
            int address = parseAddressTextField();
            errorLabel.setText("");
            simulator.loadData(address);
            updateTables();

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
            updateTables();
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
