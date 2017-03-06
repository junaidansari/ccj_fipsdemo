package com.demo.fips;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

/**
 * A Simple Servlet to demonstrate the issue, when changing the FIPS Mode to approved.
 *
 * @author Junaid Ansari
 */
public class SimpleServlet extends HttpServlet
{
  public static final String CCJ_FIPS_JCE_PROVIDER = "CCJ";

  protected void doGet( HttpServletRequest request_, HttpServletResponse response_ )
    throws ServletException, IOException
  {
    PrintWriter writer = response_.getWriter();

    // Load properties file to determine whether to operate in fips mode or not.
    InputStream inputStream = getServletContext().getResourceAsStream("/WEB-INF/fips.properties");
    Properties props = new Properties();
    props.load( new InputStreamReader( inputStream ) );

    String fipsEnabled = props.getProperty( "fipsEnabled" );
    String fipsProvider = props.getProperty( "fipsProvider" );
    String setFipsModeApproved = props.getProperty( "setFipsModeApproved" );

    boolean fipsModeApproved = com.safelogic.cryptocomply.crypto.CryptoServicesRegistrar.isInApprovedOnlyMode();

    if( Boolean.valueOf( fipsEnabled ) )
    {
      if( !com.safelogic.cryptocomply.crypto.fips.FipsStatus.isReady() )
      {
        throw new ServletException( "Crypto-J JCE Provider self-test failed." );
      }

      writer.println( "<p> Initial State - Is in FIPS Approved mode - CryptoServicesRegistrar.isInApprovedOnlyMode(): " +
                   com.safelogic.cryptocomply.crypto.CryptoServicesRegistrar.isInApprovedOnlyMode() + "</p>" );

      if( !fipsModeApproved )
      {
        // Switch to approved mode if specified.
        if( Boolean.valueOf( setFipsModeApproved ) )
        {
           com.safelogic.cryptocomply.crypto.CryptoServicesRegistrar.setApprovedOnlyMode( true );
        }

        // Create a Provider object & add it to top of the list of java security providers.
        Provider ccjFipsProvider = new com.safelogic.cryptocomply.jcajce.provider.CryptoComplyFipsProvider();

        int fipsIndex = getProviderIndex( CCJ_FIPS_JCE_PROVIDER );

        // If the provider is already present first remove it.
        if( (fipsIndex != -1) )
        {
          Security.removeProvider( CCJ_FIPS_JCE_PROVIDER );
        }

        Security.insertProviderAt( ccjFipsProvider, 1 );
      }
      else
      {
        writer.println( "<p> Skipping changing FIPS Approved mode to true, as approved mode is set as true. Try accessing or refreshing the page after sometime. </p>" );
      }
    }

    response_.setContentType( "text/html" );
    response_.setCharacterEncoding( "UTF-8" );

    writer.println( "<!DOCTYPE html><html>" );
    writer.println( "<head>" );

    writer.println( "<meta charset=\"UTF-8\" />" );
    writer.println( "<title>MyServlet.java:doGet(): Servlet code!</title>" );
    writer.println( "</head>" );
    writer.println( "<body>" );

    if( !fipsModeApproved )
    {
      writer.println( "<p>---------------------------------------------------------------------------------------<p>" );
      if( Boolean.valueOf( fipsEnabled ) )
      {
        writer.println( "<p> Fips Provider : " + fipsProvider + "</p>" );
      }
      writer.println( "<p> Fips Enabled : " + fipsEnabled + "</p>" );
      writer.println( "<p> Set Fips Mode Approved : " + setFipsModeApproved + "</p>" );
      writer.println( "<p>---------------------------------------------------------------------------------------<p>" );

      writer.println( "<p>---------------------------------------------------------------------------------------<p>" );
      writer.println( "<p> JVM SecurityProvider list: </p>" );

      Provider[] providers = getProviders();
      int i = 1;
      for( Provider provider : providers )
      {
        writer.println( "<p> Provider at position = " + i + " has name = " + provider.getName() + "</p>" );
        i++;
      }
      writer.println( "<p>---------------------------------------------------------------------------------------<p>" );

      if( Boolean.valueOf( fipsEnabled ) )
      {
        writer.println( "<p> Is in FIPS Approved mode - CryptoServicesRegistrar.isInApprovedOnlyMode(): " +
                         com.safelogic.cryptocomply.crypto.CryptoServicesRegistrar.isInApprovedOnlyMode() + "</p>" );
      }
    }

    writer.println( "<p>---------------------------------------------------------------------------------------<p>" );

    writer.println( "</body>" );
    writer.println( "</html>" );

  }

  public static int getProviderIndex( String providerName_ )
  {
    int providerIndex = 1;
    if( providerName_ == null )
    {
      return -1;
    }
    Provider[] providers_ = getProviders();
    for( Provider provider : providers_ )
    {
      if( providerName_.equalsIgnoreCase( provider.getName() ) )
      {
        return providerIndex;
      }
      providerIndex++;
    }
    return -1;
  }

  public static Provider[] getProviders()
  {
    return Security.getProviders();
  }
}
