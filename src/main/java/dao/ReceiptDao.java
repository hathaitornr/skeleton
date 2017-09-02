package dao;

import api.ReceiptResponse;
import generated.tables.records.ReceiptsRecord;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static generated.Tables.RECEIPTS;

public class ReceiptDao {
    DSLContext dsl;

    public ReceiptDao(Configuration jooqConfig) {
        this.dsl = DSL.using(jooqConfig);
    }

    public int insert(String merchantName, BigDecimal amount) {
        ReceiptsRecord receiptsRecord = dsl
                .insertInto(RECEIPTS, RECEIPTS.MERCHANT, RECEIPTS.AMOUNT)
                .values(merchantName, amount)
                .returning(RECEIPTS.ID)
                .fetchOne();

        checkState(receiptsRecord != null && receiptsRecord.getId() != null, "Insert failed");

        return receiptsRecord.getId();
    }

    public List<ReceiptsRecord> getAllReceipts() {
        return dsl.selectFrom(RECEIPTS).fetch();
    }

    public List<ReceiptsRecord> checkTag(String tag, int receiptId){
        return dsl.selectFrom(RECEIPTS).where(RECEIPTS.TAG.eq(tag)).and(RECEIPTS.ID.eq(receiptId)).fetch();

    }

    public void putTag(String tag, int receiptId) {
        dsl.update(RECEIPTS)
                .set(RECEIPTS.TAG, tag)
                .where(RECEIPTS.ID.eq(receiptId))
                .execute();
    }

    public List<ReceiptsRecord> getReceiptsWithTag(String tag) {
        return dsl.selectFrom(RECEIPTS).where(RECEIPTS.TAG.eq(tag)).fetch();
    }

}
