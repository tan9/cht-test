package com.cht.test.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.cht.test.rule.ConcurrentRule;

/**
 * 測試案例同步處理的 Annotation，由 {@link ConcurrentRule} 控制。
 *
 * 用法如下:
 * <pre>
 * public final class ConcurrentTest {
 *
 *     &#064;Rule
 *     public ConcurrentRule concurrentRule = new ConcurrentRule();
 *
 *     &#064;Test
 *     &#064;Concurrent(15)
 *     public void myTestMethod() throws InterruptedException {
 *         System.out.println(&quot;Thread &quot; + Thread.currentThread().getName() + &quot; started !&quot;);
 *         int n = new Random().nextInt(5000);
 *         System.out.println(&quot;Thread &quot; + Thread.currentThread().getName() + &quot; wait &quot; + n + &quot;ms&quot;);
 *         Thread.sleep(n);
 *         System.out.println(&quot;Thread &quot; + Thread.currentThread().getName() + &quot; finished&quot;);
 *     }
 * }
 * </pre>
 *
 * @author <a href="mathieu.carbou@gmail.com">Mathieu Carbou</a>
 * @see <a href=
 *      "http://blog.mycila.com/2009/11/writing-your-own-junit-extensions-using.html"
 *      >Writing your own JUnit extensions using @Rule</a>
 */
@Retention(RUNTIME)
@Target({ METHOD })
public @interface Concurrent {

    int value() default 10;
}
