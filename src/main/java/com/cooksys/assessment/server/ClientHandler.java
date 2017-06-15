package com.cooksys.assessment.server;

import java.io.BufferedReader;
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
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private Calendar calendar;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
		this.mapper = new ObjectMapper();
	}

	public void run() {
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				String lastCommand = message.getCommand();
				
				if (message.getCommand().isEmpty() || message.getCommand() != null) {
					message.setCommand(lastCommand);
				}
				
				//This attaches a time stamp based on when the server receives the message
				calendar = Calendar.getInstance();
				message.setTimeStamp(simpleDateFormat.format(calendar.getTime()));

				switch (message.getCommand()) {
				case "connect":
					log.info("user <{}> connected", message.getUsername());
					message.setContents("has connected");
					Server.broadcast(message);
					Server.register(this);
					currentUser = message.getUsername();
					break;
				case "disconnect":
					log.info("user <{}> disconnected", message.getUsername());
					Server.unregister(this);
					message.setContents("has disconnected");
					Server.broadcast(message);
					this.socket.close();
					break;
				case "echo":
					log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
					writeToClient(message);
					break;
				case "broadcast":
					log.info("user <{}> broadcast message <{}>", message.getUsername(), message.getContents());
					Server.broadcast(message);
					break;
				case "users":
					log.info("user <{}> requested currently connected users", message.getUsername());
					message.setContents(Server.getCurrentUsersOnServer());
					writeToClient(message);						
					break;
				default:
					if (message.getCommand().startsWith("@")) {
						String userToMessage = message.getCommand().substring(1);
						message.setCommand("whisper");
						Server.whisper(message, userToMessage);
					} else {
						message.setCommand("Command used was not recognized");
						writeToClient(message);
					}
				
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	private void writeToClient(Message message) throws JsonProcessingException {
		String response = mapper.writeValueAsString(message);
		writer.write(response);
		writer.flush();		
		
	}

	@Override
	public synchronized void recieveMessage(Message message) {
		try {
			writeToClient(message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public String getCurrentUser() {
		return currentUser;
	}

}
