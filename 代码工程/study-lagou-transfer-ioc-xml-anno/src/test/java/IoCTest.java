import com.lagou.study.dao.AccountDao;
import com.lagou.study.pojo.Company;
import com.lagou.study.service.TransferService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class IoCTest {

    @Test
    public void testIoC(){
        // 方式一：通过读取classpath下的xml文件路径来启动容器（xml模式SE应用下推荐使用）
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        // 方式二：不推荐使用
        // ApplicationContext applicationContext = new FileSystemXmlApplicationContext("文件系统的绝对路径");

        // 获取对象并打印
        Object companyBean = applicationContext.getBean("&companyBean");
        System.out.println(companyBean);

        // 获取对象并打印
        AccountDao accountDao = (AccountDao) applicationContext.getBean("accountDao");
        System.out.println(accountDao);
    }

    /**
     * 测试bean的lazy-init属性
     */
    @Test
    public void testBeanLazy(){
        // 容器初始化
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        // 获取bean对象
        Object lazyResult = applicationContext.getBean("lazyResult");
        System.out.println(lazyResult);
        applicationContext.close();
    }

    @Test
    public void testXmlAop() throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        TransferService transferService = applicationContext.getBean(TransferService.class);
        transferService.transfer("6029621011000","6029621011001",100);
    }

    @Test
    public void testXmlAnnoAop() throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        TransferService transferService = applicationContext.getBean(TransferService.class);
        transferService.transfer("6029621011000","6029621011001",100);
    }
}
