package org.sonatype.maven.plugin.tools.java5;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collection;
import java.util.Collections;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.sonatype.maven.plugin.tools.java5.model.JClass;

public class MetadataProcessingRequest
{

    private PluginDescriptor pluginDescriptor;

    private Collection<JClass> classes;

    public PluginDescriptor getPluginDescriptor()
    {
        return pluginDescriptor;
    }

    public void setPluginDescriptor( PluginDescriptor pluginDescriptor )
    {
        this.pluginDescriptor = pluginDescriptor;
    }

    public void setClasses( Collection<JClass> classes )
    {
        if ( classes == null )
        {
            this.classes = Collections.emptyList();
        }
        else
        {
            this.classes = classes;
        }
    }

    public Collection<JClass> getClasses()
    {
        return Collections.unmodifiableCollection( classes );
    }

}
