package org.sonatype.maven.plugin.tools.java5.model;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JClass
    extends JAnnotatedElement
{

    private String name;

    private String superClass;

    private int modifiers;

    private List<JField> fields = new ArrayList<JField>();

    private List<JMethod> constructors = new ArrayList<JMethod>();

    private List<JMethod> methods = new ArrayList<JMethod>();

    private boolean projectClass;

    public JClass( String name, int modifiers, String superClass )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "class name missing" );
        }
        this.name = name;
        this.modifiers = modifiers;
        this.superClass = superClass;
    }

    public boolean isProjectClass()
    {
        return projectClass;
    }

    public void setProjectClass( boolean projectClass )
    {
        this.projectClass = projectClass;
    }

    public String getName()
    {
        return name;
    }

    public String getSuperClass()
    {
        return superClass;
    }

    public int getModifiers()
    {
        return modifiers;
    }

    public boolean isPublic()
    {
        return Modifier.isPublic( getModifiers() );
    }

    public List<JField> getFields()
    {
        return Collections.unmodifiableList( fields );
    }

    public void addField( JField field )
    {
        fields.add( field );
        field.setDeclaringClass( this );
    }

    public List<JMethod> getConstructors()
    {
        return Collections.unmodifiableList( constructors );
    }

    public void addConstructor( JMethod method )
    {
        constructors.add( method );
        method.setDeclaringClass( this );
    }

    public List<JMethod> getMethods()
    {
        return Collections.unmodifiableList( methods );
    }

    public void addMethod( JMethod method )
    {
        methods.add( method );
        method.setDeclaringClass( this );
    }

    @Override
    public String toString()
    {
        return getName();
    }

}
