package org.sonatype.maven.plugin;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Locale;

public enum LifecyclePhase
{

    VALIDATE,
    INITIALIZE,
    GENERATE_SOURCES,
    PROCESS_SOURCES,
    GENERATE_RESOURCES,
    PROCESS_RESOURCES,
    COMPILE,
    PROCESS_CLASSES,
    GENERATE_TEST_SOURCES,
    PROCESS_TEST_SOURCES,
    GENERATE_TEST_RESOURCES,
    PROCESS_TEST_RESOURCES,
    TEST_COMPILE,
    PROCESS_TEST_CLASSES,
    TEST,
    PREPARE_PACKAGE,
    PACKAGE,
    PRE_INTEGRATION_TEST,
    INTEGRATION_TEST,
    POST_INTEGRATION_TEST,
    VERIFY,
    INSTALL,
    DEPLOY;

    @Override
    public String toString()
    {
        return name().toLowerCase( Locale.ENGLISH ).replace( '_', '-' );
    }

}
