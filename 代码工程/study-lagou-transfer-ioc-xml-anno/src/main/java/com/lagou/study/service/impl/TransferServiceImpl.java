package com.lagou.study.service.impl;


import com.lagou.study.dao.AccountDao;
import com.lagou.study.dao.impl.TemplateDao;
import com.lagou.study.pojo.Account;
import com.lagou.study.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("transferService")
@Transactional
public class TransferServiceImpl implements TransferService {

    // @Autowired 按照类型注入 ,如果按照类型无法唯一锁定对象，可以结合@Qualifier指定具体的id
    @Autowired
    @Qualifier("accountDao")
    private AccountDao accountDao;

    @Autowired
    @Qualifier("templateDao")
    private TemplateDao templateDao;

    @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {

        System.out.println("原有业务逻辑.....");
        /*Account from = accountDao.queryAccountByCardNo(fromCardNo);
        Account to = accountDao.queryAccountByCardNo(toCardNo);

        from.setMoney(from.getMoney()-money);
        to.setMoney(to.getMoney()+money);

        accountDao.updateAccountByCardNo(to);
//         int c = 1/0;
        accountDao.updateAccountByCardNo(from);*/

        // --------------------------------------- 声明式事务 -----------------------------------------

        Account from = templateDao.queryAccountByCardNo(fromCardNo);
        Account to = templateDao.queryAccountByCardNo(toCardNo);

        from.setMoney(from.getMoney()-money);
        to.setMoney(to.getMoney()+money);

        templateDao.updateAccountByCardNo(to);
         int c = 1/0;
        templateDao.updateAccountByCardNo(from);
    }

}
