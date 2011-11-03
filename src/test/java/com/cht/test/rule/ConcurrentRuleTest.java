package com.cht.test.rule;

import java.util.Random;

import org.junit.Rule;
import org.junit.Test;

import com.cht.test.annotation.Concurrent;

public class ConcurrentRuleTest {

    @Rule
    public ConcurrentRule concurrentRule = new ConcurrentRule();

    @Test
    @Concurrent(15)
    public void testThread() throws InterruptedException {
        System.out.println("Thread \"" + Thread.currentThread().getName() + "\" started !");
        int n = new Random().nextInt(500);
        System.out.println("Thread \"" + Thread.currentThread().getName() + "\" wait " + n + "ms");
        Thread.sleep(n);
        System.out.println("Thread \"" + Thread.currentThread().getName() + "\" finished");
    }
}
