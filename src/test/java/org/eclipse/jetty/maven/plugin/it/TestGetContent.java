//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.maven.plugin.it;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class TestGetContent
{
    @Test
    public void get_content_response()
        throws Exception
    {
        int port = getPort();
        Assert.assertTrue( port > 0 );
        HttpClient httpClient = new HttpClient();
        try
        {
            httpClient.start();

            if (Boolean.getBoolean( "helloServlet" ))
            {
                String response = httpClient.GET( "http://localhost:" + port + "/hello?name=beer" ).getContentAsString();
                Assert.assertEquals( "Hello beer", response.trim() );
                response = httpClient.GET( "http://localhost:" + port + "/hello?name=foo" ).getContentAsString();
                Assert.assertEquals( "Hello foo", response.trim() );
                System.out.println( "helloServlet" );
            }
            if (Boolean.getBoolean( "pingServlet" ))
            {
                System.out.println( "pingServlet ok" );
                String response = httpClient.GET( "http://localhost:" + port + "/ping?name=beer" ).getContentAsString();
                Assert.assertEquals( "pong beer", response.trim() );
                System.out.println( "pingServlet" );
            }
            String contentCheck = System.getProperty( "contentCheck" );
            if(StringUtils.isNotBlank( contentCheck ) )
            {
                String response = httpClient.GET( "http://localhost:" + port ).getContentAsString();
                Assert.assertTrue( "response not contentCheck: " + contentCheck + ", response:" + response, //
                                   response.contains( contentCheck ) );
                System.out.println( "contentCheck" );
            }
        }
        finally
        {
            httpClient.stop();
        }
    }


    public static int getPort()
        throws Exception
    {
        int attempts = 70;
        int port = -1;
        String s = System.getProperty( "jetty.port.file" );
        Assert.assertNotNull( s );
        Path p = Paths.get( s );
        while ( true )
        {
            if ( Files.exists(p) )
            {
                try (Reader r = Files.newBufferedReader( p ); LineNumberReader lnr = new LineNumberReader( r );)
                {
                    s = lnr.readLine();
                    Assert.assertNotNull( s );
                    port = Integer.parseInt( s.trim() );
                }
                break;
            }
            else
            {
                if ( --attempts < 0 )
                {
                    break;
                }
                else
                {
                    Thread.currentThread().sleep( 1000 );
                }
            }
        }
        return port;
    }
}
