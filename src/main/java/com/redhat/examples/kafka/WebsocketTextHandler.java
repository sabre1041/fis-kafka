package com.redhat.examples.kafka;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebsocketTextHandler extends TextWebSocketHandler {
	
	private Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<WebSocketSession>());
	private final Logger LOGGER = LoggerFactory.getLogger(WebsocketTextHandler.class);
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
		LOGGER.info("Connection Established");
	}
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		LOGGER.info("Message Received: "+message.getPayload());
		for(WebSocketSession s : sessions) {
			try {
				s.sendMessage(message);
			}
			catch(Exception e) {
				LOGGER.error("Error sending message to session " + session.getId(), e);
			}
		}
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(session);
		LOGGER.info("Connection Closed");
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		LOGGER.error("Connection Error");
		sessions.remove(session);
		session.close(CloseStatus.SERVER_ERROR);
	}

}
