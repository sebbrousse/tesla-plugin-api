package org.sonatype.maven.plugin;

import java.io.File;

import javax.inject.Inject;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Says "Hello world!".
 * 
 * @since 1.0
 */
@Goal( "hello" )
@DefaultPhase( LifecyclePhase.VALIDATE )
@DefaultGoal
@ThreadSafe
@GeneratedResourceRoot( directory = "${outputDirectory}", includes = "**/*.properties,**/*.xml", filtering = "true" )
public class HelloWorldMojo
    extends SisuMavenMojo
{

    @Inject
    private Hello hello;

    @Configuration
    @DefaultsTo( "${project}" )
    @NonEditable
    private MavenProject project;

    /**
     * The output directory.
     */
    @Configuration
    @Property( "hello.outputDirectory" )
    @DefaultsTo( "${project.build.directory}/hello" )
    @Alias( "destinationDirectory" )
    private File outputDirectory;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            hello.generate( project, outputDirectory );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

}
