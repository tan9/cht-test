package com.cht.test.rule;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.cht.test.annotation.Concurrent;

/**
 * 處理 {@link Concurrent} 標註的 {@code TestRule}。
 *
 * 用法如下:
 *
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
 * @author <a href="mailto:beta@cht.com.tw">黃培棠</a>
 * @see <a href=
 *      "http://blog.mycila.com/2009/11/writing-your-own-junit-extensions-using.html"
 *      >Writing your own JUnit extensions using @Rule</a>
 */
public final class ConcurrentRule implements TestRule, MethodRule {

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Concurrent concurrent = description.getAnnotation(Concurrent.class);
                if (concurrent == null) {
                    base.evaluate();

                } else {
                    // create an executor which simply spawns threads to execute
                    // runnables
                    Executor executor = new Executor() {
                        private final String name = description.getMethodName();
                        private int count = 0;

                        @Override
                        public void execute(Runnable command) {
                            Thread thread = new Thread(command, name + "-thread-" + count++);
                            thread.start();
                        }
                    };
                    // create a completion service to get jobs in the order they
                    // finish, to be able to cancel remaining jobs as fast as
                    // possible if an exception occurs
                    CompletionService<Void> completionService;
                    completionService = new ExecutorCompletionService<Void>(executor);
                    // latch used to pause all threads and start all of them
                    // (nearly) at the same time
                    final CountDownLatch go = new CountDownLatch(1);
                    // create the tasks
                    for (int i = 0; i < concurrent.value(); i++) {
                        completionService.submit(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    go.await();
                                    base.evaluate();

                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();

                                } catch (Exception exception) {
                                    throw exception;

                                } catch (Error error) {
                                    throw error;

                                } catch (Throwable throwable) {
                                    // case of exceptions directly subclassing
                                    // Throwable (should not occur - bad
                                    // programming)
                                    RuntimeException e = new RuntimeException(throwable // NOPMD
                                            .getMessage(), throwable);
                                    throw e;
                                }
                                return null;
                            }
                        });
                    }
                    go.countDown();
                    Throwable throwable = null;
                    for (int i = 0; i < concurrent.value(); i++) {
                        try {
                            completionService.take().get();

                        } catch (ExecutionException e) {
                            // only keep the first exception, but wait for all
                            // threads to finish
                            if (throwable == null) {
                                throwable = e.getCause();
                            }
                        }
                    }
                    if (throwable != null) {
                        throw throwable;
                    }
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        Description description = Description.createTestDescription(method.getMethod()
                .getDeclaringClass(), method.getName(), method.getAnnotations());
        return apply(base, description);
    }
}
