package com.cht.test.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author acogoluegnes
 * @see <a href=
 *      "http://blog.zenika.com/index.php?post/2010/02/05/Testing-SQL-queries-with-Spring-and-DbUnit%2C-part-2"
 *      >Testing SQL queries with Spring and DbUnit, part 2</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface DataSetLocation {

    public String value();
}
