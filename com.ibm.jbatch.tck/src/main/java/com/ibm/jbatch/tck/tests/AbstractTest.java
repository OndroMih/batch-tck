package com.ibm.jbatch.tck.tests;

import java.io.File;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.*;

public class AbstractTest extends Arquillian {

    @Deployment
    public static WebArchive createTestArchive() {
        MavenResolvedArtifact[] resolvedArtifacts = Maven.resolver().loadPomFromFile("pom.xml")
                .importDependencies(ScopeType.COMPILE, ScopeType.TEST)
                .resolve().withTransitivity().asResolvedArtifact();
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, "jbatch-test-package-all.war")
                .addPackages(true, "com.ibm.jbatch.tck")
                .as(WebArchive.class);
        for (MavenResolvedArtifact artifact : resolvedArtifacts) {
            String groupId = artifact.getCoordinate().getGroupId();
            if (groupId.startsWith("org.jboss.shrinkwrap")
                    || groupId.startsWith("org.codehaus.plexus")
                    || groupId.startsWith("org.apache.maven")) {
                continue;
            }
            if ("jar".equals(artifact.getExtension())) {
                archive.addAsLibrary(artifact.asFile());
            }
        }
        archive.as(ZipExporter.class).exportTo(new File("/tmp/arquillian.war"), true);
        return archive;
    }

}
