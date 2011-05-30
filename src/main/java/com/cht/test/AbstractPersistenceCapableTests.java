package com.cht.test;

import com.cht.test.persistence.CleanInsertTestExecutionListener;
import com.cht.test.persistence.DataSetLocation;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * 資料庫存取相關的抽象測試案例，<strong>所有需要進行資料庫存取的相關測試案例都應該繼承我。</strong>
 * <p>
 * 類似 {@link AbstractTests} ，會自動偵測 Mockito 的 Annotation，進行 Mock 物件的產生及注入；此外，還會依據
 * {@link DataSetLocation} 的設定進行測試資料表內容的建立。
 *
 * @author <a href="mailto:beta@cht.com.tw">黃培棠</a>
 */
@RunWith(MockitoJUnitRunner.class)
@TestExecutionListeners({ CleanInsertTestExecutionListener.class })
public abstract class AbstractPersistenceCapableTests extends
        AbstractTransactionalJUnit4SpringContextTests {

}
