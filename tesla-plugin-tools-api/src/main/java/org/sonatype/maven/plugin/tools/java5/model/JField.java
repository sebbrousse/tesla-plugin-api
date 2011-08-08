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
public class JField
    extends JAnnotatedElement
{

    private String name;

    private String type;

    private int modifiers;

    private JClass declaringClass;

    public JField( String name, int modifiers, String type )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "field name missing" );
        }
        if ( type == null )
        {
            throw new IllegalArgumentException( "field type missing" );
        }
        this.name = name;
        this.modifiers = modifiers;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public int getModifiers()
    {
        return modifiers;
    }

    public JClass getDeclaringClass()
    {
        return declaringClass;
    }

    void setDeclaringClass( JClass declaringClass )
    {
        if ( this.declaringClass != null )
        {
            throw new IllegalStateException( "declaring class already set" );
        }
        this.declaringClass = declaringClass;
    }

    @Override
    public String toString()
    {
        return getType() + " " + getName();
    }

}
