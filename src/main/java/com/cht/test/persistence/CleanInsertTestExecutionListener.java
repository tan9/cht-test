package com.cht.test.persistence;

import java.util.Calendar;
import java.util.Locale;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.util.StringUtils;

/**
 * 在單元測試前呼叫 DbUnit 進行資料庫資料準備作業的 {@link TestExecutionListener}。
 *
 * @author acogoluegnes
 * @see <a href=
 *      "http://blog.zenika.com/index.php?post/2010/02/05/Testing-SQL-queries-with-Spring-and-DbUnit%2C-part-2"
 *      >Testing SQL queries with Spring and DbUnit, part 2</a>
 */
public class CleanInsertTestExecutionListener implements TestExecutionListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CleanInsertTestExecutionListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        // trying to find the DbUnit dataset
        String dataSetResourcePath = null;

        // first, the annotation on the test class
        DataSetLocation dsLocation = testContext.getTestInstance().getClass()
                .getAnnotation(DataSetLocation.class);
        if (dsLocation != null) {
            // found the annotation
            dataSetResourcePath = dsLocation.value();
            LOGGER.info("annotated test, using data set: {}", dataSetResourcePath);

        } else {
            // no annotation, let's try with the name of the test
            String tempDsRes = testContext.getTestInstance().getClass().getName();
            tempDsRes = StringUtils.replace(tempDsRes, ".", "/");
            tempDsRes = "/" + tempDsRes + "-dataset.xml";
            if (getClass().getResourceAsStream(tempDsRes) != null) {
                LOGGER.info("detected default dataset: {}", tempDsRes);
                dataSetResourcePath = tempDsRes;

            } else {
                LOGGER.info("no default dataset");
            }
        }

        if (dataSetResourcePath != null) {
            Resource dataSetResource = testContext.getApplicationContext().getResource(
                    dataSetResourcePath);
            IDataSet dataSet = new FlatXmlDataSetBuilder().build(dataSetResource.getInputStream());
            ReplacementDataSet replaceDataSet = new ReplacementDataSet(dataSet);
            replaceDataSet.addReplacementObject("[NULL]", null);
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            replaceDataSet.addReplacementObject("[NOW]", cal.getTime());
            IDatabaseConnection dbConn = new DatabaseDataSourceConnection(testContext
                    .getApplicationContext().getBean(DataSource.class));
            DatabaseConfig config = dbConn.getConfig();
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
            DatabaseOperation.CLEAN_INSERT.execute(dbConn, replaceDataSet);

        } else {
            LOGGER.info("{} does not have any data set, no data injection", testContext
                    .getTestInstance().getClass().getName());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
    }
}
