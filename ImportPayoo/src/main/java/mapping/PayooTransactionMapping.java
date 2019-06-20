package mapping;

import de.bytefish.pgbulkinsert.mapping.AbstractMapping;
import model.PayooTransaction;

public class PayooTransactionMapping  extends AbstractMapping<PayooTransaction> {
    public PayooTransactionMapping() {
        super("payoo", "fico_payoo_imp");

        mapString("trans_date", PayooTransaction::getTrans_date);
        mapString("create_date", PayooTransaction::getCreate_date);
        mapString("vendor_code", PayooTransaction::getVendor_code);
        mapString("order_code", PayooTransaction::getOrder_code);
        mapString("amount", PayooTransaction::getAmount);
        mapString("full_name", PayooTransaction::getFull_name);
        mapString("is_completed", PayooTransaction::getIs_complete);
        mapString("client_code", PayooTransaction::getClient_code);
    }
}
