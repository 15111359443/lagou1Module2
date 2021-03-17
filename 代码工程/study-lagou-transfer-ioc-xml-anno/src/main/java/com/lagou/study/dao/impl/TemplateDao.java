package com.lagou.study.dao.impl;

import com.lagou.study.pojo.Account;

public interface TemplateDao {

    Account queryAccountByCardNo(String cardNo) throws Exception;

    int updateAccountByCardNo(Account account) throws Exception;
}
