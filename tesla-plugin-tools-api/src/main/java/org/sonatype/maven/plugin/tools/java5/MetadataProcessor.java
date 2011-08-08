package org.sonatype.maven.plugin.tools.java5;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collection;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

public interface MetadataProcessor
{

    Collection<MojoDescriptor> process( MetadataProcessingRequest request )
        throws Exception;

}
