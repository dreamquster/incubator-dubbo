package org.apache.dubbo.demo;

import java.math.BigDecimal;

/**
 * Created by dknight on 2018/7/1.
 */
public interface WalletService {
    boolean transferMoney(Integer fromAccount, Integer toAccount, BigDecimal amount);
}
