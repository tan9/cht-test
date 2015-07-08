package com.cht.test.asserter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 輔助測試 Object 一般函式的程式。
 *
 * @author <a href="mailto:beta@cht.com.tw">黃培棠</a>
 */
public abstract class ObjectAsserter {
    /**
     * 不可能被外面用到的 Class，用來測一般會在 {@link Object#equals(Object)} 裡用的 Class 比較。
     */
    private static class ImpossibleClass {
    }

    private static final Object IMPOSSIBLE_OBJECT = new ImpossibleClass();

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectAsserter.class);

    /**
     * 執行物件裡<strong>直接宣告</strong>的 <code>toString()</code>,
     * <code>hashCode()</code>, <code>equals(Object)</code> 等函式，確定其是否符合 General
     * Contracts。
     *
     * <p>
     * 如果您的大作裡有特別的行為，例如可以印出很漂亮 XML 的 <code>toString()</code>
     * 函式，請<strong>務必獨立進行測試</strong>。
     *
     * @param target
     *            待測物件。
     */
    public static void assertBasicObjectMethods(Object target) {
        assertTargetNotNull(target);

        for (Method method : target.getClass().getDeclaredMethods()) {
            String methodName = method.getName();
            Class<?>[] params = method.getParameterTypes();
            if ("toString".equals(methodName) && params.length == 0) {
                assertToString(target);

            } else if ("hashCode".equals(methodName) && params.length == 0) {
                assertHashCode(target);

            } else if ("equals".equals(methodName) && params.length == 1
                    && params[0].equals(Object.class)) {
                assertEquals(target);
            }
        }
    }

    /**
     * 執行物件裡的 <code>toString()</code> 函式，確定其是否符合 General Contracts。
     *
     * <p>
     * 如果您的大作裡有特別的行為，例如可以印出很漂亮 XML 的 <code>toString()</code>
     * 函式，請<strong>務必獨立進行測試</strong>。
     *
     * @param target
     *            待測物件。
     * @see java.lang.Object#toString()
     */
    public static void assertToString(Object target) {
        assertTargetNotNull(target);

        String objectAsString = target.toString();
        assertTargetNotNull(objectAsString); // 這並不是 contract，但是如果用這種方法真的很奇怪

        String className = target.getClass().getName();
        if (!objectAsString.startsWith(className + "@")) {
            LOGGER.debug(String.format(
                    "The toString() method of %s is not directly inherited from Object.toString(),"
                            + " this is a kind remind to inform you to test it separately.",
                    className));
        }
    }

    /**
     * 執行物件裡的 <code>hashCode()</code> 函式，確定其是否符合 General Contracts。
     *
     * @param target
     *            待測物件。
     * @see java.lang.Object#hashCode()
     */
    public static void assertHashCode(Object target) {
        assertTargetNotNull(target);

        // assert hashCode() general contract #1 (partial)
        assertTrue(target.hashCode() == target.hashCode());

        // assert hashCode() general contract #2
        Object newInstance = createNewInstance(target.getClass());
        if (newInstance != null) {
            if (target.equals(newInstance)) {
                assertTrue(target.hashCode() == newInstance.hashCode());

            } else {
                assertFalse(target.hashCode() == newInstance.hashCode());
            }
        }
    }

    /**
     * 執行物件裡的 <code>equals()</code> 函式，確定其是否符合 General Contracts。
     *
     * @param target
     *            待測物件。
     * @see java.lang.Object#equals(Object)
     */
    public static void assertEquals(Object target) {
        assertTargetNotNull(target);

        assertFalse(target.equals(IMPOSSIBLE_OBJECT));

        // assert equals() general contract #1
        assertTrue(target.equals(target));

        // assert equals() general contract #5
        assertFalse(target.equals(null));

        Object newInstance = createNewInstance(target.getClass());
        if (newInstance != null) {
            // assert equals() general contract #2
            if (target.equals(newInstance)) {
                assertTrue(newInstance.equals(target));

            } else {
                assertFalse(newInstance.equals(target));
            }

            // 衝 coverage 吧！
            for (Field field : target.getClass().getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers())
                        || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                try {
                    BeanUtils.copyProperties(target, newInstance);

                    Object value = ReflectionUtils.getField(field, target);
                    Class<?> type = field.getType();
                    Object generatedValue = null;
                    if (int.class.equals(type) || Integer.class.equals(type)) {
                        generatedValue = Integer.parseInt(value.toString()) + 1;

                    } else if (String.class.equals(type)) {
                        generatedValue = value + "~lala";

                    } else if (java.util.Date.class.equals(type)
                            || java.sql.Date.class.equals(type)) {
                        generatedValue = new java.sql.Date(System.currentTimeMillis() + 7788);
                    }

                    if (generatedValue != null) {
                        ReflectionUtils.setField(field, newInstance, generatedValue);
                        assertFalse(
                                String.format(
                                        "Test equals() on property %s, original value: %s, generated value: %s",
                                        field.getName(), value, generatedValue),
                                target.equals(newInstance));
                    }

                } catch (Exception e) {
                    LOGGER.debug(String.format(
                            "Failed to set value of field \"%s\" for equals() testing.",
                            field.getName()), e);
                }
            }
        }
    }

    // TODO public static void assertEquals(Object target, List<String>
    // properties) {}

    static void assertTargetNotNull(Object target) {
        if (target == null) {
            fail("Object \"target\" is required, it must not be null.");
        }
    }

    static <T> T createNewInstance(Class<T> clazz) {
        Constructor<T> defaultConstructor = ClassUtils.getConstructorIfAvailable(clazz);

        if (defaultConstructor != null) {
            try {
                return defaultConstructor.newInstance();

            } catch (Exception e) {
                LOGGER.info(
                        String.format("Failed to invoke the default Constructor on %s.",
                                clazz.getName()), e);
            }
        }
        return null;
    }
}
