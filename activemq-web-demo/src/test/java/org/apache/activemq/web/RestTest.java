/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.web;

import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

public class RestTest extends JettyTestSupport {
    private static final Log LOG = LogFactory.getLog(RestTest.class);
	
	public void testConsume() throws Exception {
	    producer.send(session.createTextMessage("test"));
	    LOG.info("message sent");
	    
	    HttpClient httpClient = new HttpClient();
        httpClient.start();
        ContentExchange contentExchange = new ContentExchange();
        httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        contentExchange.setURL("http://localhost:8080/message/test?readTimeout=1000&type=queue");
        httpClient.send(contentExchange);
        contentExchange.waitForDone();
        assertEquals("test", contentExchange.getResponseContent());	    
	}
	
	public void testSubscribeFirst() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        ContentExchange contentExchange = new ContentExchange();
        httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        contentExchange.setURL("http://localhost:8080/message/test?readTimeout=5000&type=queue");
        httpClient.send(contentExchange);
        
        Thread.sleep(1000);
        
        producer.send(session.createTextMessage("test"));
        LOG.info("message sent");
        
        contentExchange.waitForDone();
        assertEquals("test", contentExchange.getResponseContent());	    
	}
	
	public void testSelector() throws Exception {
	    TextMessage msg1 = session.createTextMessage("test1");
	    msg1.setIntProperty("test", 1);
	    producer.send(msg1);
	    LOG.info("message 1 sent");
	    
	    TextMessage msg2 = session.createTextMessage("test2");
	    msg2.setIntProperty("test", 2);
	    producer.send(msg2);
	    LOG.info("message 2 sent");
	    
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        ContentExchange contentExchange = new ContentExchange();
        httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        contentExchange.setURL("http://localhost:8080/message/test?readTimeout=1000&type=queue");
        contentExchange.setRequestHeader("selector", "test=2");
        httpClient.send(contentExchange);
        contentExchange.waitForDone();
        assertEquals("test2", contentExchange.getResponseContent());
	}

}
