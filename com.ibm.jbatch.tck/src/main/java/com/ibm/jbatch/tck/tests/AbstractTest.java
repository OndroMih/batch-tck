package com.ibm.jbatch.tck.tests;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.*;
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
                .as(WebArchive.class);
        
        for (MavenResolvedArtifact artifact : resolvedArtifacts) {
            String groupId = artifact.getCoordinate().getGroupId();
            String artifactId = artifact.getCoordinate().getArtifactId();
            if (groupId.startsWith("org.jboss.shrinkwrap")
                    || groupId.startsWith("org.codehaus.plexus")
                    || groupId.startsWith("org.apache.maven")) {
                continue;
            }
            if ("jar".equals(artifact.getExtension())) {
                archive = archive.addAsLibrary(artifact.asFile());
            }
        }
        return archive;
    }

}
