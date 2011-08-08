package org.sonatype.maven.plugin.tools.java5.model;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JAnnotation
{

    private String name;

    private Map<String, Object> values = new LinkedHashMap<String, Object>();

    private transient Object annotationProxy;

    public JAnnotation( Class<? extends Annotation> type )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "annotation type missing" );
        }
        name = type.getName();
    }

    public JAnnotation( Class<? extends Annotation> type, Object value )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "annotation type missing" );
        }
        if ( value == null )
        {
            throw new IllegalArgumentException( "annotation value must not be null" );
        }
        name = type.getName();
        addValue( "value", value );
    }

    public JAnnotation( String name )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "annotation type missing" );
        }
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void addValue( String name, Object value )
    {
        values.put( name, value );
    }

    public void addEnum( String name, String type, String value )
    {
        values.put( name, new EnumValue( type, value ) );
    }

    public void addType( String name, String value )
    {
        values.put( name, new TypeValue( value ) );
    }

    public void addArray( String name, JArray array )
    {
        values.put( name, array );
    }

    public void addAnnotation( String name, JAnnotation annotation )
    {
        values.put( name, annotation );
    }

    <T extends Annotation> T get( Class<T> type )
    {
        if ( !getName().equals( type.getName() ) )
        {
            throw new IllegalArgumentException( "bad annotation class " + type.getName() + ", should be " + getName() );
        }

        if ( !type.isInstance( annotationProxy ) )
        {
            annotationProxy =
                Proxy.newProxyInstance( type.getClassLoader(), new Class<?>[] { type },
                                        new AnnotationInvocationHandler( type ) );
        }

        return type.cast( annotationProxy );
    }

    @Override
    public String toString()
    {
        return getName() + values;
    }

    public static class JArray
    {

        List<Object> values = new ArrayList<Object>();

        public void addValue( Object value )
        {
            values.add( value );
        }

        public void addType( String value )
        {
            values.add( new TypeValue( value ) );
        }

        public void addEnum( String type, String value )
        {
            values.add( new EnumValue( type, value ) );
        }

        public void addAnnotation( JAnnotation annotation )
        {
            values.add( annotation );
        }

        @Override
        public String toString()
        {
            return values.toString();
        }

    }

    static class EnumValue
    {

        public final String name;

        public final String value;

        public EnumValue( String name, String value )
        {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return name + "." + value;
        }

    }

    static class TypeValue
    {

        public final String name;

        public TypeValue( String name )
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }

    }

    class AnnotationInvocationHandler
        implements InvocationHandler
    {

        private final Class<? extends Annotation> annotationClass;

        public AnnotationInvocationHandler( Class<? extends Annotation> annotationClass )
        {
            this.annotationClass = annotationClass;
        }

        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            if ( "toString".equals( method.getName() ) )
            {
                return JAnnotation.this.toString();
            }

            Object value = JAnnotation.this.values.get( method.getName() );
            if ( value != null )
            {
                return convert( method.getReturnType(), value );
            }

            Method am = annotationClass.getDeclaredMethod( method.getName(), method.getParameterTypes() );
            return am.getDefaultValue();
        }

        @SuppressWarnings( { "rawtypes", "unchecked" } )
        private Object convert( Class<?> type, Object value )
            throws Exception
        {
            Object result = value;

            if ( value instanceof EnumValue )
            {
                EnumValue enumValue = (EnumValue) value;
                Class enumType = type;
                result = Enum.valueOf( enumType, enumValue.value );
            }
            else if ( value instanceof TypeValue )
            {
                TypeValue typeValue = (TypeValue) value;
                result = annotationClass.getClassLoader().loadClass( typeValue.name );
            }
            else if ( value instanceof JArray )
            {
                Class<?> componentType = type.getComponentType();

                JArray jArray = (JArray) value;
                List<Object> results = new ArrayList<Object>();
                for ( Object item : jArray.values )
                {
                    results.add( convert( componentType, item ) );
                }

                result = results.toArray( (Object[]) Array.newInstance( componentType, results.size() ) );
            }
            else if ( value instanceof JAnnotation )
            {
                Class annotationClass = type;
                result = ( (JAnnotation) value ).get( annotationClass );
            }

            return result;
        }

    }

}
