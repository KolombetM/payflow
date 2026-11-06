package playground;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class LoginBeanPostProcessor implements BeanPostProcessor {
    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().getPackageName().equals("playground")) System.out.println("After init: " + beanName);
        return bean;
    }

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().getPackageName().equals("playground")) System.out.println("Before init: " + beanName);
        return bean;
    }
}
