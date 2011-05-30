package com.cht.test.asserter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 輔助測試 Throwable 物件基本功能的程式。
 *
 * @author <a href="mailto:beta@cht.com.tw">黃培棠</a>
 */
public abstract class ThrowableAsserter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrowableAsserter.class);

    /**
     * 處理特定 Type 的 Handler。
     *
     * @author <a href="mailto:beta@cht.com.tw">黃培棠</a>
     * @param <T>
     */
    public static abstract class TypeHandler<T> {
        @SuppressWarnings("hiding")
        abstract <T> boolean accepts(Class<? extends T> type);

        @SuppressWarnings("hiding")
        abstract <T> T mock(Class<? extends T> type);
    }

    /**
     * 輔助測試 Throwable 的 Construcotr 們...
     * <p>
     * 其實好像也可以拿來測試一般 Class 的 Constructor。
     *
     * @param clazz
     *            待測的 Throwable 類別。
     */
    public static void assertThrowable(Class<? extends Throwable> clazz) {
        // 試著找出所有的 Constructor。
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            testConstructor(constructor);
        }
    }

    private static void testConstructor(Constructor<?> constructor) {
        List<Object> parameters = new ArrayList<Object>();
        for (Class<?> type : constructor.getParameterTypes()) {
            for (TypeHandler<?> typeHandler : getTypeHandlers()) {
                if (typeHandler.accepts(type)) {
                    try {
                        parameters.add(typeHandler.mock(type));

                    } catch (Exception e) {
                        LOGGER.info(String.format("Failed to creaete instance of %1$s.",
                                type.getName()), e);
                    }
                    break;
                }
            }
        }

        if (parameters.size() == constructor.getParameterTypes().length) {
            try {
                constructor.newInstance(parameters.toArray(new Object[parameters.size()]));

            } catch (Exception e) {
                LOGGER.info(String.format("Failed to invoke constructor of %1$s.", constructor
                        .getDeclaringClass().getName()), e);
            }
        } else {
            LOGGER.debug("Not all parameters has been resolved.");
        }
    }

    private static List<TypeHandler<?>> getTypeHandlers() {
        List<TypeHandler<?>> typeHandlers = new ArrayList<TypeHandler<?>>();

        // 註冊 String Handler。
        typeHandlers.add(new TypeHandler<String>() {
            @Override
            <T> boolean accepts(Class<? extends T> type) {
                return String.class.isAssignableFrom(type);
            }

            @SuppressWarnings("unchecked")
            @Override
            <T> T mock(Class<? extends T> type) {
                return (T) "STRING_STRING";
            }
        });

        // 註冊 Throwable Handler。
        typeHandlers.add(new TypeHandler<Throwable>() {
            @Override
            <T> boolean accepts(Class<? extends T> type) {
                return Throwable.class.isAssignableFrom(type);
            }

            @Override
            <T> T mock(Class<? extends T> type) {
                try {
                    return type.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create mock instance.", e);
                }
            }
        });

        typeHandlers.add(new TypeHandler<Object>() {

            @Override
            <T> boolean accepts(Class<? extends T> type) {
                return type.isArray();
            }

            @SuppressWarnings("unchecked")
            @Override
            <T> T mock(Class<? extends T> type) {
                Class<?> componentType = type.getComponentType();
                return (T) Array.newInstance(componentType, 0);
            }
        });

        // 註冊 General 的 Object Handler。 (as fallback)
        typeHandlers.add(new TypeHandler<Object>() {
            @Override
            <T> boolean accepts(Class<? extends T> type) {
                return Object.class.isAssignableFrom(type);
            }

            @Override
            <T> T mock(Class<? extends T> type) {
                try {
                    return type.newInstance();

                } catch (Exception e) {
                    LOGGER.info("Cannot create mock instance by invoking its newInstance(),"
                            + " try to mock with Mockito.");

                    return Mockito.mock(type);
                }
            }
        });

        return typeHandlers;
    }
}
