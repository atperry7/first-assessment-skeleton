package com.cooksys.assessment.model;

public class Message {

	private String username;
	private String command;
	private String contents;
	private String timeStamp;

    /**
     * @return the name of the user
     */
	public String getUsername() {
		return username;
	}

    /**
     * @param the name of the user
     */
	public void setUsername(String username) {
		this.username = username;
	}

    /**
     * @return the name of the command
     */
	public String getCommand() {
		return command;
	}
	
	/**
	 * @param the name of the command
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
    /**
     * @return the contents of the message
     */
	public String getContents() {
		return contents;
	}
	
	/**
	 * @param the contents of the message
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}

    /**
     * @return the date that is attached the message
     */
	public String getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * @param the date that is attached to the message
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

}
