package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends Application {

    @FXML
    public Button cadenlar;


    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("sample.fxml"));
        //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        Parent root=loader.load();
        primaryStage.setTitle("Upload Payoo Transaction");
        primaryStage.setScene(new Scene(root, 500, 300));

        Controller controller =  (Controller)loader.getController();

        primaryStage.show();
        primaryStage.setResizable(false);
        //primaryStage.setOnCloseRequest(e->closeProgram());
        //cadenlar.setValue(LocalDate.now());

        // shutdown any running threads when application exit button is pressed

        //primaryStage.setOnCloseRequest(e -> controller.shutdown());
        primaryStage.setOnCloseRequest(evt -> {
            boolean flag = controller.btnSettle.isDisabled();
            if(flag==false) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You dont settle, do you really want to close this applicetion?", ButtonType.YES, ButtonType.NO);
                ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

                if (ButtonType.NO.equals(result)) {
                    // no choice or no clicked -> don't close
                    evt.consume();
                }
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
