package com.cht.test.persistence;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

/**
 * 自動將 {@link Entity} 及 {@link Embeddable} 加入到
 * {@link MutablePersistenceUnitInfo} 中。
 *
 * @see <a
 *      href="http://www.bewareofthebear.com/java/testing-jpa-with-junit-spring-and-maven/"
 *      >Testing JPA with JUnit, Spring and Maven</a>
 */
public class EnlistEntityPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EnlistEntityPersistenceUnitPostProcessor.class);

    /**
     * {@inheritDoc}
     */
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        URL persistenceUnitRootUrl = pui.getPersistenceUnitRootUrl();

        String protocol = persistenceUnitRootUrl.getProtocol();
        String host = persistenceUnitRootUrl.getHost();
        int port = persistenceUnitRootUrl.getPort();
        String file = persistenceUnitRootUrl.getFile();
        String newFile;
        if (file.lastIndexOf("test-classes") != -1) {
            newFile = file.substring(0, file.lastIndexOf("test-classes")) + "classes/";

        } else {
            return;
        }

        URL url = null;
        try {
            url = new URL(protocol, host, port, newFile);

        } catch (MalformedURLException e) {
            LOGGER.warn("Malformed url: " + url, e);
            return;
        }

        AnnotationDB annotationDB = new AnnotationDB();
        try {
            annotationDB.scanArchives(url);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Unexpected IOException while scanning archive for annotation.", e);
        }

        enlistClasses(pui, annotationDB, Entity.class.getName());
        enlistClasses(pui, annotationDB, Embeddable.class.getName());
        enlistClasses(pui, annotationDB, MappedSuperclass.class.getName());
    }

    private void enlistClasses(MutablePersistenceUnitInfo pui, AnnotationDB annotationDB,
            String className) {
        if (annotationDB.getAnnotationIndex().containsKey(className)) {
            for (String entity : annotationDB.getAnnotationIndex().get(className)) {
                pui.addManagedClassName(entity);
                LOGGER.debug("Adding {}: {}", className, entity);
            }
        }
    }
}
