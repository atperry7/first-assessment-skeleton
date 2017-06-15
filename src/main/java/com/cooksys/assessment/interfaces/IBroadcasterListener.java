package com.cooksys.assessment.interfaces;

import com.cooksys.assessment.model.Message;

/**
 * Interface is used to communicate with all chat clients connected
 */
public interface IBroadcasterListener {
	/**
	 * Allows the user to receive a broadcasted message
	 * @param message Takes the message object to be sent across all clients
	 */
	public void receiveMessage(Message message);
	
	/**
	 * Allows the server to find out what user is currently connected to the client
	 * @return String of the current user
	 */
	public String getCurrentUser();
}
