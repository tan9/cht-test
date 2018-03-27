package com.cht.test;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * 一般用途抽象測試案例。會自動偵測 Mockito 的 Annotation 並進行 Mock 物件的產生及注入。
 * <p>
 * 請先在貴系統測試用的 Source Folder 裡新增一個抽象測試類別，並且繼承我:
 *
 * <pre>
 * &#064;ContextConfiguration
 * public abstract class Abstract<i>&lt;系統代碼&gt;</i>Tests extends AbstractTests {
 *
 * }
 * </pre>
 *
 * 再將測試用的 Spring 設定檔放在相同 classpath 下，並取名
 * <code>Abstract<i>&lt;系統代碼&gt;</i>Tests-context.xml</code>，之後所有的測試案例都只要繼承該類別就可以了。
 * <p>
 * 如果是需要使用到 dbUnit 及 h2 database 進行的資料庫存取測試，請使用 {@link AbstractPersistenceCapableTests}。
 *
 *
 * @author <a href="mailto:beta@cht.com.tw">黃培棠</a>
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractTests extends AbstractJUnit4SpringContextTests {

}
