package org.sonatype.maven.plugin.tools.java5.processor;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.descriptor.InvalidParameterException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.tools.plugin.ExtendedMojoDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.sonatype.maven.plugin.Alias;
import org.sonatype.maven.plugin.Configuration;
import org.sonatype.maven.plugin.DefaultPhase;
import org.sonatype.maven.plugin.DefaultsTo;
import org.sonatype.maven.plugin.Goal;
import org.sonatype.maven.plugin.NonEditable;
import org.sonatype.maven.plugin.Property;
import org.sonatype.maven.plugin.Required;
import org.sonatype.maven.plugin.RequiresDependencyCollection;
import org.sonatype.maven.plugin.RequiresDependencyResolution;
import org.sonatype.maven.plugin.RequiresDirectInvocation;
import org.sonatype.maven.plugin.RequiresOnline;
import org.sonatype.maven.plugin.RequiresProject;
import org.sonatype.maven.plugin.ThreadSafe;
import org.sonatype.maven.plugin.tools.java5.MetadataProcessingRequest;
import org.sonatype.maven.plugin.tools.java5.MetadataProcessor;
import org.sonatype.maven.plugin.tools.java5.model.JClass;
import org.sonatype.maven.plugin.tools.java5.model.JField;

@Component( role = MetadataProcessor.class )
public class DefaultMetadataProcessor
    implements MetadataProcessor
{

    public Collection<MojoDescriptor> process( MetadataProcessingRequest request )
        throws Exception
    {
        List<MojoDescriptor> descriptors = new ArrayList<MojoDescriptor>();

        List<JClass> mojoClasses = new ArrayList<JClass>();
        Map<String, JClass> classIndex = new HashMap<String, JClass>( request.getClasses().size() * 2 );

        for ( JClass jClass : request.getClasses() )
        {
            classIndex.put( jClass.getName(), jClass );

            if ( jClass.isProjectClass() && jClass.isPublic() && jClass.hasAnnotation( Goal.class ) )
            {
                mojoClasses.add( jClass );
            }
        }
        
        for ( JClass mojoClass : mojoClasses )
        {
            MojoDescriptor mojoDescriptor = createDescriptor( mojoClass, classIndex );

            mojoDescriptor.setPluginDescriptor( request.getPluginDescriptor() );

            descriptors.add( mojoDescriptor );
        }

        return descriptors;
    }

    private MojoDescriptor createDescriptor( JClass mojoClass, Map<String, JClass> classIndex )
        throws Exception
    {
        ExtendedMojoDescriptor mojoDescriptor = new ExtendedMojoDescriptor();

        mojoDescriptor.setImplementation( mojoClass.getName() );

        mojoDescriptor.setInheritedByDefault( true );
        mojoDescriptor.setAggregator( false );

        mojoDescriptor.setDescription( mojoClass.getDescription() );
        mojoDescriptor.setDeprecated( mojoClass.getDeprecated() );
        mojoDescriptor.setSince( getSince( mojoClass, classIndex ) );

        Goal goal = getAnno( mojoClass, classIndex, Goal.class );
        mojoDescriptor.setGoal( goal.value() );

        ThreadSafe threadSafe = getAnno( mojoClass, classIndex, ThreadSafe.class );
        if ( threadSafe != null )
        {
            mojoDescriptor.setThreadSafe( threadSafe.value() );
        }
        else
        {
            mojoDescriptor.setThreadSafe( false );
        }

        DefaultPhase defaultPhase = getAnno( mojoClass, classIndex, DefaultPhase.class );
        if ( defaultPhase != null )
        {
            mojoDescriptor.setPhase( defaultPhase.value().toString() );
        }

        RequiresOnline requiresOnline = getAnno( mojoClass, classIndex, RequiresOnline.class );
        if ( requiresOnline != null )
        {
            mojoDescriptor.setOnlineRequired( requiresOnline.value() );
        }
        else
        {
            mojoDescriptor.setOnlineRequired( false );
        }

        RequiresProject requiresProject = getAnno( mojoClass, classIndex, RequiresProject.class );
        if ( requiresProject != null )
        {
            mojoDescriptor.setProjectRequired( requiresProject.value() );
        }
        else
        {
            mojoDescriptor.setProjectRequired( true );
        }

        RequiresDirectInvocation requiresDirectInvocation =
            getAnno( mojoClass, classIndex, RequiresDirectInvocation.class );
        if ( requiresDirectInvocation != null )
        {
            mojoDescriptor.setDirectInvocationOnly( requiresDirectInvocation.value() );
        }
        else
        {
            mojoDescriptor.setDirectInvocationOnly( false );
        }

        RequiresDependencyResolution requiresDependencyResolution =
            getAnno( mojoClass, classIndex, RequiresDependencyResolution.class );
        if ( requiresDependencyResolution != null )
        {
            mojoDescriptor.setDependencyResolutionRequired( requiresDependencyResolution.value().toString() );
        }

        RequiresDependencyCollection requiresDependencyCollection =
            getAnno( mojoClass, classIndex, RequiresDependencyCollection.class );
        if ( requiresDependencyCollection != null )
        {
            mojoDescriptor.setDependencyCollectionRequired( requiresDependencyCollection.value().toString() );
        }

        populateParameters( mojoDescriptor, mojoClass, classIndex );
        
        return mojoDescriptor;
    }

    private String getSince( JClass jClass, Map<String, JClass> classIndex )
    {
        for ( JClass superClass = jClass; superClass != null; superClass = classIndex.get( superClass.getSuperClass() ) )
        {
            String since = superClass.getSince();
            if ( since != null )
            {
                return since;
            }
        }
        return null;
    }

    private void populateParameters( MojoDescriptor mojoDescriptor, JClass mojoClass, Map<String, JClass> classIndex )
        throws Exception
    {
        Map<String, JField> fields = getAllFields( mojoClass, classIndex );

        for ( JField jField : fields.values() )
        {
            if ( jField.hasAnnotation( Configuration.class ) )
            {
                Parameter parameter = new Parameter();

                parameter.setName( jField.getName() );
                parameter.setType( jField.getType() );

                parameter.setDescription( jField.getDescription() );
                parameter.setDeprecated( jField.getDeprecated() );
                parameter.setSince( jField.getSince() );

                parameter.setRequired( jField.hasAnnotation( Required.class ) );
                parameter.setEditable( !jField.hasAnnotation( NonEditable.class ) );

                Alias alias = jField.getAnnotation( Alias.class );
                if ( alias != null )
                {
                    if ( !parameter.isEditable() )
                    {
                        throw newBadFieldException( jField, "non-editable parameter must not have an alias" );
                    }
                    if ( alias.value().length() <= 0 )
                    {
                        throw newBadFieldException( jField, "alias for parameter must not be empty" );
                    }
                    parameter.setAlias( alias.value() );
                }

                Property property = jField.getAnnotation( Property.class );
                if ( property != null )
                {
                    if ( !parameter.isEditable() )
                    {
                        throw newBadFieldException( jField, "non-editable parameter must not have a property" );
                    }
                    if ( property.value().length() <= 0 )
                    {
                        throw newBadFieldException( jField, "property name must not be empty" );
                    }
                    parameter.setExpression( "${" + property.value() + "}" );
                }

                DefaultsTo defaultValue = jField.getAnnotation( DefaultsTo.class );
                if ( defaultValue != null )
                {
                    parameter.setDefaultValue( defaultValue.value() );
                }
                else if ( !parameter.isEditable() )
                {
                    throw newBadFieldException( jField, "non-editable parameter must have a default value" );
                }

                mojoDescriptor.addParameter( parameter );
            }
        }
    }

    private Exception newBadFieldException( JField jField, String msg )
    {
        return new InvalidParameterException( jField.getDeclaringClass().getName() + "#" + jField.getName() + ": "
            + msg, null );
    }

    private Map<String, JField> getAllFields( JClass jClass, Map<String, JClass> classIndex )
    {
        JClass superClass = classIndex.get( jClass.getSuperClass() );

        Map<String, JField> fields;
        if ( superClass != null )
        {
            fields = getAllFields( superClass, classIndex );
        }
        else
        {
            fields = new LinkedHashMap<String, JField>();
        }

        for ( JField jField : jClass.getFields() )
        {
            fields.put( jField.getName(), jField );
        }

        return fields;
    }

    private <A extends Annotation> A getAnno( JClass jClass, Map<String, JClass> classIndex, Class<A> annotationClass )
    {
        A annotation = jClass.getAnnotation( annotationClass );

        if ( annotation == null && annotationClass.isAnnotationPresent( Inherited.class ) )
        {
            for ( JClass superClass = classIndex.get( jClass.getSuperClass() ); superClass != null
                && annotation == null; superClass = classIndex.get( superClass.getSuperClass() ) )
            {
                annotation = superClass.getAnnotation( annotationClass );
            }
        }

        return annotation;
    }

}
