package org.sonatype.maven.plugin;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.project.MavenProject;

@Named
@Singleton
public class DefaultHello
    implements Hello
{

    public void generate( MavenProject project, File outputDirectory )
        throws Exception
    {
        File outputFile = new File( outputDirectory, project.getArtifactId() + ".txt" );
        outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();
    }

}
