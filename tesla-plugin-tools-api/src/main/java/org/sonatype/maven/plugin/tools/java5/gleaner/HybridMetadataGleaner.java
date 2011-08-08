package org.sonatype.maven.plugin.tools.java5.gleaner;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.objectweb.asm.ClassReader;
import org.sonatype.maven.plugin.Alias;
import org.sonatype.maven.plugin.Configuration;
import org.sonatype.maven.plugin.DefaultsTo;
import org.sonatype.maven.plugin.Goal;
import org.sonatype.maven.plugin.NonEditable;
import org.sonatype.maven.plugin.Property;
import org.sonatype.maven.plugin.Required;
import org.sonatype.maven.plugin.RequiresDependencyResolution;
import org.sonatype.maven.plugin.RequiresDirectInvocation;
import org.sonatype.maven.plugin.RequiresOnline;
import org.sonatype.maven.plugin.RequiresProject;
import org.sonatype.maven.plugin.tools.java5.MetadataGleaner;
import org.sonatype.maven.plugin.tools.java5.MetadataGleaningRequest;
import org.sonatype.maven.plugin.tools.java5.model.JAnnotatedElement;
import org.sonatype.maven.plugin.tools.java5.model.JAnnotation;
import org.sonatype.maven.plugin.tools.java5.model.JClass;
import org.sonatype.maven.plugin.tools.java5.model.JField;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;

@Component( role = MetadataGleaner.class, hint = "hybrid" )
public class HybridMetadataGleaner
    implements MetadataGleaner
{

    public Collection<JClass> glean( MetadataGleaningRequest request )
        throws Exception
    {
        Map<String, JClass> classes = new LinkedHashMap<String, JClass>( 1024 );
        List<PluginDescriptor> pluginDescriptors = new ArrayList<PluginDescriptor>();

        scanBinaries( classes, pluginDescriptors, request );

        scanMojoDescriptorsFromDependencies( classes, pluginDescriptors );

        scanSources( classes, request );

        return new ArrayList<JClass>( classes.values() );
    }

    private void scanBinaries( Map<String, JClass> classes, Collection<PluginDescriptor> pluginDescriptors,
                               MetadataGleaningRequest request )
        throws Exception
    {
        List<File> reversed = new ArrayList<File>( request.getCompileDependencies() );
        Collections.reverse( reversed );

        for ( File compileDependency : reversed )
        {
            if ( compileDependency.isDirectory() )
            {
                scanDirectory( classes, pluginDescriptors, compileDependency, false );
            }
            else
            {
                scanArchive( classes, pluginDescriptors, compileDependency );
            }
        }

        for ( File classesDir : request.getClassDirectories() )
        {
            scanDirectory( classes, pluginDescriptors, classesDir, true );
        }
    }

    private void scanMojoDescriptorsFromDependencies( Map<String, JClass> classes,
                                                      Collection<PluginDescriptor> descriptors )
    {
        for ( PluginDescriptor descriptor : descriptors )
        {
            @SuppressWarnings( "unchecked" )
            Collection<MojoDescriptor> mojoDescriptors = descriptor.getMojos();

            if ( mojoDescriptors != null )
            {
                for ( MojoDescriptor mojoDescriptor : mojoDescriptors )
                {
                    JClass jClass = classes.get( mojoDescriptor.getImplementation() );

                    if ( jClass != null )
                    {
                        merge( jClass, mojoDescriptor );

                        @SuppressWarnings( "unchecked" )
                        List<Parameter> params = mojoDescriptor.getParameters();

                        if ( params != null )
                        {
                            for ( Parameter param : params )
                            {
                                JField jField = getField( jClass, param.getName(), classes );
                                if ( jField != null )
                                {
                                    merge( jField, param );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void merge( JClass jClass, MojoDescriptor mojoDescriptor )
    {
        if ( mojoDescriptor.getDescription() != null )
        {
            jClass.setDescription( mojoDescriptor.getDescription() );
        }
        if ( mojoDescriptor.getDeprecated() != null )
        {
            jClass.setDeprecated( mojoDescriptor.getDeprecated() );
        }
        if ( mojoDescriptor.getSince() != null )
        {
            jClass.setSince( mojoDescriptor.getSince() );
        }

        if ( !jClass.hasAnnotation( Goal.class ) )
        {
            jClass.addAnnotation( new JAnnotation( Goal.class, mojoDescriptor.getGoal() ) );
        }
        if ( mojoDescriptor.isOnlineRequired() && !jClass.hasAnnotation( RequiresOnline.class ) )
        {
            jClass.addAnnotation( new JAnnotation( RequiresOnline.class, Boolean.TRUE ) );
        }
        if ( mojoDescriptor.isProjectRequired() && !jClass.hasAnnotation( RequiresProject.class ) )
        {
            jClass.addAnnotation( new JAnnotation( RequiresProject.class, Boolean.TRUE ) );
        }
        if ( mojoDescriptor.isDirectInvocationOnly() && !jClass.hasAnnotation( RequiresDirectInvocation.class ) )
        {
            jClass.addAnnotation( new JAnnotation( RequiresDirectInvocation.class, Boolean.TRUE ) );
        }
        if ( mojoDescriptor.isDependencyResolutionRequired() != null
            && !jClass.hasAnnotation( RequiresDependencyResolution.class ) )
        {
            jClass.addAnnotation( new JAnnotation( RequiresDependencyResolution.class,
                                                   mojoDescriptor.isDependencyResolutionRequired() ) );
        }
    }

    private void merge( JField jField, Parameter param )
    {
        if ( param.getDescription() != null )
        {
            jField.setDescription( param.getDescription() );
        }
        if ( param.getDeprecated() != null )
        {
            jField.setDeprecated( param.getDeprecated() );
        }
        if ( param.getSince() != null )
        {
            jField.setSince( param.getSince() );
        }

        if ( !jField.hasAnnotation( Configuration.class ) )
        {
            jField.addAnnotation( new JAnnotation( Configuration.class ) );
        }
        if ( !param.isEditable() && !jField.hasAnnotation( NonEditable.class ) )
        {
            jField.addAnnotation( new JAnnotation( NonEditable.class ) );
        }
        if ( param.isRequired() && !jField.hasAnnotation( Required.class ) )
        {
            jField.addAnnotation( new JAnnotation( Required.class ) );
        }
        if ( param.getAlias() != null && !jField.hasAnnotation( Alias.class ) )
        {
            jField.addAnnotation( new JAnnotation( Alias.class, param.getAlias() ) );
        }
        if ( param.getDefaultValue() != null && !jField.hasAnnotation( DefaultsTo.class ) )
        {
            jField.addAnnotation( new JAnnotation( DefaultsTo.class, param.getDefaultValue() ) );
        }
        if ( param.getExpression() != null )
        {
            String exp = param.getExpression();
            if ( exp.matches( "\\$\\{[^}]+\\}" ) )
            {
                jField.addAnnotation( new JAnnotation( Property.class, exp.substring( 2, exp.length() - 1 ) ) );
            }
            else if ( !jField.hasAnnotation( DefaultsTo.class ) )
            {
                jField.addAnnotation( new JAnnotation( DefaultsTo.class, param.getExpression() ) );
            }
        }
    }

    private JField getField( JClass jClass, String fieldName, Map<String, JClass> classes )
    {
        for ( JField jField : jClass.getFields() )
        {
            if ( jField.getName().equals( fieldName ) )
            {
                return jField;
            }
        }

        JClass superClass = classes.get( jClass.getSuperClass() );
        if ( superClass != null )
        {
            return getField( superClass, fieldName, classes );
        }

        return null;
    }

    private void scanDirectory( Map<String, JClass> classes, Collection<PluginDescriptor> descriptors, File classesDir,
                                boolean projectClasses )
        throws IOException
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( classesDir );
        scanner.addDefaultExcludes();
        if ( projectClasses )
        {
            scanner.setIncludes( new String[] { "**/*.class" } );
        }
        else
        {
            scanner.setIncludes( new String[] { "**/*.class", "META-INF/maven/plugin.xml" } );
        }
        scanner.scan();

        String[] classFiles = scanner.getIncludedFiles();

        for ( String classFile : classFiles )
        {
            InputStream is = new BufferedInputStream( new FileInputStream( new File( classesDir, classFile ) ) );
            try
            {
                if ( classFile.endsWith( "plugin.xml" ) )
                {
                    descriptors.add( readDescriptor( is ) );
                }
                else if ( classFile.endsWith( ".class" ) )
                {
                    JClass jClass = gleanClass( is );

                    jClass.setProjectClass( projectClasses );

                    classes.put( jClass.getName(), jClass );
                }
            }
            finally
            {
                IOUtil.close( is );
            }

        }
    }

    private void scanArchive( Map<String, JClass> classes, Collection<PluginDescriptor> descriptors, File classesArchive )
        throws IOException
    {
        ZipInputStream zis = new ZipInputStream( new FileInputStream( classesArchive ) );
        try
        {
            for ( ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry() )
            {
                if ( ze.getName().equals( "META-INF/maven/plugin.xml" ) )
                {
                    descriptors.add( readDescriptor( zis ) );
                }
                else if ( ze.getName().endsWith( ".class" ) )
                {
                    JClass jClass = gleanClass( zis );

                    classes.put( jClass.getName(), jClass );
                }
            }
        }
        finally
        {
            IOUtil.close( zis );
        }
    }

    private JClass gleanClass( InputStream is )
        throws IOException
    {
        ClassReader rdr = new ClassReader( is );

        JClassGleaner classGleaner = new JClassGleaner();

        rdr.accept( classGleaner, ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG );

        return classGleaner.getJClass();
    }

    private void scanSources( Map<String, JClass> classes, MetadataGleaningRequest request )
    {
        JavaDocBuilder jdocBuilder = new JavaDocBuilder();
        jdocBuilder.setEncoding( request.getSourceEncoding() );
        for ( File sourceDir : request.getSourceDirectories() )
        {
            jdocBuilder.addSourceTree( sourceDir );
        }

        for ( JavaClass jdocClass : jdocBuilder.getClasses() )
        {
            JClass jClass = classes.get( jdocClass.getFullyQualifiedName() );
            if ( jClass != null )
            {
                gleanJavadoc( jClass, jdocClass );

                Map<String, JField> fieldIndex = new HashMap<String, JField>();
                for ( JField jField : jClass.getFields() )
                {
                    fieldIndex.put( jField.getName(), jField );
                }

                for ( JavaField jdocField : jdocClass.getFields() )
                {
                    JField jField = fieldIndex.get( jdocField.getName() );
                    if ( jField != null )
                    {
                        gleanJavadoc( jField, jdocField );
                    }
                }
            }
        }
    }

    private void gleanJavadoc( JAnnotatedElement jElement, AbstractJavaEntity jdocElement )
    {
        jElement.setDescription( jdocElement.getComment() );

        DocletTag tag = jdocElement.getTagByName( "since" );
        if ( tag != null )
        {
            jElement.setSince( tag.getValue() );
        }

        tag = jdocElement.getTagByName( "deprecated" );
        if ( tag != null )
        {
            jElement.setDeprecated( tag.getValue() );
        }
    }

    private PluginDescriptor readDescriptor( InputStream is )
        throws IOException
    {
        Reader reader = ReaderFactory.newXmlReader( new NonClosingInputStream( is ) );

        PluginDescriptorBuilder builder = new PluginDescriptorBuilder();

        try
        {
            return builder.build( reader );
        }
        catch ( PlexusConfigurationException e )
        {
            throw (IOException) new IOException( e.getMessage() ).initCause( e );
        }
    }

    private static class NonClosingInputStream
        extends FilterInputStream
    {

        public NonClosingInputStream( InputStream is )
        {
            super( is );
        }

        @Override
        public void close()
        {
            // don't
        }

    }

}
