package com.cht.test.asserter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Constructor;

import javassist.Modifier;

import org.springframework.util.ClassUtils;

/**
 * 輔助測試 JavaBean 是否符合<a href="http://goo.gl/j7oD7">JavaBean&trade;
 * Specification</a>。
 *
 * @author <a href="mailto:beta@cht.com.tw">黃培棠</a>
 */
public abstract class JavaBeanAsserter {

    /**
     * 測試一般 JavaBean 的 Contract。
     *
     * <p>
     * 主要包含 JavaBean 是否有 Public 的 Default Constructor、是否有 Getter 與 Setter、是否為
     * Serializable，也會順道測試 Object Method。
     *
     * <p>
     * 有關 JavaBean 的詳細規範，請參考 <a href="http://goo.gl/j7oD7">JavaBean&trade;
     * Specification</a>。
     *
     * @param target
     *            待測 JavaBean 物件。
     * @see ObjectAsserter#assertBasicObjectMethods(Object)
     * @see PropertyAsserter#assertBasicGetterSetterBehavior(Object)
     */
    public static void assertJavaBean(Object target) {
        ObjectAsserter.assertTargetNotNull(target);

        assertHasDefaultConstructor(target);
        assertIsSerializable(target);

        ObjectAsserter.assertBasicObjectMethods(target);
        PropertyAsserter.assertBasicGetterSetterBehavior(target);
    }

    /**
     * 確認該 JavaBean 有 public 的 Default Constructor 。
     *
     * @param target
     *            待測 JavaBean 物件。
     */
    public static void assertHasDefaultConstructor(Object target) {
        Constructor<?> constructor = ClassUtils.getConstructorIfAvailable(target.getClass());

        assertNotNull(String.format("JavaBean %s must have default constructor.", target.getClass()
                .getName()), constructor);

        assertTrue(String.format(
                "The default constructor visibility of JavaBean %s must be \"public\".", target
                        .getClass().getName()), Modifier.isPublic(constructor.getModifiers()));
    }

    /**
     * 確認該 JavaBean 實作 {@link Serializable} 或是 {@link Externalizable}。
     *
     * @param target
     *            待測 JavaBean 物件。
     */
    public static void assertIsSerializable(Object target) {
        String message = String
                .format("According to \"JavaBeans API Specification 1.01-A\" page 23:"
                        + " All beans must support either \"Serialization\" or \"Externalization\".");
        assertTrue(message, Serializable.class.isAssignableFrom(target.getClass())
                || Externalizable.class.isAssignableFrom(target.getClass()));
    }
}
