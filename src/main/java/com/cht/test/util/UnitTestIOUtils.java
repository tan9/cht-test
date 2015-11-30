package com.cht.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.util.ResourceUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * 協助測試用的 IO 存取工具。
 *
 * @author <a href="matilto:beta@cht.com.tw">黃培棠</a>
 */
public class UnitTestIOUtils {

    /**
     * 載入測試檔案，測試檔案請跟測試案例放在同一個 Package 下，命名規則為 {@code XxxTest-fileName}。
     * <p>
     * 例如在
     * {@code com.cht.test.util.IOUtilsTest} 裡執行 {@code loadFile("test.xml")}
     * ，則會載入 {@code com/cht/test/util/IOUtilsTest-test.xml}。
     *
     * @param fileName
     *            檔案名稱。
     * @return File 物件。
     * @throws FileNotFoundException
     *             代表指定的檔案不存在。
     */
    public static File loadFile(String fileName) throws FileNotFoundException {
        return loadFile(fileName, 2);
    }

    private static File loadFile(String fileName, int stackTraceIndex) throws FileNotFoundException {
        String className = new Throwable().fillInStackTrace().getStackTrace()[stackTraceIndex]
                .getClassName();
        File file = ResourceUtils.getFile("classpath:" + className.replace(".", "/") + "-"
                + fileName);
        return file;
    }

    /**
     * 將測試檔案以 InputStream 的方式載入，測試檔案請跟測試案例放在同一個 Package 下，命名規則為
     * {@code XxxTest-fileName}。
     * <p>
     * 例如在
     * {@code com.cht.test.util.IOUtilsTest} 裡執行 {@code loadFile("test.xml")}
     * ，則會載入 {@code com/cht/test/util/IOUtilsTest-test.xml}。
     *
     * @param fileName
     *            檔案名稱。
     * @return 該 File 對應的 InputStream 物件。
     * @throws FileNotFoundException
     *             代表指定的檔案不存在。
     */
    public static InputStream loadFileAsStream(String fileName) throws FileNotFoundException {
        File file = loadFile(fileName, 2);
        InputStream inputStream = new FileInputStream(file);
        return inputStream;
    }

    /**
     * 以指定的編碼將測試檔案載入成字串。
     *
     * @param fileName
     *            檔案名稱。
     * @param charset
     *            編碼。
     * @return 字串。
     * @throws IOException
     *             代表檔案存取或轉換時發生錯誤。
     */
    public static String loadFileAsString(String fileName, Charset charset) throws IOException {
        File file = loadFile(fileName, 2);
        String result = Files.toString(file, charset);
        return result;
    }

    /**
     * 使用 UTF-8 編碼將測試檔案載入成字串。
     *
     * @param fileName
     *            檔案名稱。
     * @return 字串。
     * @throws IOException
     *             代表檔案存取或轉換時發生錯誤。
     */
    public static String loadFileAsString(String fileName) throws IOException {
        File file = loadFile(fileName, 2);
        String result = Files.toString(file, Charsets.UTF_8);
        return result;
    }
}
