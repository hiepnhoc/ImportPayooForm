package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import model.PayooTransaction;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.stream.Stream;

public class Controller implements Initializable {
    @FXML
    public AnchorPane archorPane;

    @FXML
    public TextField txtFile;

    @FXML
    public DatePicker cadenlar;

    @FXML
    public Button btnSettle;

    @FXML
    public TextField txtRecord;

    @FXML
    public Button btnSync;

    public static final String DB_URL = "jdbc:postgresql://10.1.66.30:5435/ficodb";
    //public static final String DB_URL_PRO = "jdbc:postgresql://10.1.65.23:5435/ficodb";
    public static final String USER = "postgres";
    public static final String PASS = "postgres";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cadenlar.setValue(LocalDate.now());
        // Window window=archorPane.getScene().getWindow();
        //window.setOnCloseRequest(e->closeProgram());

    }

    public void shutdown() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Are u want to close", ButtonType.APPLY);
        a.showAndWait();
    }

    public void load(ActionEvent event) throws IOException, SQLException {
        try {
            //loading.setProgress(0);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open file");
            File file = fileChooser.showOpenDialog(archorPane.getScene().getWindow());

            if (file != null) {
                txtFile.setText(file.getAbsolutePath());
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.showAndWait();
        }
    }

    public void execute(ActionEvent event) throws SQLException, IOException, InterruptedException {

        if (txtFile.getText() == null || txtFile.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File not upload !!!");
            alert.showAndWait();
            return;
        }

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File(txtFile.getText()));

        // Retrieving the number of sheets in the Workbook
        System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");


        Label processResult = new Label();
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "Operation in progress"
        );

        ProgressIndicator progressIndi=new ProgressIndicator();

        alert.setTitle("Running Operation");
        alert.setHeaderText("Please wait... ");
        alert.setGraphic(progressIndi);
        Task<Void> task = new Task<Void>() {
            final int N_ITERATIONS = 5;

            {
                setOnFailed(a -> {
//                    alert.close();
//                    Alert alert1 = new Alert(
//                            Alert.AlertType.INFORMATION,
//                            getException().getMessage().substring(0,20)
//                    );
                    //alert1.showAndWait();
                    alert.setHeaderText("ERROR");
                    alert.setContentText(getException().getMessage());

                    updateMessage("Failed");


                    btnSettle.setDisable(true);
                });
                setOnSucceeded(a -> {
                    alert.close();
                    updateMessage("Succeeded");
                    btnSettle.setDisable(false);
                });
                setOnCancelled(a -> {
                    alert.close();
                    updateMessage("Cancelled");
                    btnSettle.setDisable(true);
                });
            }

            @Override
            protected Void call() throws Exception {
                updateMessage("Processing");

//                int i;
//                for (i = 0; i < N_ITERATIONS; i++) {
//                    if (isCancelled()) {
//                        break;
//                    }
//
//                    updateProgress(i, N_ITERATIONS);
//
//                    try {
//                        Thread.sleep(1_000);
//                    } catch (InterruptedException e) {
//                        Thread.interrupted();
//                    }
//                }

                //read data excel to list object
                // Getting the Sheet at index zero
                Sheet sheet = workbook.getSheetAt(0);
                // Create a DataFormatter to format and get each cell's value as String
                DataFormatter dataFormatter = new DataFormatter();

                List<PayooTransaction> payooTransactionList = new ArrayList<>();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                DateTimeFormatter formatterIns = DateTimeFormatter.ofPattern("M/d/yyyy H:m:s");
                LocalDateTime now = LocalDateTime.now();
                String trans_date = now.format(formatter);

                DateTimeFormatter formatterParse = DateTimeFormatter.ofPattern("d/M/yyyy H:m:s");

                for (int i = 3; i < sheet.getPhysicalNumberOfRows(); i++) {
                    Row row = sheet.getRow(i);

                    if (row.getCell(1) != null && !dataFormatter.formatCellValue(sheet.getRow(i).getCell(1)).equals("")) {
                        String s=dataFormatter.formatCellValue(sheet.getRow(i).getCell(0));
                        LocalDateTime ld=LocalDateTime.parse(dataFormatter.formatCellValue(sheet.getRow(i).getCell(0)),formatterParse);
                        //String d=ld.format()
                        PayooTransaction payooTransaction = PayooTransaction.builder().trans_date(trans_date)
                                .create_date(LocalDateTime.parse(dataFormatter.formatCellValue(sheet.getRow(i).getCell(0)),formatterParse).format(formatterIns))
                                .vendor_code(dataFormatter.formatCellValue(sheet.getRow(i).getCell(1)))
                                .order_code(dataFormatter.formatCellValue(sheet.getRow(i).getCell(2)))
                                .amount(dataFormatter.formatCellValue(sheet.getRow(i).getCell(4)).replace(",", ""))
                                .full_name(dataFormatter.formatCellValue(sheet.getRow(i).getCell(5)).replace("'","''"))
                                .client_code(dataFormatter.formatCellValue(sheet.getRow(i).getCell(3))).build();

                        payooTransactionList.add(payooTransaction);
                    }
                }

                //get ra maxdate
                Comparator<PayooTransaction> comparator = Comparator.comparing(PayooTransaction::getCreate_date);
                PayooTransaction minObject = payooTransactionList.stream().max(comparator).get();



                //convert String to LocalDate
                LocalDateTime localDate = LocalDateTime.parse(minObject.getCreate_date(), formatterIns);
               //trans_date = localDate.format(formatter);

                trans_date=localDate.toLocalDate().format(formatter);

                //insert database: sua lai la get max createion date truyen vao, sua ngay 26/6
                insertDatabase(payooTransactionList, trans_date);

                //set value cadenlar
                cadenlar.setValue(localDate.toLocalDate());

                //
                txtRecord.setText(String.valueOf(payooTransactionList.size()));

//                if (!isCancelled()) {
//                    updateProgress(i, N_ITERATIONS);
//                }

                return null;
            }
        };

        progressIndi.progressProperty().bind(task.progressProperty());
        processResult.textProperty().unbind();
        processResult.textProperty().bind(task.messageProperty());

        Thread taskThread = new Thread(
                task
        );
        taskThread.start();

        alert.initOwner(archorPane.getScene().getWindow());
        Optional<ButtonType> result = alert.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.CANCEL && task.isRunning()) {
//            task.cancel();
//        }
//        //enable button
//        btnSettle.setDisable(false);

        //reset txt file
        txtFile.setText("");
    }

    public void settle(ActionEvent event) throws SQLException {
        LocalDate ld = cadenlar.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String trans_date = ld.format(formatter);

        settleStore(trans_date);


        btnSettle.setDisable(true);
    }

    private void insertDatabase(List<PayooTransaction> payooTransactionList, String transDate) throws SQLException {
        String updateStmt = "INSERT INTO payoo.fico_payoo_imp\n" +
                "(trans_date, create_date, vendor_code, order_code, amount, full_name,is_completed, client_code)\n" +
                "VALUES\n" +
                "('%s', '%s','%s','%s', '%s','%s','0','%s');\n";

        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();

        String sql = "";
        for (PayooTransaction payooTransaction : payooTransactionList) {
            sql += String.format(updateStmt,transDate, payooTransaction.getCreate_date(),
                    payooTransaction.getVendor_code(), payooTransaction.getOrder_code(), payooTransaction.getAmount(),
                    payooTransaction.getFull_name(), payooTransaction.getClient_code());

        }
        int result = stmt.executeUpdate(sql);

        System.out.println("insertDatabase: OK" + result);

        conn.close();
    }

    private void settleStore(String trans_date) throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
//        LocalDateTime now = LocalDateTime.now();
//        String trans_date=now.format(formatter);

            String storeProc = String.format("CALL payoo.sp_insert_payment_settle('%s');", trans_date);
            int result = stmt.executeUpdate(storeProc);

            System.out.println("settleStore: OK" + result);

            conn.close();

            Alert alert1 = new Alert(
                    Alert.AlertType.INFORMATION,
                    "Settle successfully!"
            );
            alert1.showAndWait();
        } catch (Exception e) {
            Alert alert1 = new Alert(
                    Alert.AlertType.ERROR,
                    e.getMessage()
            );
            alert1.showAndWait();
        }
    }

    public void syncData(ActionEvent event) throws IOException, SQLException {
        try {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to reload CustomNetReceivable from F1?", ButtonType.YES, ButtonType.NO);
            ButtonType resultBtn = alert.showAndWait().orElse(ButtonType.NO);

            if (ButtonType.NO.equals(resultBtn)) {
                // no choice or no clicked -> don't close
               return;
            }

            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();

            String storeProc = String.format("CALL payoo.sp_ora_data_net_amount();");
            int result = stmt.executeUpdate(storeProc);

            System.out.println("syncData: OK" + result);

            conn.close();

            Alert alert1 = new Alert(
                    Alert.AlertType.INFORMATION,
                    "Sync data successfully!"
            );
            alert1.showAndWait();
        } catch (Exception e) {
            Alert alert1 = new Alert(
                    Alert.AlertType.ERROR,
                    e.getMessage()
            );
            alert1.showAndWait();
        }
    }
}

