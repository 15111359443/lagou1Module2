package com.lagou.study.dao;

import com.lagou.study.pojo.Account;

public interface AccountDao {

    Account queryAccountByCardNo(String cardNo) throws Exception;

    int updateAccountByCardNo(Account account) throws Exception;
}
