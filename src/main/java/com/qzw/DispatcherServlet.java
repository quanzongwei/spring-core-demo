package com.qzw;

import com.qzw.annotaion.Autowire;
import com.qzw.annotaion.Component;
import com.qzw.annotaion.Controller;
import com.qzw.annotaion.RequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author BG388892
 * @date 2019/4/29
 */
public class DispatcherServlet extends HttpServlet {
    private Properties properties;
    private List<String> classNames = new ArrayList<>();
    private Map<String, Object> iocContainer = new HashMap<>();
    private Map<String, Method> handlerMapping = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        String requestUrl = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestUrl.replace(contextPath, "").replaceAll("/+", "/");
        Method method = handlerMapping.get(url);
        String clazzName = method.getDeclaringClass().getCanonicalName();
        Object controller = iocContainer.get(clazzName);
        try {
            method.invoke(controller, req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 加载配置文件
        String location = config.getInitParameter("contextConfigLocation");
        URL resource = this.getClass().getClassLoader().getResource(location);
        //请注意classLoader加载的文件流是类目录下的,和com平级.这个很好理解, 因为他是classLoader啊
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(location);
        properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 2. 扫描所有的类
        doScan(String.valueOf(properties.get("scanPackage")));
        // 3. 初始化所有的类,保存到IOC容器中
        doInstance();
        // 4. 依赖注入
        doAutowire();
        // 5. 构造handlerMapping
        initHandlerMapping();
        System.out.println("servlet初始化成功");
        super.init(config);
    }

    private void initHandlerMapping() {
        for (Object one : iocContainer.values()) {
            if (one.getClass().isAnnotationPresent(Controller.class)) {
                String base = "/";
                if (one.getClass().isAnnotationPresent(RequestMapping.class)) {
                    base = base + one.getClass().getAnnotation(RequestMapping.class).value();
                }
                Method[] methods = one.getClass().getMethods();
                for (Method method : methods) {
                    String methodUrl = base;
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        methodUrl = methodUrl + "/" + method.getAnnotation(RequestMapping.class).value();
                        methodUrl = methodUrl.replaceAll("/+", "/");
                        method.getDeclaringClass().getCanonicalName().concat(".").concat(method.getName());
                        handlerMapping.put(methodUrl, method);
                    }
                }
            }
        }
    }


    /**
     * 容器循环注入就好了, 我这里简化处理, 全部扫描
     * spring 是
     */
    private void doAutowire() {
        for (Object o : iocContainer.values()) {
            Field[] fields = o.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowire.class)) {
                    Class<?> clazz = field.getType();
                    for (Object obj : iocContainer.values()) {
                        if (clazz.isInstance(obj)) {
                            field.setAccessible(true);
                            try {
                                field.set(o, obj);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void doInstance() {
        try {
            for (String one : classNames) {
                Class clazz = Class.forName(one);
                Annotation annotation = getAnnotation(clazz, Component.class);
                if (annotation != null) {
                    Object instance = clazz.newInstance();
                    iocContainer.put(instance.getClass().getCanonicalName(), instance);
                    if (clazz.isAnnotationPresent(Controller.class)) {

                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private void doScan(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File file = new File(url.getFile());
        for (File one : file.listFiles()) {
            if (one.isDirectory()) {
                doScan(scanPackage + "." + one.getName());
            } else {
                classNames.add(scanPackage + "." + one.getName().replaceAll("\\.class", ""));
            }
        }
    }

    /**
     * 查看是不是级联的注解
     */
    public static Annotation getAnnotation(AnnotatedElement annotatedElement, Class annotationType) {
        try {
            Annotation ex = annotatedElement.getAnnotation(annotationType);
            if (ex == null) {
                Annotation[] var3 = annotatedElement.getAnnotations();
                int var4 = var3.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    Annotation metaAnn = var3[var5];
                    ex = metaAnn.annotationType().getAnnotation(annotationType);
                    if (ex != null) {
                        break;
                    }
                }
            }
            return ex;
        } catch (Throwable var7) {
            return null;
        }
    }
}
