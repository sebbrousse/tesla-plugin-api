package org.sonatype.maven.plugin.tools.java5.model;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JMethod
    extends JAnnotatedElement
{

    private String name;

    private String returnType;

    private int modifiers;

    private List<JMethodParameter> parameters = new ArrayList<JMethodParameter>();

    private JClass declaringClass;

    public JMethod( String name, int modifiers, String returnType )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "method name missing" );
        }
        if ( returnType == null )
        {
            throw new IllegalArgumentException( "method return type missing" );
        }
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
    }

    public String getName()
    {
        return name;
    }

    public String getReturnType()
    {
        return returnType;
    }

    public int getModifiers()
    {
        return modifiers;
    }

    public List<JMethodParameter> getParameters()
    {
        return Collections.unmodifiableList( parameters );
    }

    public void addParameter( JMethodParameter parameter )
    {
        parameters.add( parameter );
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
        return getName() + getParameters();
    }

}
