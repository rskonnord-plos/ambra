/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.trackback;

import org.ambraproject.BaseWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.struts2.ServletActionContext;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransport;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class CreatePingbackActionTest extends BaseWebTest {

  @Autowired
  protected CreatePingbackAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeMethod
  public void setUpServlet() {
    request = (MockHttpServletRequest) ServletActionContext.getRequest();
    VirtualJournalContext dummyVjc = new VirtualJournalContext(null, null, "http", 80, "localhost", "", null);
    request.setAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, dummyVjc);
    response = new MockHttpServletResponse();
    ServletActionContext.setResponse(response);
  }


  private static final String SAMPLE_VALID_URI = "http://localhost/";

  private static String createMockXmlrpcBody(String methodName, Object[] params) {
    StringBuilder sb = new StringBuilder(0x100)
        .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        .append("<methodCall>\n")
        .append("  <methodName>").append(methodName).append("</methodName>\n")
        .append("  <params>\n");
    for (Object param : params) {
      sb.append("    <param><value>").append(param).append("</value></param>\n");
    }
    sb
        .append("  </params>\n")
        .append("</methodCall>\n");
    return sb.toString();
  }

  private static class MockResponseReader extends XmlRpcSunHttpTransport {
    public MockResponseReader() {
      super(new XmlRpcClient());
      getClient().setConfig(new XmlRpcClientConfigImpl());
    }

    public Object readMockResponse(MockHttpServletResponse httpResponse) throws IOException {
      String content = httpResponse.getContentAsString();
      InputStream stream = new ByteArrayInputStream(content.getBytes());
      try {
        return readResponse((XmlRpcStreamRequestConfig) getClient().getClientConfig(), stream);
      } catch (XmlRpcException e) {
        return e;
      }
    }
  }

  private void putXmlAsRequestContent(String xml) {
    request.setContent(xml.getBytes());
    request.addHeader(CreatePingbackAction.CONTENT_TYPE_HEADER, CreatePingbackAction.XML_CONTENT_TYPE);
    request.setContentType(CreatePingbackAction.XML_CONTENT_TYPE);
  }


  @Test
  public void testExecute() throws Exception {
    String body = createMockXmlrpcBody(CreatePingbackAction.PINGBACK_METHOD_NAME,
        new String[]{SAMPLE_VALID_URI, SAMPLE_VALID_URI});
    putXmlAsRequestContent(body);
    action.execute();

    Object rpcResult = new MockResponseReader().readMockResponse(response);
  }

  @DataProvider(name = "invalidXmlRpc")
  public Object[][] getInvalidXmlRpc() {
    List<Object[]> testData = new ArrayList<Object[]>();

    testData.add(new Object[]{"someOtherRpcMethod", new Object[0], PingbackFault.METHOD_NOT_FOUND, "invalid RPC method name"});
    testData.add(new Object[]{CreatePingbackAction.PINGBACK_METHOD_NAME, new Object[]{"This is not a URI", SAMPLE_VALID_URI},
        PingbackFault.SOURCE_DNE, "invalid source URI"});
    testData.add(new Object[]{CreatePingbackAction.PINGBACK_METHOD_NAME, new Object[]{SAMPLE_VALID_URI, "This is not a URI either"},
        PingbackFault.TARGET_DNE, "invalid target URI"});

    final int validNumberOfParameters = 2;
    for (int i = 0; i < 5; i++) {
      if (i != validNumberOfParameters) {
        Object[] params = Collections.nCopies(i, SAMPLE_VALID_URI).toArray();
        String description = i + " parameter" + (i == 1 ? "" : "s") + " (valid: " + validNumberOfParameters + " parameters)";
        testData.add(new Object[]{CreatePingbackAction.PINGBACK_METHOD_NAME, params, PingbackFault.INVALID_PARAMS, description});
      }
    }

    return testData.toArray(new Object[testData.size()][]);
  }

  @Test(dataProvider = "invalidXmlRpc")
  public void testInvalidXmlRpc(String methodName, Object[] params, PingbackFault expected, String description)
      throws Exception {
    String body = createMockXmlrpcBody(methodName, params);
    putXmlAsRequestContent(body);
    action.execute();
    Object rpcResult = new MockResponseReader().readMockResponse(response);
    assertTrue(rpcResult instanceof XmlRpcException, "RPC didn't halt on " + description);
    int faultCode = ((XmlRpcException) rpcResult).code;
    assertEquals(faultCode, expected.getCode(), "RPC didn't return expected fault code from " + description);
  }


}
