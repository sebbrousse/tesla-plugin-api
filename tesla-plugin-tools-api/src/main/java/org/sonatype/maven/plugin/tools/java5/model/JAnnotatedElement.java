package org.sonatype.maven.plugin.tools.java5.model;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class JAnnotatedElement
{

    private Map<String, JAnnotation> annotations = new HashMap<String, JAnnotation>();

    private String description;

    private String since;

    private String deprecated;

    public void addAnnotation( JAnnotation annotation )
    {
        annotations.put( annotation.getName(), annotation );

        if ( Deprecated.class.equals( annotation.getName() ) && getDeprecated() == null )
        {
            setDeprecated( "" );
        }
    }

    public boolean hasAnnotation( Class<? extends Annotation> annotationClass )
    {
        return annotations.get( annotationClass.getName() ) != null;
    }

    public <A extends Annotation> A getAnnotation( Class<A> annotationClass )
    {
        JAnnotation annotation = annotations.get( annotationClass.getName() );
        if ( annotation == null )
        {
            return null;
        }
        return annotation.get( annotationClass );
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getSince()
    {
        return since;
    }

    public void setSince( String since )
    {
        this.since = since;
    }

    public String getDeprecated()
    {
        return deprecated;
    }

    public void setDeprecated( String deprecated )
    {
        this.deprecated = deprecated;
    }

}
