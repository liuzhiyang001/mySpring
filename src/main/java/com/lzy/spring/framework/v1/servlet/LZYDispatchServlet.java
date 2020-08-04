package com.lzy.spring.framework.v1.servlet;

import com.lzy.spring.framework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @ Author     ：lzy
 * @ Date       ：Created in  2020/7/28 21:36
 * @ Description：
 */
public class LZYDispatchServlet extends HttpServlet{

    //配置文件
    private Properties contextConfig = new Properties();

    //存储路劲
    private List<String> classPath = new ArrayList<String>();

    //IoC容器
    private Map<String,Object> springIoC = new HashMap<String, Object>();

    //路径容器
    Map<String,Method> handlerMapping = new HashMap<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req,resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        try {
            if (!handlerMapping.containsKey(url)){
                resp.getWriter().print("404 not found");
                return;
            }
            Method method = handlerMapping.get(url);
            Map<String,String[]> params = req.getParameterMap();

            Class<?>[] parameterTypes = method.getParameterTypes();
            Object [] parameterValues=new Object[parameterTypes.length];


            for (int i=0;i<parameterTypes.length;i++) {
                Class paramterType= parameterTypes[i];
                if (paramterType == HttpServletRequest.class){
                    parameterValues[i]=req;
                }if (paramterType == HttpServletResponse.class){
                    parameterValues[i]=resp;
                }if (paramterType == String.class){
//                    Annotation annotationPresent = paramterType.getAnnotation(LZYRequestParam.class);
                    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                    for (Annotation annotation: parameterAnnotations[i]){
                        if (annotation instanceof LZYRequestParam){
                            String value = ((LZYRequestParam) annotation).value();
                            if ("".equals(value)) continue;
                            parameterValues[i]= Arrays.toString(params.get(value))
                                    .replaceAll("\\[|\\]","")
                                    .replaceAll("\\s+",",");
                        }
                    }
                }
            }
            String beanName = toLowerFirstChar(method.getDeclaringClass().getSimpleName());
            method.invoke(springIoC.get(beanName),parameterValues);
        }  catch (Exception e){
            e.printStackTrace();
        }


    }


    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        String properties=servletConfig.getInitParameter("contextConfigLocation");
        //1、加载配置文件
        doConfig(properties);

        //2、获取classpath
        String scanPackage=contextConfig.getProperty("scanPackage");
        doClassPath(scanPackage);

        //3、创建IoC容器容器，与实例化
        createIoC();

        //4、注入
        doAutowired();


        //5、初始化HandlerMapping
        initHandlerMapping();
    }

    /**
     * @Date: 2020/8/3 20:48
     * @description :
     * @author :lzy
     * @params: []
     * @return: void
     * @other: 初始化handlmapping，让url处理方法对应
    */
    private void initHandlerMapping() {
        for (Map.Entry bean: springIoC.entrySet()) {
            Class<?> aClass = bean.getValue().getClass();

            Method[] methods = aClass.getMethods();

            StringBuffer url = new StringBuffer("/");

            if (aClass.isAnnotationPresent(LZYRequestMapping.class)){
                url.append(aClass.getAnnotation(LZYRequestMapping.class).value())
                    .append("/");
            }

            for (Method method:methods) {
                if (method.isAnnotationPresent(LZYRequestMapping.class)){
                    url.append(method.getAnnotation(LZYRequestMapping.class).value());
                    handlerMapping.put(url.toString().replaceAll("/+","/"),method);
                }
            }

        }
    }

    /**
     * @Date: 2020/8/3 20:24
     * @description :
     * @author :lzy
     * @params: []
     * @return: void
     * @other: 注入依赖
    */
    private void doAutowired() {
        for (Map.Entry bean: springIoC.entrySet()) {
            Object value = bean.getValue();
            Field[] declaredFields = value.getClass().getDeclaredFields();
            for (Field field: declaredFields) {
                if (!field.isAnnotationPresent(LZYAutoWired.class)){
                    continue;
                }
                String autowiredValueName = field.getDeclaredAnnotation(LZYAutoWired.class).value().trim();
                if ("".equals(autowiredValueName)){
                    autowiredValueName = field.getType().getName();
                }
                field.setAccessible(true);


                try {
                    field.set(bean.getValue(),springIoC.get(autowiredValueName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     * @Date: 2020/7/30 23:23
     * @description :
     * @author :lzy
     * @params: []
     * @return: void
     * @other: 实体创建
    */
    private void createIoC() {
        for (String className:classPath)
            try {
                Class<?> aClass = Class.forName(className);
                Object instance = null;
                String springIoCKey = null;
                //什么样的类需要注入，而什么类需要实例化
                if (aClass.isAnnotationPresent(LZYController.class)) {
                    instance = aClass.newInstance();
                    springIoCKey=toLowerFirstChar(aClass.getSimpleName());
                }else if (aClass.isAnnotationPresent(LZYService.class)){
                    LZYService annotation = aClass.getAnnotation(LZYService.class);
                    springIoCKey = annotation.value();
                    if("".equals(springIoCKey.trim())){
                        springIoCKey = toLowerFirstChar(aClass.getSimpleName());
                    }
                    instance = aClass.newInstance();
                    for (Class<?> interfaceClass:aClass.getInterfaces()){
                        if (springIoC.containsKey(interfaceClass.getSimpleName())){
                            throw new RuntimeException("this is "+interfaceClass.getName()+"is exit !!");
                        }
                        springIoC.put(interfaceClass.getSimpleName(),instance);
                    }
                }else continue;
                if (instance != null)
                springIoC.put(springIoCKey,instance);
//                aClass.getAnnotations();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
    }
    //首字母小写
    private String toLowerFirstChar(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0]+=32;
        System.out.println(chars.toString());

        return String.valueOf(chars);
    }


    /**
     * @Date: 2020/7/29 20:11
     * @description :
     * @author :lzy
     * @params: []
     * @return: void
     * @other: 加载所有的classpath路劲
    */
    private void doClassPath(String scanPackage) {
        String  resource = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.","/")).getPath();
//        .replaceAll("%20"," ")

        File file = new File(resource);
        for (File file1: file.listFiles()) {
            String filepath = scanPackage + "."+file1.getName();
            if(file1.isDirectory())
                doClassPath(filepath);
            if (file1.getName().endsWith(".class")){
//                resource.replaceAll(".class","");
                classPath.add(filepath.replaceAll(".class",""));
            }
        }
    }
        /**
     * @Date: 2020/7/29 20:09
     * @description :
     * @author :lzy
     * @params: [properties]
     * @return: void
     * @other: 加载获取配置文件
    */
    private void doConfig(String properties) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(properties);
        try {
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
