package org.sonatype.maven.plugin;

import java.io.File;

import org.apache.maven.project.MavenProject;

public interface Hello
{
    void generate( MavenProject project, File outputDirectory )
        throws Exception;
}
