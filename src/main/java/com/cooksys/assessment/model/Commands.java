package com.cooksys.assessment.model;

/**
 * List of commonly used commands on client chat server for communicating with other users.
 * Plus some basic helper commands
 * <p>
 * "@", connect and disconnect are not included due to their special nature
 */
public enum Commands {
	ECHO("echo"),
	BROADCAST("broadcast"),
	USERS("users"),
	HELP("help");
	
	
	private String command;
	private Commands(String command) { this.command = command; }
	
	public String getCommand() {
		return this.command;
	}
}
