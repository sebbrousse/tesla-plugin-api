package org.sonatype.maven.plugin;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/**
 * 
 */
public enum ClasspathType
{

    COMPILE( "compile" ), COMPILE_PLUS_RUNTIME( "compile+runtime" ), RUNTIME( "runtime" ), TEST( "test" );

    private final String value;

    private ClasspathType( String value )
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }

}
