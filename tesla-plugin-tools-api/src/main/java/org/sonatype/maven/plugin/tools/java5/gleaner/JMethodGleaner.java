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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.sonatype.maven.plugin.tools.java5.model.JAnnotation;
import org.sonatype.maven.plugin.tools.java5.model.JMethod;
import org.sonatype.maven.plugin.tools.java5.model.JMethodParameter;

public class JMethodGleaner
    implements MethodVisitor
{

    private final JMethod jMethod;

    public JMethodGleaner( JMethod jMethod )
    {
        this.jMethod = jMethod;
    }

    public AnnotationVisitor visitAnnotationDefault()
    {
        return null;
    }

    public AnnotationVisitor visitAnnotation( String desc, boolean visible )
    {
        JAnnotation jAnnotation = new JAnnotation( Type.getType( desc ).getClassName() );
        jMethod.addAnnotation( jAnnotation );
        return new JAnnotationGleaner( jAnnotation );
    }

    public AnnotationVisitor visitParameterAnnotation( int parameter, String desc, boolean visible )
    {
        JMethodParameter jMethodParameter = jMethod.getParameters().get( parameter );
        JAnnotation jAnnotation = new JAnnotation( Type.getType( desc ).getClassName() );
        jMethodParameter.addAnnotation( jAnnotation );
        return new JAnnotationGleaner( jAnnotation );
    }

    public void visitAttribute( Attribute attr )
    {
    }

    public void visitCode()
    {
    }

    public void visitFrame( int type, int nLocal, Object[] local, int nStack, Object[] stack )
    {
    }

    public void visitInsn( int opcode )
    {
    }

    public void visitIntInsn( int opcode, int operand )
    {
    }

    public void visitVarInsn( int opcode, int var )
    {
    }

    public void visitTypeInsn( int opcode, String type )
    {
    }

    public void visitFieldInsn( int opcode, String owner, String name, String desc )
    {
    }

    public void visitMethodInsn( int opcode, String owner, String name, String desc )
    {
    }

    public void visitJumpInsn( int opcode, Label label )
    {
    }

    public void visitLabel( Label label )
    {
    }

    public void visitLdcInsn( Object cst )
    {
    }

    public void visitIincInsn( int var, int increment )
    {
    }

    public void visitTableSwitchInsn( int min, int max, Label dflt, Label[] labels )
    {
    }

    public void visitLookupSwitchInsn( Label dflt, int[] keys, Label[] labels )
    {
    }

    public void visitMultiANewArrayInsn( String desc, int dims )
    {
    }

    public void visitTryCatchBlock( Label start, Label end, Label handler, String type )
    {
    }

    public void visitLocalVariable( String name, String desc, String signature, Label start, Label end, int index )
    {
    }

    public void visitLineNumber( int line, Label start )
    {
    }

    public void visitMaxs( int maxStack, int maxLocals )
    {
    }

    public void visitEnd()
    {
    }

}
