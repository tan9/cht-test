package com.cht.test.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * <p>
 * 沒錯，就是物件強盜，協助取得物件中的 field，或是執行物件的 method，而不管該成員的 access modifier 是不是
 * <code>public</code> 的。
 * <p>
 * <b>建議只在開發測試情況下使用，現場環境可能因為安全性的原則而不能修改 visibility。</b>
 * </p>
 * <p>
 * 雖然我很想把他取名為 ObjectRapper，但是理智還是讓我沒做出這件事。
 * </p>
 * <p>
 * 本類別大量使用 <a href="http://download.oracle.com/javase/tutorial/reflect/index.html">
 * Reflection</a> 機制進行物件操作，有興趣的話可以自行研究。
 *
 * @author <a href="matilto:beta@cht.com.tw">黃培棠</a>
 */
public class ObjectRobber {

    /**
     * 由 <code>klass</code> 這個 Class 物件中取得其 <code>fieldName</code>
     * 類別欄位(靜態欄位)的數值。
     * <p>
     * 如果要取得實體欄位的內容，請改用 {@link #get(Object, String)} 方法。
     *
     * @see java.lang.reflect.Field#get(Object)
     * @param <T>
     *            取得數值的型態。
     * @param klass
     *            包含取值對象的類別物件。
     * @param fieldName
     *            欄位名稱。
     * @return 該欄位的內容，如果是 primitive 數值，則會回傳其 Wrapper 類別。
     * @throws SecurityException
     *             表示無法完成本項要求。
     * @throws NoSuchFieldException
     *             表示欄位名稱錯誤。
     * @throws IllegalAccessException
     *             表示該欄位無法存取。
     * @throws NullPointerException
     *             表示丟入的參數錯誤，均不可為 <code>null</code>; 或是該欄位是實體變數，請改用
     *             {@link #get(Object, String)} 取值。
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<?> klass, String fieldName) throws SecurityException,
            NoSuchFieldException, IllegalAccessException {
        return (T) doFieldOperation(klass, null, fieldName, false, null);
    }

    /**
     * 由 <code>klass</code> 這個 Class 物件中取得其 <code>fieldName</code> 欄位的數值。
     * <p>
     * 如果要取得類別欄位的內容，請改用 {@link #get(Class, String)} 方法。
     *
     * @see java.lang.reflect.Field#get(Object)
     * @param <T>
     *            取得數值的型態。
     * @param object
     *            包含取值對象的實體物件。
     * @param fieldName
     *            欄位名稱，支援巢狀用法，例如 directProperty.subProperty。
     * @return 該欄位的內容，如果是 primitive 數值，則會回傳其 Wrapper 類別。
     * @throws SecurityException
     *             表示無法完成本項要求。
     * @throws NoSuchFieldException
     *             表示欄位名稱錯誤。
     * @throws IllegalAccessException
     *             表示該欄位無法存取。
     * @throws IllegalArgumentException
     *             表示丟入的參數錯誤，均不可為 <code>null</code>。
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object object, String fieldName) throws SecurityException,
            NoSuchFieldException, IllegalAccessException {
        for (String field : fieldName.split("\\.")) {
            object = getImpl(object, field);
        }

        return (T) object;
    }

    private static Object getImpl(Object object, String fieldName) throws SecurityException,
            NoSuchFieldException, IllegalAccessException {
        // 自動由 object 取得其 Class，所以一定要指定值
        if (object == null) {
            throw new IllegalArgumentException("\"object\" must be specified."
                    + " If you want to get value of a static memeber,"
                    + " you should use get(Class, String) instead.");
        }

        return doFieldOperation(object.getClass(), object, fieldName, false, null);
    }

    /**
     * 將 <code>klass</code> 這個類別中 <code>fieldName</code> 類別欄位(靜態欄位)的數值設為
     * <code>value</code>。
     * <p>
     * 如果欄位是 primitive 型別，會嘗試由 value 物件自動進行 un-boxing。
     *
     * @see java.lang.reflect.Field#set(Object, Object)
     * @param klass
     *            包含取值對象的類別物件。
     * @param fieldName
     *            欄位名稱。
     * @param value
     *            要指定的數值。
     * @throws SecurityException
     *             表示無法完成本項要求。
     * @throws NoSuchFieldException
     *             表示欄位名稱錯誤。
     * @throws IllegalAccessException
     *             代表無存取權限。
     * @throws IllegalArgumentException
     *             代表丟入的參數錯誤。
     */
    public static void set(Class<?> klass, String fieldName, Object value)
            throws SecurityException, NoSuchFieldException, IllegalAccessException {
        doFieldOperation(klass, null, fieldName, true, value);
    }

    /**
     * 將 <code>object</code> 這個物件中 <code>fieldName</code> 欄位的數值設為
     * <code>value</code>。
     * <p>
     * 如果欄位是 primitive 型別，會嘗試由 value 物件自動進行 un-boxing。
     * <p>
     * 如果要設定類別欄位的內容，請改用 {@link #set(Class, String, Object)} 方法。
     *
     * @see java.lang.reflect.Field#set(Object, Object)
     * @param object
     *            包含設值對象的實體物件。
     * @param fieldName
     *            欄位名稱。
     * @param value
     *            要指定的數值。
     * @throws SecurityException
     *             表示無法完成本項要求。
     * @throws NoSuchFieldException
     *             表示欄位名稱錯誤。
     * @throws IllegalAccessException
     *             代表無存取權限。
     * @throws IllegalArgumentException
     *             表示丟入的參數錯誤，均不可為 <code>null</code>。
     */
    public static void set(Object object, String fieldName, Object value) throws SecurityException,
            NoSuchFieldException, IllegalAccessException {
        // 自動由 object 取得其 Class，所以一定要指定值
        if (object == null) {
            throw new IllegalArgumentException("\"object\" must be specified."
                    + " If you want to set value of a static memeber,"
                    + " you should use set(Class, String, Object) instead.");
        }
        doFieldOperation(object.getClass(), object, fieldName, true, value);
    }

    /**
     * 將物件中有 {@link Autowired} 或是 {@link Resource} 標註，且資料型態與指定值相符的欄位填入物件。
     *
     * @param object
     *            包含設值對象的實體物件。
     * @param valueToBeInjected
     *            要指定的數值。
     * @throws SecurityException
     *             表示無法完成本項要求。
     * @throws IllegalAccessException
     *             代表無存取權限。
     */
    public static void manualWire(Object object, Object valueToBeInjected)
            throws SecurityException, IllegalAccessException {
        Assert.notNull(object);
        Assert.notNull(valueToBeInjected);

        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ((field.getAnnotation(Autowired.class) != null || field
                    .getAnnotation(Resource.class) != null)
                    && field.getType().isAssignableFrom(valueToBeInjected.getClass())) {

                try {
                    set(object, field.getName(), valueToBeInjected);

                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Cannot set value.", e);
                }
            }
        }
    }

    private static Object doFieldOperation(Class<?> klass, Object object, String fieldName,
            boolean set, Object value) throws SecurityException, NoSuchFieldException,
            IllegalAccessException {
        // 取得欄位，如果取不到就往父類別找
        Field field = null;
        while (field == null) {
            try {
                field = klass.getDeclaredField(fieldName);

            } catch (NoSuchFieldException e) {
                if (klass.getSuperclass() != null) {
                    klass = klass.getSuperclass();

                } else {
                    throw e;
                }
            }
        }

        final boolean isAccessable = field.isAccessible();
        if (isAccessable == false) {
            field.setAccessible(true);
        }

        Object returnValue = null;
        if (set) {
            field.set(object, value);

        } else {
            returnValue = field.get(object);
        }

        if (isAccessable == false) {
            field.setAccessible(false);
        }

        return returnValue;
    }

    /**
     * 呼叫 <code>object</code> 物件的 <code>methodName</code> ，並依序代入所需的參數。
     *
     * @param object
     *            目標物件。
     * @param methodName
     *            函式名稱。
     * @param params
     *            執行該函式的參數。
     * @param <T>
     *            回傳物件型別。
     * @return 執行結果，如果該函式沒有回傳值，則回傳 <code>null</code>。
     * @throws SecurityException
     *             表示無法完成本項要求。
     * @throws IllegalAccessException
     *             代表無存取權限。
     * @throws IllegalArgumentException
     *             表示丟入的參數錯誤，均不可為 <code>null</code>。
     * @throws InvocationTargetException
     *             表示呼叫底層物件時發生錯錯誤。
     */
    public static <T> T invoke(Object object, String methodName, Object... params)
            throws SecurityException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        // 必須要指定物件，否則無法取得對應的內容
        if (object == null) {
            throw new IllegalArgumentException("\"object\" must be specified."
                    + " If you want to invoke a static method, you should use"
                    + " invoke(Class, Object, String, Class[], Object[]),"
                    + " and omit the \"Object\" parameter instead.");
        }

        // 如果沒有指定參數內容，那表示不用參數
        if (params == null) {
            return (T) invoke(object.getClass(), object, methodName, new Class[0], new Object[0]);
        }

        // 不然就一一取得 params 的類別，然後再 invoke
        Class<?>[] args = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            if (params[i] != null) {
                args[i] = params[i].getClass();
            }
        }

        // 由實際的 method 中取得相容的變數型態
        Method[] methods = object.getClass().getDeclaredMethods();
        Set<Integer> possibleIndexSet = new HashSet<Integer>();
        for (int i = 0; i < methods.length; i++) {
            Class<?>[] possibleArgs = methods[i].getParameterTypes();
            if (Arrays.equals(possibleArgs, args)) {
                return invoke(object.getClass(), object, methodName, args, params);

            } else if (isCompatiable(possibleArgs, args)) {
                possibleIndexSet.add(Integer.valueOf(i));
            }
        }

        if (possibleIndexSet.size() == 1) {
            Integer index = possibleIndexSet.iterator().next();
            args = methods[index.intValue()].getParameterTypes();
            return (T) invoke(object.getClass(), object, methodName, args, params);

        } else if (possibleIndexSet.isEmpty()) {
            throw new NoSuchMethodException(object.getClass().getName() + "." + methodName + "("
                    + Arrays.asList(args) + ")");

        } else {
            throw new IllegalArgumentException("\"params\""
                    + " cannot be determined as a possible arguments array."
                    + " You are suggest to use "
                    + "invoke(Class, Object, String, Class[], Object[])"
                    + " to describe the desired method explicitly.");

        }
    }

    private static boolean isCompatiable(Class<?>[] possibleArgs, Object[] params) {
        // TODO 判斷兩個 Class[] 是否相容
        return false;
    }

    /**
     * Reflection 中 method.invoke 的包裝，不過你必須明確的指定所有的參數(除了那些可為 <code>null</code>
     * 的以外)。
     *
     * @param klass
     *            類別物件。如果其值為 <code>null</code>，那會自動由 object 取得，如果 object 也為
     *            <code>null</code>，那你就爽了。
     * @param object
     *            實體物件，必須為 klass 或其子類別的實體; 不過如果要呼叫的方法為類別方法(靜態方法)，其值可為
     *            <code>null</code>。
     * @param methodName
     *            要呼叫的方法名稱。
     * @param args
     *            要呼叫的方法的引數的 Class 陣列; 如果沒有引數，可為 <code>null</code>。
     * @param params
     *            要呼叫的方法所需代入的參數; 如果不需要參數，可為 <code>null</code>。
     * @return 初始化後的物件。
     * @throws SecurityException
     *             表示無法完成本項要求。
     * @throws NoSuchFieldException
     *             表示欄位名稱錯誤。
     * @throws IllegalAccessException
     *             代表無存取權限。
     * @throws IllegalArgumentException
     *             表示丟入的參數錯誤，均不可為 <code>null</code>。
     * @throws InvocationTargetException
     *             表示呼叫底層物件時發生錯錯誤。
     */
    private static <T> T invoke(Class<?> klass, Object object, String methodName, Class<?>[] args,
            Object[] params) throws SecurityException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        // 如果 klass 沒指定，只給 object，那就好心的幫忙懶人設定吧
        if (klass == null || object != null) {
            klass = object.getClass();
        }

        Method method = null;
        while (method == null) {
            try {
                method = klass.getDeclaredMethod(methodName, args);

            } catch (NoSuchMethodException e) {
                if (klass.getSuperclass() != null) {
                    klass = klass.getSuperclass();

                } else {
                    throw e;
                }
            }
        }

        final boolean isAccessable = method.isAccessible();
        if (isAccessable == false) {
            method.setAccessible(true);
        }

        Object value = method.invoke(object, params);

        if (isAccessable == false) {
            method.setAccessible(false);
        }

        @SuppressWarnings("unchecked")
        T result = (T) value;
        return result;
    }

    /**
     * 輸入初始化參數，取得對應的 constructor，並產生物件實體。private 的 constructor 也可以使用。
     *
     * @param klass
     *            要建立的物件類別。
     * @param initArgs
     *            初始化參數。
     * @param <T>
     *            物件型別。
     * @return 初始化後的物件。
     * @throws NoSuchMethodException
     *             表示沒有可用的建構式。
     * @throws InstantiationException
     *             表示初始化過程發生錯誤。
     * @throws IllegalAccessException
     *             表示無存取權限。
     * @throws InvocationTargetException
     *             表示呼叫過程發生錯誤。
     */
    public static <T> T genInstance(Class<?> klass, Object... initArgs)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        Class<?>[] initArgClasses = new Class<?>[initArgs.length];
        for (int i = 0; i < initArgs.length; ++i)
            initArgClasses[i] = initArgs[i].getClass();
        Constructor<?> constructor = klass.getDeclaredConstructor(initArgClasses);
        if (!constructor.isAccessible())
            constructor.setAccessible(true);

        @SuppressWarnings("unchecked")
        T result = (T) constructor.newInstance(initArgs);
        return result;
    }
}
