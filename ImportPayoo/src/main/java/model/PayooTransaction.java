package model;


import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayooTransaction {
    private String id;
    private String trans_date;
    private String create_date;
    private String vendor_code;
    private String order_code;
    private String amount;
    private String full_name;
    private String client_code;
    private String is_complete;

    public LocalDateTime getCreateDateFormat(){
        DateTimeFormatter formatterIns = DateTimeFormatter.ofPattern("M/d/yyyy H:m:s");
        return LocalDateTime.parse(this.create_date, formatterIns);
    }

}
