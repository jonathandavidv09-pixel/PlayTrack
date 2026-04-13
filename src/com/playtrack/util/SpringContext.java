package com.playtrack.util;

import org.springframework.context.ApplicationContext;

// Utility component: shared helpers for the system layer.
public class SpringContext {
    private static ApplicationContext context;

    // setContext.
    public static void setContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}