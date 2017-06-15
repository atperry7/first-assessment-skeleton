package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.interfaces.IBroadcasterListener;
import com.cooksys.assessment.model.Message;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);

	private int port;
	private static ExecutorService executor;
	
	//Used to keep track of currently connected clients
	private static LinkedList<IBroadcasterListener> listeners = new LinkedList<>();

	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		Server.executor = executor;
	}

	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket);
				executor.execute(handler);

			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	/**
	 * Adds a client to listen for broadcasted messages
	 * @param listener the client to be added
	 */
	public static synchronized void register(IBroadcasterListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Remove a client from listening to the broadcasted messages
	 * @param listener the client to be removed
	 */
	public static synchronized void unregister(IBroadcasterListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Goes through the currently registered clients on the server and broadcasts a message to them
	 * @param message the Message object to be broadcasted to other users
	 */
	public static synchronized void broadcast(Message message) {
		listeners.parallelStream()
				 .forEach(listener -> executor.execute(() -> listener.receiveMessage(message)));
	}
	
	/**
	 * Goes through and attempts to find the user to send a private message to
	 * @param message the Message object to be sent in a private message to a client
	 * @param userName the name of the user to send a private message to
	 */
	public static synchronized void whisper(Message message, String userName) {
		listeners.stream()
				 .filter(listener -> listener.getCurrentUser().equals(userName))
				 .forEach(listener -> executor.execute(() -> { listener.receiveMessage(message); }));
	}
	
	/**
	 * Used to obtain a current list of users on the chat server
	 * @return String containing all the connected users
	 */
	public static synchronized String getCurrentUsersOnServer() {
		String contents = "";
		for (IBroadcasterListener iBroadcasterListener : listeners) {
			contents += "<" + iBroadcasterListener.getCurrentUser() + ">" + "\n";
		}
		return contents;
	}
	
	/**
	 * Checks for user by there username 
	 * @param username username to check the server for
	 * @return true or false based on the finding
	 */
	public static boolean checkForUser(String username) {
		for (IBroadcasterListener listener : listeners) {
			if (username.equals(listener.getCurrentUser())) {
				return true;
			}
		}	
		return false;
	}
}
