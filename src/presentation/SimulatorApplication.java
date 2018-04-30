package presentation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SimulatorApplication extends Application {

    private static Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("SimulatorWindow.fxml"));
        stage.setTitle("Virtual Memory Simulator");
        stage.setScene(new Scene(root, 1200, 750));
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return stage;
    }
}