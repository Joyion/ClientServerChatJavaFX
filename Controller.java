package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class Controller {
	
	@FXML
	Text userLabel;
	
	@FXML 
	TextArea textDisplay;
	
	@FXML 
	TextArea textSend;
	
	@FXML 
	Button sendButton;
	
	Text sendMessage;
	
	Socket connectionSocket;
	Receiver receiveService;
	
	
	public void initialize() {
		System.out.println("HELLO");
		
		 Task<Integer> startNetwork = new Task<Integer>() {
			 Socket mysocket;
	         @Override protected Integer call() {
	        	 try {
	        		 mysocket = new Socket("localhost", 5000);
	    			System.out.println("Connected to Server");
	        	 }
	        	 catch(Exception e) {
	        		 System.out.println(e.getMessage());
	        	 }
	             return 200;
	         }

	         @Override 
	         protected void succeeded() {
	        	if(mysocket.isConnected()) {
	         connectionSocket = mysocket;
	           receiveService = new Receiver(connectionSocket);
	           textDisplay.textProperty().bind(receiveService.messageProperty());
	          userLabel.textProperty().bind(receiveService.titleProperty());
	           receiveService.start();
	       
	        	}
	  
	         }
	         
	         @Override protected void failed() {
				    super.failed();
				   
				    }

	      
	     };
	     
	     new Thread(startNetwork).start();
	// end of initialize 
	}
	
	@FXML
	public void sendMessage() {
		String text = textSend.getText();
		textSend.clear();
		
		Task<Integer> messager = new Task <Integer>() {
			@Override
			protected Integer call() {
				try {
					
					PrintWriter sender = new PrintWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
					sender.println("Client: " + text);
					sender.flush();
					}
					catch(Exception e) {
						
					}
				return null;
			}
			
			@Override protected void failed() {
			    super.failed();

			    
			    
			    }
		};
		new Thread(messager).start();
// end of send message 
	}

	
// end of controller class	
}



class Receiver extends Service<String> {
	Socket socket;

	StringBuilder display = new StringBuilder();
	
	public Receiver(Socket s) {
		socket = s;
	}
		
   @Override
   protected Task<String> createTask() {
       return new Task<String>() {
    	  
           @Override
           protected String call() throws Exception {
        	   
        	BufferedReader receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	updateTitle("Client: Connected");
        	while(socket.isConnected()) {
        		String message;
        		
        		while((message = receiver.readLine()) != null) {
        			
        			
        			if(message.equalsIgnoreCase("QUIT")) {
        				socket.close();
        				break;
        			}
        			
        			else {
        			display.append(message + "\n");
        			updateMessage(display.toString());
        			
        			if(message.startsWith("Server")) {
        			PrintWriter sender = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					sender.println(message);
					sender.flush();
        			}
        			
        			
        			
        			
        		}
        	
        		}
        		
        		
        	}
        	display.append("Server has stopped. Connection Lost");
        	updateTitle("Client: Disconnected");
        	updateMessage(display.toString());
        	return "Server has stopped";
          
           }
           
           @Override protected void failed() {
			    super.failed();
			    String transcript = getMessage();
			    updateMessage(transcript + "\n" + "*Unable to Connect to Server. Please restart Application*");
			    }
       };
   }
}
// end of Receiver class
