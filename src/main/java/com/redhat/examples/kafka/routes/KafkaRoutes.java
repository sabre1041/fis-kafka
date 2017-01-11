package com.redhat.examples.kafka.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaRoutes extends RouteBuilder {

	@Value("${camel.route.sendWebsocketIn}")
	private String websocketRouteIn;
	
	@Value("${camel.route.sendRestOut}")
	private String restRouteOut;
	
	@Override
	public void configure() throws Exception {
	
        restConfiguration()
        .contextPath("/api").apiContextPath("/api-doc")
            .apiProperty("api.title", "Fuse Integration Services Kafka Demo REST API")
            .apiProperty("host", "")
            .apiProperty("api.version", "1.0")
            .apiProperty("cors", "true")
            .apiContextRouteId("doc-api")
        .component("servlet");

        rest("/kafka").description("Kafka Services")

        .post("/").description("Send a message to Kafka").param().name("body").type(RestParamType.body).dataType("string").endParam()
            .route().routeId("rest-input")
            .convertBodyTo(String.class)
            .setHeader(KafkaConstants.KEY, constant("{{kafka.key}}"))
            .to(restRouteOut);
        
        from(websocketRouteIn).id("kafka-input")
        	.log("Kafka Message Received: ${body}")
        	.to("ahc-ws:localhost:{{server.port}}/websocket");

	}

}
