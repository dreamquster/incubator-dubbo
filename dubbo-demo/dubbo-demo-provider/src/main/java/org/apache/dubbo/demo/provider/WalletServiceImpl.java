package org.apache.dubbo.demo.provider;

import org.apache.dubbo.demo.WalletService;

import java.math.BigDecimal;

/**
 * Created by dknight on 2018/7/1.
 */
public class WalletServiceImpl implements WalletService {
    @Override
    public boolean transferMoney(Integer fromAccount, Integer toAccount, BigDecimal amount) {
        if (fromAccount.equals(110)) {
            throw new IllegalArgumentException("The balance of account is insufficient");
        }
        return false;
    }
}
