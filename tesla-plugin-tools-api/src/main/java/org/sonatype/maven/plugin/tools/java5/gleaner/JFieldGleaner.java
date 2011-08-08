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
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;
import org.sonatype.maven.plugin.tools.java5.model.JAnnotation;
import org.sonatype.maven.plugin.tools.java5.model.JField;

public class JFieldGleaner
    implements FieldVisitor
{

    private final JField jField;

    public JFieldGleaner( JField jField )
    {
        this.jField = jField;
    }

    public AnnotationVisitor visitAnnotation( String desc, boolean visible )
    {
        JAnnotation jAnnotation = new JAnnotation( Type.getType( desc ).getClassName() );
        jField.addAnnotation( jAnnotation );
        return new JAnnotationGleaner( jAnnotation );
    }

    public void visitAttribute( Attribute attr )
    {
    }

    public void visitEnd()
    {
    }

}
