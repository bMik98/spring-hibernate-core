package sorokin.java.course.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccountProperties {
    private final int defaultAmount;
    private final double transferCommission;

    public AccountProperties(
            @Value("${account.default-amount:500}") int defaultAmount,
            @Value("${account.transfer-commission:0.02}") double transferCommission
    ) {
        this.defaultAmount = defaultAmount;
        this.transferCommission = transferCommission;
    }

    public int getDefaultAmount() {
        return defaultAmount;
    }

    public double getTransferCommission() {
        return transferCommission;
    }
}
