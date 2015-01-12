package com.cht.test.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.HashMap;

import org.junit.Test;

public class ObjectRobberTest {

    @Test
    public void testGetClassString() throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException {
        Object result = ObjectRobber.get(Calendar.class, "COMPUTED");

        assertEquals(1, ((Integer) result).intValue());
    }

    @Test
    public void testGetObjectString() throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        Object result = ObjectRobber.get(map, "threshold");

        assertEquals(16, ((Integer) result).intValue());
    }

    @Test
    public void testSetClassStringObject() throws SecurityException, NoSuchFieldException,
            IllegalAccessException {
        ObjectRobber.set(DummyClass.class, "CLASS_VALUE", Integer.valueOf(32));

        assertEquals(32, DummyClass.getClassValue());
    }

    @Test
    public void testSetObjectStringObject() throws SecurityException, NoSuchFieldException,
            IllegalAccessException {
        DummyClass obj = new DummyClass();
        ObjectRobber.set(obj, "OBJECT_VALUE", Integer.valueOf(32));

        assertEquals(32, obj.getObjectValue());
    }

    @Test
    public void testGenInstance() throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        DummyClass dummyClass = (DummyClass) ObjectRobber.genInstance(DummyClass.class);
        assertEquals(0, dummyClass.getObjectValue());
        dummyClass = (DummyClass) ObjectRobber.genInstance(DummyClass.class, Integer.valueOf(10));
        assertEquals(10, dummyClass.getObjectValue());
    }

    @Test
    public void testInvoke1() throws SecurityException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        DummyClass dummyClass = new DummyClass();
        Integer integer = (Integer) ObjectRobber.invoke(dummyClass, "getObjectValue");
        assertEquals(dummyClass.getObjectValue(), integer.intValue());
    }

    @Test
    public void testInvoke2() throws SecurityException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        DummyClass dummyClass = new DummyClass();
        Integer integer = (Integer) ObjectRobber.invoke(dummyClass, "setObjectValue", 10);
        assertEquals(dummyClass.getObjectValue(), integer.intValue());
    }

    @Test
    public void testInvoke3() throws SecurityException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        DummyClass dummyClass = new DummyClass();
        Integer integer = (Integer) ObjectRobber.invoke(dummyClass, "getClassValue");
        assertEquals(DummyClass.getClassValue(), integer.intValue());
    }
}

class DummyClass {
    private static int CLASS_VALUE = 0;
    private int OBJECT_VALUE = 0;

    public DummyClass() {
    }

    @SuppressWarnings("unused")
    private DummyClass(Integer i) {
        OBJECT_VALUE = i;
    }

    public static int getClassValue() {
        return CLASS_VALUE;
    }

    public int getObjectValue() {
        return OBJECT_VALUE;
    }

    @SuppressWarnings("unused")
    private int setObjectValue(Integer i) {
        OBJECT_VALUE = i;
        return OBJECT_VALUE;
    }
}
