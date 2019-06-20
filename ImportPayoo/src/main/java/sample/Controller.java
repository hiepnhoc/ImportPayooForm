package sample;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import de.bytefish.pgbulkinsert.util.PostgreSqlUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import mapping.PayooTransactionMapping;
import model.PayooTransaction;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Controller {
    @FXML
    public AnchorPane archorPane;

    @FXML
    public TextField txtFile;

    @FXML
    public DatePicker cadenlar;

    @FXML
    public ProgressIndicator loading;

    @FXML
    public Button btnSettle;

    @FXML
    public TextField txtRecord;

    public static final String DB_URL = "jdbc:postgresql://10.1.66.30:5435/ficodb";
    public static final String USER = "postgres";
    public static final String PASS = "postgres";

    public Controller()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDateTime now = LocalDateTime.now();
        String trans_date=now.format(formatter);
        cadenlar=new DatePicker(LocalDate.of(1998, 10, 8));
        cadenlar.setValue(LocalDate.of(1998, 10, 8));
    }


    public void load(ActionEvent event) throws IOException, SQLException {
        //loading.setProgress(0);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        File file = fileChooser.showOpenDialog(archorPane.getScene().getWindow());
        txtFile.setText(file.getAbsolutePath());


    }

    public void execute(ActionEvent event) throws SQLException, IOException {
        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File(txtFile.getText()));

        // Retrieving the number of sheets in the Workbook
        System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

        //read data excel to list object
        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);
        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        List<PayooTransaction> payooTransactionList=new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDateTime now = LocalDateTime.now();
        String trans_date=now.format(formatter);
        for (int i=3;i< sheet.getPhysicalNumberOfRows();i++) {
            Row row=sheet.getRow(i);
            if(row.getCell(1)!=null && !dataFormatter.formatCellValue(sheet.getRow(i).getCell(1)).equals("")) {
                PayooTransaction payooTransaction = PayooTransaction.builder().trans_date(trans_date)
                        .create_date(dataFormatter.formatCellValue(sheet.getRow(i).getCell(0)))
                        .vendor_code(dataFormatter.formatCellValue(sheet.getRow(i).getCell(1)))
                        .order_code(dataFormatter.formatCellValue(sheet.getRow(i).getCell(2)))
                        .amount(dataFormatter.formatCellValue(sheet.getRow(i).getCell(4)).replace(",",""))
                        .full_name(dataFormatter.formatCellValue(sheet.getRow(i).getCell(5)))
                        .client_code(dataFormatter.formatCellValue(sheet.getRow(i).getCell(3))).build();

                payooTransactionList.add(payooTransaction);
            }
        }

        //insert database
        insertDatabase(payooTransactionList,trans_date);

        //
        txtRecord.setText(String.valueOf(payooTransactionList.size()));

        //enable button
        btnSettle.setDisable(false);
    }

    public void settle(ActionEvent event) throws SQLException {
        settleStore();
        btnSettle.setDisable(true);
    }

    private void insertDatabase(List<PayooTransaction> payooTransactionList,String transDate) throws SQLException {
        String updateStmt ="INSERT INTO payoo.fico_payoo_imp\n" +
                "(trans_date, create_date, vendor_code, order_code, amount, full_name,is_completed, client_code)\n" +
                "VALUES\n" +
                "('%s', '%s','%s','%s', '%s','%s','0','%s');\n";

        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();

        String sql="";
        for (PayooTransaction payooTransaction : payooTransactionList){
            sql+=String.format(updateStmt,payooTransaction.getTrans_date(),payooTransaction.getCreate_date(),
                    payooTransaction.getVendor_code(),payooTransaction.getOrder_code(), payooTransaction.getAmount(),
                    payooTransaction.getFull_name(), payooTransaction.getClient_code());

        }
        int result =stmt.executeUpdate(sql);

        System.out.println("insertDatabase: OK" + result);

        conn.close();
    }

    private void settleStore() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDateTime now = LocalDateTime.now();
        String trans_date=now.format(formatter);

        String storeProc=String.format("CALL payoo.sp_insert_payment_settle('%s');",trans_date);
        int result =stmt.executeUpdate(storeProc);

        System.out.println("settleStore: OK" + result);

        conn.close();
    }
}
