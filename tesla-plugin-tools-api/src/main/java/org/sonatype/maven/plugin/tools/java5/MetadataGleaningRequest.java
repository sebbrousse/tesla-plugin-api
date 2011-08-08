package org.sonatype.maven.plugin.tools.java5;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetadataGleaningRequest
{

    private List<File> classDirectories = new ArrayList<File>();

    private List<File> compileDependencies = new ArrayList<File>();

    private List<File> sourceDirectories = new ArrayList<File>();

    private String sourceEncoding = "UTF-8";

    public List<File> getClassDirectories()
    {
        return Collections.unmodifiableList( classDirectories );
    }

    public void addClassDirectory( File classDirectory )
    {
        classDirectories.add( classDirectory );
    }

    public List<File> getCompileDependencies()
    {
        return Collections.unmodifiableList( compileDependencies );
    }

    public void addCompileDependency( File compileDependency )
    {
        compileDependencies.add( compileDependency );
    }

    public List<File> getSourceDirectories()
    {
        return Collections.unmodifiableList( sourceDirectories );
    }

    public void addSourceDirectory( File sourceDirectory )
    {
        sourceDirectories.add( sourceDirectory );
    }

    public String getSourceEncoding()
    {
        return sourceEncoding;
    }

    public void setSourceEncoding( String sourceEncoding )
    {
        if ( sourceEncoding == null || sourceEncoding.length() <= 0 )
        {
            this.sourceEncoding = System.getProperty( "file.encoding" );
        }
        else
        {
            this.sourceEncoding = sourceEncoding;
        }
    }

}
