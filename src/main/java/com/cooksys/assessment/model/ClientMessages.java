package com.cooksys.assessment.model;

/**
 * Commonly used messages that are sent to clients
 */
public enum ClientMessages {
	HAS_DISCONNECTED("has disconnected."),
	HAS_CONNECTED("has connected."),
	COMMAND_NOT_RECOGNIZED("Command used was not recognized. Type 'help' for supported commands."),
	HELP_COMMAND_MESSAGE("Currently supported commands are:\ndisconnect\nusers\necho (message)\nbroadcast (message)\n@username (message)\n");
	
	private String cMessage;
	private ClientMessages(String cMessage) { this.cMessage = cMessage; }
	
	public String getMessage() {
		return this.cMessage;
	}
}
