package com.lagou.study.service.impl;


import com.lagou.study.annotations.LagouStudyAutowired;
import com.lagou.study.annotations.LagouStudyService;
import com.lagou.study.annotations.LagouStudyTransactional;
import com.lagou.study.dao.AccountDao;
import com.lagou.study.service.TransferService;
import com.lagou.study.pojo.Account;
import com.lagou.study.utils.TransactionManager;

@LagouStudyTransactional("cglib")
@LagouStudyService("transferService")
public class TransferServiceImpl implements TransferService {

    // private AccountDao accountDao = new JdbcAccountDaoImpl();

    // 仅申明dao层接口
//    private AccountDao accountDao;
    // 提供 set 方法供外部注入dao层实现类对象
//    public void setAccountDao(AccountDao accountDao) {
//        this.accountDao = accountDao;
//    }

    @LagouStudyAutowired
    private AccountDao accountDao;

    @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {
       /* try{
            // 开启事务(关闭事务的自动提交)
            transactionManager.beginTransaction();*/

            Account from = accountDao.queryAccountByCardNo(fromCardNo);
            Account to = accountDao.queryAccountByCardNo(toCardNo);

            from.setMoney(from.getMoney()-money);
            to.setMoney(to.getMoney()+money);

            accountDao.updateAccountByCardNo(to);
//            int c = 1/0;
            accountDao.updateAccountByCardNo(from);

        /*    // 提交事务
            transactionManager.commit();
        }catch (Exception e) {
            e.printStackTrace();
            // 回滚事务
            transactionManager.rollback();

            // 抛出异常便于上层servlet捕获
            throw e;
        }*/
    }

}
