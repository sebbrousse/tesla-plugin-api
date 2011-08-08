package org.sonatype.maven.plugin.tools.java5.gleaner;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.sonatype.maven.plugin.tools.java5.model.JAnnotation;
import org.sonatype.maven.plugin.tools.java5.model.JClass;
import org.sonatype.maven.plugin.tools.java5.model.JField;
import org.sonatype.maven.plugin.tools.java5.model.JMethod;
import org.sonatype.maven.plugin.tools.java5.model.JMethodParameter;

public class JClassGleaner
    implements ClassVisitor
{

    private JClass jClass;

    public JClass getJClass()
    {
        return jClass;
    }

    public void visit( int version, int access, String name, String signature, String superName, String[] interfaces )
    {
        jClass =
            new JClass( Type.getObjectType( name ).getClassName(), access,
                        Type.getObjectType( superName ).getClassName() );
    }

    public AnnotationVisitor visitAnnotation( String desc, boolean visible )
    {
        JAnnotation jAnnotation = new JAnnotation( Type.getType( desc ).getClassName() );
        jClass.addAnnotation( jAnnotation );
        return new JAnnotationGleaner( jAnnotation );
    }

    public FieldVisitor visitField( int access, String name, String desc, String signature, Object value )
    {
        JField jField = new JField( name, access, Type.getType( desc ).getClassName() );
        jClass.addField( jField );
        return new JFieldGleaner( jField );
    }

    public MethodVisitor visitMethod( int access, String name, String desc, String signature, String[] exceptions )
    {
        JMethod jMethod = new JMethod( name, access, Type.getReturnType( desc ).getClassName() );
        Type[] paramTypes = Type.getArgumentTypes( desc );
        for ( int i = 0; i < paramTypes.length; i++ )
        {
            JMethodParameter jMethodParameter = new JMethodParameter( paramTypes[i].getClassName() );
            jMethod.addParameter( jMethodParameter );
        }
        if ( "<init>".equals( name ) )
        {
            jClass.addConstructor( jMethod );
        }
        else
        {
            jClass.addMethod( jMethod );
        }
        return new JMethodGleaner( jMethod );
    }

    public void visitAttribute( Attribute attr )
    {
    }

    public void visitSource( String source, String debug )
    {
    }

    public void visitOuterClass( String owner, String name, String desc )
    {
    }

    public void visitInnerClass( String name, String outerName, String innerName, int access )
    {
    }

    public void visitEnd()
    {
    }

}
