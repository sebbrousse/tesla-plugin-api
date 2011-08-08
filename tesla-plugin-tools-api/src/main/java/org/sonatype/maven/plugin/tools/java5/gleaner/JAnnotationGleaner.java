package org.sonatype.maven.plugin.tools.java5.gleaner;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.sonatype.maven.plugin.tools.java5.model.JAnnotation;

public class JAnnotationGleaner
    implements AnnotationVisitor
{

    private final JAnnotation jAnnotation;

    public JAnnotationGleaner( JAnnotation jAnnotation )
    {
        this.jAnnotation = jAnnotation;
    }

    public void visit( String name, Object value )
    {
        if ( value instanceof Type )
        {
            jAnnotation.addType( name, ( (Type) value ).getClassName() );
        }
        else
        {
            jAnnotation.addValue( name, value );
        }
    }

    public void visitEnum( String name, String desc, String value )
    {
        jAnnotation.addEnum( name, Type.getType( desc ).getClassName(), value );
    }

    public AnnotationVisitor visitAnnotation( String name, String desc )
    {
        JAnnotation jAnnotation = new JAnnotation( Type.getType( desc ).getClassName() );
        jAnnotation.addAnnotation( name, jAnnotation );
        return new JAnnotationGleaner( jAnnotation );
    }

    public AnnotationVisitor visitArray( String name )
    {
        JAnnotation.JArray jArray = new JAnnotation.JArray();
        jAnnotation.addArray( name, jArray );
        return new JAnnotationArrayGleaner( jArray );
    }

    public void visitEnd()
    {
    }

    static class JAnnotationArrayGleaner
        implements AnnotationVisitor
    {

        private final JAnnotation.JArray jArray;

        public JAnnotationArrayGleaner( JAnnotation.JArray jArray )
        {
            this.jArray = jArray;
        }

        public void visit( String name, Object value )
        {
            if ( value instanceof Type )
            {
                jArray.addType( ( (Type) value ).getClassName() );
            }
            else
            {
                jArray.addValue( value );
            }
        }

        public void visitEnum( String name, String desc, String value )
        {
            jArray.addEnum( Type.getType( desc ).getClassName(), value );
        }

        public AnnotationVisitor visitAnnotation( String name, String desc )
        {
            JAnnotation jAnnotation = new JAnnotation( Type.getType( desc ).getClassName() );
            jArray.addAnnotation( jAnnotation );
            return new JAnnotationGleaner( jAnnotation );
        }

        public AnnotationVisitor visitArray( String name )
        {
            return null;
        }

        public void visitEnd()
        {
        }

    }

}
