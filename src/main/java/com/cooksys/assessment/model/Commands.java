package com.cooksys.assessment.model;

/**
 * List of commonly used commands on client chat server
 * <p>
 * "@" is not included due to its special nature
 */
public enum Commands {
	ECHO("echo"),
	BROADCAST("broadcast"),
	USERS("users"),
	CONNECT("connect"),
	DISCONNECT("disconnect"),
	HELP("help");
	
	
	private String command;
	private Commands(String command) { this.command = command; }
	
	public String getCommand() {
		return this.command;
	}
}
