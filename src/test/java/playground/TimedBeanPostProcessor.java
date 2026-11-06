package playground;

import org.aopalliance.intercept.MethodInterceptor;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class TimedBeanPostProcessor implements BeanPostProcessor {

    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var methods = bean.getClass().getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(Timed.class)) {
                ProxyFactory pf = new ProxyFactory(bean);
                pf.setProxyTargetClass(true);
                pf.addAdvice((MethodInterceptor) invocation -> {
                    if (invocation.getMethod().getAnnotation(Timed.class) != null) {
                        var t = System.nanoTime();
                        Object obj = null;
                        try {
                            obj = invocation.proceed();
                        }  catch (Throwable throwable) {
                            System.out.println("[Timed] " + invocation.getMethod().getName() + " took " + (System.nanoTime() - t) / 1000000 + "ms");
                            throw throwable;
                        }
                        System.out.println("[Timed] " + invocation.getMethod().getName() + " took " + (System.nanoTime() - t) / 1000000 + "ms");
                        return obj;
                    } else {
                        return invocation.proceed();
                    }
                });
                return pf.getProxy();
            }
        }
        return bean;
    }
}
