package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.interfaces.IBroadcasterListener;
import com.cooksys.assessment.model.Message;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private static ExecutorService executor;
	private static LinkedList<IBroadcasterListener > listeners = new LinkedList<>();

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
	
	public static synchronized void broadcast(Message message) {
		for (final IBroadcasterListener  listener : listeners)
			executor.execute(new Runnable() {
				@Override
				public void run() {
					listener.recieveMessage(message);
				}
			});
	}
	

	public static synchronized void register(IBroadcasterListener  listener) {
		listeners.add(listener);
	}

	public static synchronized void unregister(IBroadcasterListener  listener) {
		listeners.remove(listener);
	}


}
