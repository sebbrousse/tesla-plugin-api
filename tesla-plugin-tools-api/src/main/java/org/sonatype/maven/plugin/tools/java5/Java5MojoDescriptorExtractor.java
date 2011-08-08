package org.sonatype.maven.plugin.tools.java5;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.DefaultPluginToolsRequest;
import org.apache.maven.tools.plugin.PluginToolsRequest;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = MojoDescriptorExtractor.class, hint = "java5" )
public class Java5MojoDescriptorExtractor
    implements MojoDescriptorExtractor
{

    @Requirement
    private MetadataGleaner metadataGleaner;

    @Requirement
    private MetadataProcessor metadataProcessor;

    public List<MojoDescriptor> execute( MavenProject project, PluginDescriptor pluginDescriptor )
        throws ExtractionException, InvalidPluginDescriptorException
    {
        return execute( new DefaultPluginToolsRequest( project, pluginDescriptor ) );
    }

    public List<MojoDescriptor> execute( PluginToolsRequest request )
        throws ExtractionException, InvalidPluginDescriptorException
    {
        MavenProject project = request.getProject();

        File basedir = project.getBasedir();

        MetadataGleaningRequest gleanRequest = new MetadataGleaningRequest();

        File classesDir = resolve( new File( project.getBuild().getOutputDirectory() ), basedir );
        gleanRequest.addClassDirectory( classesDir );

        @SuppressWarnings( "unchecked" )
        List<String> sourceRoots = project.getCompileSourceRoots();
        for ( String sourceRoot : sourceRoots )
        {
            gleanRequest.addSourceDirectory( resolve( new File( sourceRoot ), basedir ) );
        }
        gleanRequest.setSourceEncoding( request.getEncoding() );

        try
        {
            @SuppressWarnings( "unchecked" )
            List<String> classpathElements = project.getCompileClasspathElements();
            classpathElements = classpathElements.subList( 1, classpathElements.size() );
            for ( String classpathElement : classpathElements )
            {
                gleanRequest.addCompileDependency( resolve( new File( classpathElement ), basedir ) );
            }
        }
        catch ( DependencyResolutionRequiredException e )
        {
            throw new ExtractionException( e.getMessage(), e );
        }

        MetadataProcessingRequest procRequest = new MetadataProcessingRequest();
        procRequest.setPluginDescriptor( request.getPluginDescriptor() );

        try
        {
            procRequest.setClasses( metadataGleaner.glean( gleanRequest ) );
        }
        catch ( Exception e )
        {
            throw new ExtractionException( e.getMessage(), e );
        }

        List<MojoDescriptor> mojoDescriptors;
        try
        {
            mojoDescriptors = new ArrayList<MojoDescriptor>( metadataProcessor.process( procRequest ) );
        }
        catch ( Exception e )
        {
            throw new ExtractionException( e.getMessage(), e );
        }

        return mojoDescriptors;
    }

    private File resolve( File file, File basedir )
    {
        if ( !file.isAbsolute() )
        {
            return new File( basedir, file.getPath() ).getAbsoluteFile();
        }
        return file;
    }

}
