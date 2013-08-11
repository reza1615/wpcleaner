/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Manage interactions with the tool server.
 */
public class ToolServer {

  private final Log log = LogFactory.getLog(ToolServer.class);

  /**
   * HTTP Client.
   */
  private final HttpClient httpClient;

  /**
   * Create a ToolServer object.
   * 
   * @param httpClient HTTP client.
   */
  ToolServer(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Send a POST request to the Tool Server.
   * 
   * @param path Path on the tool server.
   * @param properties Request properties.
   * @param manager Response manager.
   * @throws APIException
   */
  void sendPost(
      String              path,
      Map<String, String> properties,
      ResponseManager     manager) throws APIException {
    HttpMethod method = null;
    InputStream inputStream = null;
    try {
      String url = "http://toolserver.org/" + path;
      method = HttpUtils.createHttpMethod(url, properties, false);
      int statusCode = httpClient.executeMethod(method);
      inputStream = method.getResponseBodyAsStream();
      inputStream = new BufferedInputStream(inputStream);
      Header contentEncoding = method.getResponseHeader("Content-Encoding");
      if (contentEncoding != null) {
        if (contentEncoding.getValue().equals("gzip")) {
          inputStream = new GZIPInputStream(inputStream);
        }
      }
      if ((statusCode == HttpStatus.SC_OK) && (manager != null)) {
        manager.manageResponse(inputStream);
      }
      try {
        while (inputStream.read() >= 0) {
          //
        }
      } catch (IOException e) {
        //
      }
      if (statusCode != HttpStatus.SC_OK) {
        throw new APIException("URL access returned " + HttpStatus.getStatusText(statusCode));
      }
    } catch (HttpException e) {
      throw new APIException("HttpException: " + e.getMessage());
    } catch (IOException e) {
      throw new APIException("IOException: " + e.getMessage());
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn("Error closing stream: " + e.getMessage());
        }
      }
      if (method != null) {
        method.releaseConnection();
      }
    }
  }

  /**
   * Send a GET request to the Tool Server.
   * 
   * @param path Path on the tool server.
   * @param manager Response manager.
   * @throws APIException
   */
  public void sendGet(
      String          path,
      ResponseManager manager) throws APIException {
    HttpMethod method = null;
    InputStream inputStream = null;
    try {
      String url = "http://toolserver.org/" + path;
      method = HttpUtils.createHttpMethod(url, null, true);
      int statusCode = httpClient.executeMethod(method);
      if (statusCode == HttpStatus.SC_NOT_FOUND) {
        return;
      }
      inputStream = method.getResponseBodyAsStream();
      inputStream = new BufferedInputStream(inputStream);
      Header contentEncoding = method.getResponseHeader("Content-Encoding");
      if (contentEncoding != null) {
        if (contentEncoding.getValue().equals("gzip")) {
          inputStream = new GZIPInputStream(inputStream);
        }
      }
      if ((statusCode == HttpStatus.SC_OK) && (manager != null)) {
        manager.manageResponse(inputStream);
      }
      try {
        while (inputStream.read() >= 0) {
          //
        }
      } catch (IOException e) {
        //
      }
      if (statusCode != HttpStatus.SC_OK) {
        throw new APIException("URL access returned " + HttpStatus.getStatusText(statusCode));
      }
    } catch (HttpException e) {
      throw new APIException("HttpException: " + e.getMessage());
    } catch (IOException e) {
      throw new APIException("IOException: " + e.getMessage());
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn("Error closing stream: " + e.getMessage());
        }
      }
      if (method != null) {
        method.releaseConnection();
      }
    }
  }
}
