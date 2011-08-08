package org.sonatype.maven.plugin.tools.java5.model;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/**
 */
public class JMethodParameter
    extends JAnnotatedElement
{

    private String type;

    public JMethodParameter( String type )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "method parameter type missing" );
        }
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return getType();
    }

}
