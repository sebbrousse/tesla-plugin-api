package org.sonatype.maven.plugin.tools.java5.gleaner;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.tools.plugin.ExtendedMojoDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class PluginDescriptorBuilder
{
    public PluginDescriptor build( Reader reader )
        throws PlexusConfigurationException
    {
        return build( reader, null );
    }

    public PluginDescriptor build( Reader reader, String source )
        throws PlexusConfigurationException
    {
        PlexusConfiguration c = buildConfiguration( reader );

        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        pluginDescriptor.setSource( source );
        pluginDescriptor.setGroupId( c.getChild( "groupId" ).getValue() );
        pluginDescriptor.setArtifactId( c.getChild( "artifactId" ).getValue() );
        pluginDescriptor.setVersion( c.getChild( "version" ).getValue() );
        pluginDescriptor.setGoalPrefix( c.getChild( "goalPrefix" ).getValue() );

        pluginDescriptor.setName( c.getChild( "name" ).getValue() );
        pluginDescriptor.setDescription( c.getChild( "description" ).getValue() );

        String isolatedRealm = c.getChild( "isolatedRealm" ).getValue();

        if ( isolatedRealm != null )
        {
            pluginDescriptor.setIsolatedRealm( Boolean.parseBoolean( isolatedRealm ) );
        }

        String inheritedByDefault = c.getChild( "inheritedByDefault" ).getValue();

        if ( inheritedByDefault != null )
        {
            pluginDescriptor.setInheritedByDefault( Boolean.parseBoolean( inheritedByDefault ) );
        }

        // ----------------------------------------------------------------------
        // Components
        // ----------------------------------------------------------------------

        PlexusConfiguration[] mojoConfigurations = c.getChild( "mojos" ).getChildren( "mojo" );

        for ( PlexusConfiguration component : mojoConfigurations )
        {
            MojoDescriptor mojoDescriptor = buildComponentDescriptor( component, pluginDescriptor );

            pluginDescriptor.addMojo( mojoDescriptor );
        }

        return pluginDescriptor;
    }

    public MojoDescriptor buildComponentDescriptor( PlexusConfiguration c, PluginDescriptor pluginDescriptor )
        throws PlexusConfigurationException
    {
        ExtendedMojoDescriptor mojo = new ExtendedMojoDescriptor();
        mojo.setPluginDescriptor( pluginDescriptor );

        mojo.setGoal( c.getChild( "goal" ).getValue() );

        mojo.setImplementation( c.getChild( "implementation" ).getValue() );

        PlexusConfiguration langConfig = c.getChild( "language" );

        if ( langConfig != null )
        {
            mojo.setLanguage( langConfig.getValue() );
        }

        PlexusConfiguration configuratorConfig = c.getChild( "configurator" );

        if ( configuratorConfig != null )
        {
            mojo.setComponentConfigurator( configuratorConfig.getValue() );
        }

        PlexusConfiguration composerConfig = c.getChild( "composer" );

        if ( composerConfig != null )
        {
            mojo.setComponentComposer( composerConfig.getValue() );
        }

        String since = c.getChild( "since" ).getValue();

        if ( since != null )
        {
            mojo.setSince( since );
        }

        PlexusConfiguration deprecated = c.getChild( "deprecated", false );

        if ( deprecated != null )
        {
            mojo.setDeprecated( deprecated.getValue() );
        }

        String phase = c.getChild( "phase" ).getValue();

        if ( phase != null )
        {
            mojo.setPhase( phase );
        }

        String executePhase = c.getChild( "executePhase" ).getValue();

        if ( executePhase != null )
        {
            mojo.setExecutePhase( executePhase );
        }

        String executeMojo = c.getChild( "executeGoal" ).getValue();

        if ( executeMojo != null )
        {
            mojo.setExecuteGoal( executeMojo );
        }

        String executeLifecycle = c.getChild( "executeLifecycle" ).getValue();

        if ( executeLifecycle != null )
        {
            mojo.setExecuteLifecycle( executeLifecycle );
        }

        mojo.setInstantiationStrategy( c.getChild( "instantiationStrategy" ).getValue() );

        mojo.setDescription( c.getChild( "description" ).getValue() );

        PlexusConfiguration dependencyResolution = c.getChild( "requiresDependencyResolution", false );

        if ( dependencyResolution != null )
        {
            mojo.setDependencyResolutionRequired( dependencyResolution.getValue() );
        }

        PlexusConfiguration dependencyCollection = c.getChild( "requiresDependencyCollection", false );

        if ( dependencyCollection != null )
        {
            mojo.setDependencyCollectionRequired( dependencyCollection.getValue() );
        }

        String directInvocationOnly = c.getChild( "requiresDirectInvocation" ).getValue();

        if ( directInvocationOnly != null )
        {
            mojo.setDirectInvocationOnly( Boolean.parseBoolean( directInvocationOnly ) );
        }

        String requiresProject = c.getChild( "requiresProject" ).getValue();

        if ( requiresProject != null )
        {
            mojo.setProjectRequired( Boolean.parseBoolean( requiresProject ) );
        }

        String requiresReports = c.getChild( "requiresReports" ).getValue();

        if ( requiresReports != null )
        {
            mojo.setRequiresReports( Boolean.parseBoolean( requiresReports ) );
        }

        String aggregator = c.getChild( "aggregator" ).getValue();

        if ( aggregator != null )
        {
            mojo.setAggregator( Boolean.parseBoolean( aggregator ) );
        }

        String requiresOnline = c.getChild( "requiresOnline" ).getValue();

        if ( requiresOnline != null )
        {
            mojo.setOnlineRequired( Boolean.parseBoolean( requiresOnline ) );
        }

        String inheritedByDefault = c.getChild( "inheritedByDefault" ).getValue();

        if ( inheritedByDefault != null )
        {
            mojo.setInheritedByDefault( Boolean.parseBoolean( inheritedByDefault ) );
        }

        String threadSafe = c.getChild( "threadSafe" ).getValue();

        if ( threadSafe != null )
        {
            mojo.setThreadSafe( Boolean.parseBoolean( threadSafe ) );
        }

        // ----------------------------------------------------------------------
        // Parameters
        // ----------------------------------------------------------------------

        PlexusConfiguration[] parameterConfigurations = c.getChild( "parameters" ).getChildren( "parameter" );

        List<Parameter> parameters = new ArrayList<Parameter>();

        for ( PlexusConfiguration d : parameterConfigurations )
        {
            Parameter parameter = new Parameter();

            parameter.setName( d.getChild( "name" ).getValue() );

            parameter.setAlias( d.getChild( "alias" ).getValue() );

            parameter.setType( d.getChild( "type" ).getValue() );

            String required = d.getChild( "required" ).getValue();

            parameter.setRequired( Boolean.parseBoolean( required ) );

            PlexusConfiguration editableConfig = d.getChild( "editable" );

            // we need the null check for pre-build legacy plugins...
            if ( editableConfig != null )
            {
                String editable = d.getChild( "editable" ).getValue();

                parameter.setEditable( editable == null || Boolean.parseBoolean( editable ) );
            }

            parameter.setDescription( d.getChild( "description" ).getValue() );

            parameter.setDeprecated( d.getChild( "deprecated" ).getValue() );

            parameter.setImplementation( d.getChild( "implementation" ).getValue() );

            parameters.add( parameter );
        }

        mojo.setParameters( parameters );

        // ----------------------------------------------------------------------
        // Configuration
        // ----------------------------------------------------------------------

        mojo.setMojoConfiguration( c.getChild( "configuration" ) );

        if ( mojo.getMojoConfiguration() != null )
        {
            for ( PlexusConfiguration config : mojo.getMojoConfiguration().getChildren() )
            {
                Parameter param = (Parameter) mojo.getParameterMap().get( config.getName() );
                if ( param != null )
                {
                    param.setDefaultValue( config.getAttribute( "default-value", null ) );
                    param.setExpression( config.getValue( null ) );
                }
            }
        }

        // ----------------------------------------------------------------------
        // Requirements
        // ----------------------------------------------------------------------

        PlexusConfiguration[] requirements = c.getChild( "requirements" ).getChildren( "requirement" );

        for ( PlexusConfiguration requirement : requirements )
        {
            ComponentRequirement cr = new ComponentRequirement();

            cr.setRole( requirement.getChild( "role" ).getValue() );

            cr.setRoleHint( requirement.getChild( "role-hint" ).getValue() );

            cr.setFieldName( requirement.getChild( "field-name" ).getValue() );

            mojo.addRequirement( cr );
        }

        return mojo;
    }

    private PlexusConfiguration buildConfiguration( Reader configuration )
        throws PlexusConfigurationException
    {
        try
        {
            return new XmlPlexusConfiguration( Xpp3DomBuilder.build( configuration ) );
        }
        catch ( IOException e )
        {
            throw new PlexusConfigurationException( e.getMessage(), e );
        }
        catch ( XmlPullParserException e )
        {
            throw new PlexusConfigurationException( e.getMessage(), e );
        }
    }

}
