package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends Application {

    @FXML
    public DatePicker cadenlar;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        primaryStage.setTitle("Upload Payoo Transaction");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        //cadenlar.setValue(LocalDate.now());
    }


    public static void main(String[] args) {
        launch(args);
    }
}
