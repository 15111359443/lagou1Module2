package com.lagou.study.factory;

import com.lagou.study.annotations.LagouStudyAutowired;
import com.lagou.study.annotations.LagouStudyRepository;
import com.lagou.study.annotations.LagouStudyService;
import com.lagou.study.annotations.LagouStudyTransactional;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工厂类，生产对象（使用反射技术）
 */
public class BeanFactory {

    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static Map<String, Object> map = new HashMap<>();  // 存储对象


    static {
        // 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
        // 加载xml
        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        // 解析xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> beanList = rootElement.selectNodes("//bean");
            for (int i = 0; i < beanList.size(); i++) {
                Element element =  beanList.get(i);
                // 处理每个bean元素，获取到该元素的id 和 class 属性
                String id = element.attributeValue("id");        // accountDao
                String clazz = element.attributeValue("class");  // com.lagou.edu.dao.impl.JdbcAccountDaoImpl
                // 通过反射技术实例化对象
                Class<?> aClass = Class.forName(clazz);
                Object o = aClass.newInstance();  // 实例化之后的对象

                // 存储到map中待用
                map.put(id,o);

            }

            // 实例化完成之后维护对象的依赖关系，检查哪些对象需要传值进入，根据它的配置，我们传入相应的值
            // 有property子元素的bean就有传值需求
            List<Element> propertyList = rootElement.selectNodes("//property");
            // 解析property，获取父元素
            for (int i = 0; i < propertyList.size(); i++) {
                Element element =  propertyList.get(i);   //<property name="AccountDao" ref="accountDao"></property>
                String name = element.attributeValue("name");
                String ref = element.attributeValue("ref");

                // 找到当前需要被处理依赖关系的bean
                Element parent = element.getParent();

                // 调用父元素对象的反射功能
                String parentId = parent.attributeValue("id");
                Object parentObject = map.get(parentId);
                // 遍历父对象中的所有方法，找到"set" + name
                Method[] methods = parentObject.getClass().getMethods();
                for (int j = 0; j < methods.length; j++) {
                    Method method = methods[j];
                    if(method.getName().equalsIgnoreCase("set" + name)) {  // 该方法就是 setAccountDao(AccountDao accountDao)
                        method.invoke(parentObject,map.get(ref));
                    }
                }

                // 把处理之后的parentObject重新放到map中
                map.put(parentId,parentObject);

            }

            // 注解扫描，把相关注解的对象添加到 map 存储对象中
            List<Element> componentScanBeanList =  rootElement.selectNodes("//component-scan");
            for(Element element : componentScanBeanList){
                componentScan(element.attributeValue("base-package"));
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @describe: 注解扫描
     * @param basePackage 扫描路径
     **/
    private static void componentScan(String basePackage) throws Exception{
        // scanPackage: com.lagou.studymvc.demo  package---->  磁盘上的文件夹（File）  com/lagou/studymvc/demo
        String scanPackagePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + basePackage.replaceAll("\\.", "/");
        File pack = new File(scanPackagePath);

        File[] files = pack.listFiles();

        for(File file: files) {
            if(file.isDirectory()) { // 子package
                // 递归
                componentScan(basePackage + "." + file.getName());  // com.lagou.studymvc.demo.controller
            }else if(file.getName().endsWith(".class")) {
                // 获取权限定类名
                String className = basePackage + "." + file.getName().replaceAll(".class", "");
                // 反射获取Class
                Class<?> aClass = Class.forName(className);

                // 区分service类
                if(aClass.isAnnotationPresent(LagouStudyService.class)) {
                    // 获取注解信息
                    LagouStudyService annotation = aClass.getAnnotation(LagouStudyService.class);
                    // 获取注解value值
                    String beanName = annotation.value();

                    // 如果指定了id，就以指定的为准
                    if(!"".equals(beanName.trim())) {
                        map.put(beanName,aClass.newInstance());
                    }else{
                        // 如果没有指定，就以类名首字母小写
                        beanName = lowerFirst(aClass.getSimpleName());
                        map.put(beanName,aClass.newInstance());
                    }

                    // service层往往是有接口的，面向接口开发，此时再以接口名为id，放入一份对象到ioc中，便于根据接口类型注入
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (int j = 0; j < interfaces.length; j++) {
                        Class<?> anInterface = interfaces[j];
                        // 以接口的全限定类名作为id放入
                        map.put(anInterface.getName(), aClass.newInstance());
                    }
                }
                // dao 层注解
                else if(aClass.isAnnotationPresent(LagouStudyRepository.class)) {
                    // 获取注解信息
                    LagouStudyRepository annotation = aClass.getAnnotation(LagouStudyRepository.class);
                    // 获取注解value值
                    String beanName = annotation.value();

                    // 如果指定了id，就以指定的为准
                    if(!"".equals(beanName.trim())) {
                        map.put(beanName,aClass.newInstance());
                    }else{
                        // 如果没有指定，就以类名首字母小写
                        beanName = lowerFirst(aClass.getSimpleName());
                        map.put(beanName,aClass.newInstance());
                    }

                    // dao层也是有接口的，面向接口开发，此时再以接口名为id，放入一份对象到ioc中，便于根据接口类型注入
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (int j = 0; j < interfaces.length; j++) {
                        Class<?> anInterface = interfaces[j];
                        // 以接口的全限定类名作为id放入
                        map.put(anInterface.getName(), aClass.newInstance());
                    }
                }
            }
        }

        // 实现依赖注入
        lagouStudyAutowired();

        // 实现事务声明
        lagouStudyTransactional();
    }

    /**
     * @describe: 实现依赖注入
     **/
    private static void lagouStudyAutowired() throws Exception{
        // 判断缓存的ioc容器集合中是否有数据
        if(map.isEmpty()) {return;}

        // 有对象，再进行依赖注入处理

        // 遍历ioc中所有对象，查看对象中的字段，是否有@LagouStudyAutowired注解，如果有需要维护依赖注入关系
        for(Map.Entry<String,Object> entry: map.entrySet()) {
            // 获取bean对象中的字段信息
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            // 遍历判断处理
            for (int i = 0; i < declaredFields.length; i++) {
                Field declaredField = declaredFields[i];   //  @LagouStudyAutowired  private IDemoService demoService;

                // 判断是否有 LagouStudyAutowired 注解；若没有，跳过当前循环
                if(!declaredField.isAnnotationPresent(LagouStudyAutowired.class)) { continue; }

                // 有该注解
                LagouStudyAutowired annotation = declaredField.getAnnotation(LagouStudyAutowired.class);
                String beanName = annotation.value();  // 需要注入的bean的id
                if("".equals(beanName.trim())) {
                    // 没有配置具体的bean id，那就需要根据当前字段类型注入（接口注入）  IDemoService
                    beanName = declaredField.getType().getName();
                }

                // 开启赋值
                declaredField.setAccessible(true);
                declaredField.set(entry.getValue(), map.get(beanName));
            }
        }
    }

    /**
     * @describe: 实现事务声明
     **/
    private static void lagouStudyTransactional() throws Exception{
        // 判断缓存的ioc容器集合中是否有数据
        if(map.isEmpty()) {return;}

        // 遍历ioc中所有对象，查看对象@LagouStudyTransactional注解，如果有需要维护事务声明关系
        for(Map.Entry<String,Object> entry: map.entrySet()) {
            // 判断是否有事务注解
            if(entry.getValue().getClass().isAnnotationPresent(LagouStudyTransactional.class)) {
                // 获取注解信息
                LagouStudyTransactional annotation = entry.getValue().getClass().getAnnotation(LagouStudyTransactional.class);
                // 获取注解value值
                String proxyType = annotation.value();


                // 获取代理对象
                ProxyFactory proxyFactory = (ProxyFactory) BeanFactory.getBean("proxyFactory");
                // 实现代理方式
                if(proxyType != null && proxyType.equals("cglib")){
                    map.put(entry.getKey(), proxyFactory.getCglibProxy(entry.getValue()));
                }else{
                    map.put(entry.getKey(), proxyFactory.getJdkProxy(entry.getValue()));
                }

            }
        }
    }

    /**
     * @describe: 首字母小写方法
     **/
    private static String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        if('A' <= chars[0] && chars[0] <= 'Z') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }


    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static Object getBean(String id) {
        return map.get(id);
    }

}
