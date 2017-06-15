package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.interfaces.IBroadcasterListener;
import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable, IBroadcasterListener {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private PrintWriter writer;
	private ObjectMapper mapper;
	private String currentUser;
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private Calendar calendar;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
		this.mapper = new ObjectMapper();
		try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				calendar = Calendar.getInstance();
				message.setTimeStamp(simpleDateFormat.format(calendar.getTime()));

				switch (message.getCommand()) {
				case "connect":
					log.info("user <{}> connected", message.getUsername());
					message.setContents(" has connected");
					Server.broadcast(message);
					Server.register(this);
					currentUser = message.getUsername();
					break;
				case "disconnect":
					log.info("user <{}> disconnected", message.getUsername());
					Server.unregister(this);
					message.setContents(" has disconnected");
					Server.broadcast(message);
					this.socket.close();
					break;
				case "echo":
					log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
					String response = mapper.writeValueAsString(message);
					writer.write(response);
					writer.flush();
					break;
				case "broadcast":
					log.info("user <{}> broadcast message <{}>", message.getUsername(), message.getContents());
					Server.broadcast(message);
					break;
				case "users":
					message.setContents(Server.getCurrentUsersOnServer());
					response = mapper.writeValueAsString(message);
					writer.write(response);
					writer.flush();					
					break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	@Override
	public synchronized void recieveMessage(Message message) {
		try {
			String response = mapper.writeValueAsString(message);
			writer.write(response);
			writer.flush();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public String getCurrentUser() {
		return currentUser;
	}

}
