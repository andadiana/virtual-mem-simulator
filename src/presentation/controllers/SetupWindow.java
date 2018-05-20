package presentation.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class SetupWindow {

    @FXML
    private TextField virtualMemorySizeField;

    @FXML
    private TextField mainMemorySizeField;

    @FXML
    private TextField pageSizeField;

    @FXML
    private TextField tlbSizeField;

    @FXML
    private Button startSimulationButton;

    @FXML
    private Label errorLabel;

    private int virtualMemSize;
    private int mainMemSize;
    private int pageSize;
    private int tlbSize;

    @FXML
    private void startButtonClicked(ActionEvent event) {

        if (virtualMemorySizeField.getText().length() == 0 ||
                mainMemorySizeField.getText().length() == 0 ||
                pageSizeField.getText().length() == 0 ||
                tlbSizeField.getText().length() == 0) {
            errorLabel.setText("Must not have empty fields!");
        }
        else {
            try {
                parseFields();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/view/SimulatorWindow.fxml"));
                Parent root = loader.load();
                SimulatorWindow simulatorWindowController = loader.getController();
                simulatorWindowController.initData(virtualMemSize, mainMemSize, pageSize, tlbSize);
                Stage stage = new Stage();
                stage.setTitle("Virtual memory simulator");
                Scene scene = new Scene(root, 1000, 670);
                scene.getStylesheets().add(getClass().getResource("../resources/css/style-class.css").toExternalForm());
                stage.setScene(scene);
                stage.show();

                Stage currentStage = (Stage) startSimulationButton.getScene().getWindow();
                currentStage.close();

            }
            catch (Exception e) {
                errorLabel.setText(e.getMessage());
            }
        }
    }

    private void parseFields() throws Exception{
        try {
            virtualMemSize = Integer.parseInt(virtualMemorySizeField.getText());
            mainMemSize = Integer.parseInt(mainMemorySizeField.getText());
            pageSize = Integer.parseInt(pageSizeField.getText());
            tlbSize = Integer.parseInt(tlbSizeField.getText());
        }
        catch (Exception e) {
            throw new Exception("All fields must contain numerical values!");
        }

        if (virtualMemSize % 2 != 0) {
            throw new Exception("Memory size must be a multiple of 2! Please input new size");
        }
        if (mainMemSize % 2 != 0) {
            throw new Exception("Memory size must be a multiple of 2! Please input new size");
        }
        if (pageSize % 2 != 0) {
            throw new Exception("Page size must be a multiple of 2! Please input new size");
        }
        if (mainMemSize > virtualMemSize) {
            throw new Exception("Main memory size must be smaller than virtual memory size! Please input new size");
        }
        if (pageSize > mainMemSize) {
            throw new Exception("Page size must be smaller than main memory size! Please input new size");
        }
        errorLabel.setText("");
    }

}
