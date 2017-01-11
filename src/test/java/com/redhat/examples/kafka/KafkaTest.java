package com.redhat.examples.kafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class KafkaTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CamelContext camelContext;
    
    @EndpointInject(uri="mock:websocket")
    private MockEndpoint mockEndpoint;

    @Test
    public void testSwaggerEndpoint() {
 
        ResponseEntity<String> orderResponse = restTemplate.getForEntity("/api/api-doc", String.class);
        assertThat(orderResponse.getStatusCodeValue(), equalTo(HttpStatus.SC_OK));
    }
    @Test
    public void activeDefinitionTest() throws Exception {
    	
  
        NotifyBuilder notify = new NotifyBuilder(camelContext)
            .fromRoute("rest-input")
            .whenDone(1)
            .and()
            .wereSentTo("mock:websocket")
            .whenDone(1)
            .create();
    	
    	camelContext.getRouteDefinition("kafka-input").adviceWith(camelContext, new AdviceWithRouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				interceptSendToEndpoint("ahc-ws*").skipSendToOriginalEndpoint().to("mock:websocket");
			}
		});

    	String message = "Test Message";
    	
        ResponseEntity<String> orderResponse = restTemplate.postForEntity("/api/kafka", message, String.class);
        assertThat(orderResponse.getStatusCodeValue(), equalTo(HttpStatus.SC_OK));
        
        assertThat(notify.matches(10, TimeUnit.SECONDS), Matchers.is(true));
        
        mockEndpoint.expectedMessageCount(1);
        assertThat(mockEndpoint.getReceivedExchanges().get(0).getIn().getBody(String.class), equalTo(message));
        
    }
    
    
}
