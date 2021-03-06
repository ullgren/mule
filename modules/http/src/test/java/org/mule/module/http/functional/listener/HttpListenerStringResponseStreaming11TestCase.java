/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.http.functional.listener;

import static org.apache.http.HttpVersion.HTTP_1_1;

import org.apache.http.HttpVersion;
import org.junit.Test;

public class HttpListenerStringResponseStreaming11TestCase extends HttpListenerResponseStreamingTestCase
{

  @Override
  protected HttpVersion getHttpVersion()
  {
    return HTTP_1_1;
  }

  @Override
  protected String getConfigFile()
  {
    return "http-listener-string-response-streaming-config.xml";
  }

  // AUTO

  @Test
  public void string() throws Exception
  {
    final String url = getUrl("string");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithContentLengthHeader() throws Exception
  {
    final String url = getUrl("stringWithContentLengthHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithContentLengthOutboundProperty() throws Exception
  {
    final String url = getUrl("stringWithContentLengthOutboundProperty");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithTransferEncodingHeader() throws Exception
  {
    final String url = getUrl("stringWithTransferEncodingHeader");
    testResponseIsChunkedEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithTransferEncodingOutboundProperty() throws Exception
  {
    final String url = getUrl("stringWithTransferEncodingOutboundProperty");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithTransferEncodingAndContentLengthHeader() throws Exception
  {
    final String url = getUrl("stringWithTransferEncodingAndContentLengthHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithTransferEncodingAndContentLengthOutboundProperty() throws Exception
  {
    final String url = getUrl("stringWithTransferEncodingAndContentLengthOutboundProperty");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithTransferEncodingHeaderAndContentLengthOutboundProperty() throws Exception
  {
    final String url = getUrl("stringWithTransferEncodingHeaderAndContentLengthOutboundProperty");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithTransferEncodingOutboundPropertyAndContentLengthHeader() throws Exception
  {
    final String url = getUrl("stringWithTransferEncodingOutboundPropertyAndContentLengthHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  // NEVER

  @Test
  public void neverString() throws Exception
  {
    final String url = getUrl("neverString");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void neverStringTransferEncodingHeader() throws Exception
  {
    final String url = getUrl("neverStringTransferEncodingHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void neverStringTransferEncodingOutboundProperty() throws Exception
  {
    final String url = getUrl("neverStringTransferEncodingOutboundProperty");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  // ALWAYS

  @Test
  public void alwaysString() throws Exception
  {
    final String url = getUrl("alwaysString");
    testResponseIsChunkedEncoding(url, getHttpVersion());
  }

  @Test
  public void alwaysStringContentLengthHeader() throws Exception
  {
    final String url = getUrl("alwaysStringContentLengthHeader");
    testResponseIsChunkedEncoding(url, getHttpVersion());
  }

  @Test
  public void alwaysStringContentLengthOutboundProperty() throws Exception
  {
    final String url = getUrl("alwaysStringContentLengthOutboundProperty");
    testResponseIsChunkedEncoding(url, getHttpVersion());
  }

}
