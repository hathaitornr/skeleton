package api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import generated.tables.records.ReceiptsRecord;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalTime;

/**
 * This is an API Object.  Its purpose is to model the JSON API that we expose.
 * This class is NOT used for storing in the Database.
 *
 * This ReceiptResponse in particular is the model of a Receipt that we expose to users of our API
 *
 * Any properties that you want exposed when this class is translated to JSON must be
 * annotated with {@link JsonProperty}
 */
@JsonInclude(Include.NON_NULL)
public class ReceiptResponse {
    @JsonProperty
    Integer id;

    @JsonProperty
    Time uploadTime;

    @JsonProperty
    String merchant;

    @JsonProperty
    BigDecimal amount;

    @JsonProperty
    String tag;

    public ReceiptResponse(ReceiptsRecord dbRecord) {
        this.merchant = dbRecord.getMerchant();
        this.amount = dbRecord.getAmount();
        this.id = dbRecord.getId();
        this.uploadTime = dbRecord.getUploaded();
        this.tag = dbRecord.getTag();
    }
}
