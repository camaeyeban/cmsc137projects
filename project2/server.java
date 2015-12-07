/**
	Author: Yeban, Erica Mae M.
	Section: CD - 2L
	File Name: server.java
**/

// import statements
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 

// declares the class for the mini-webserver
public class server extends Thread {
	// declares class attributes
	private webserver_ui ui;
	private int port;

	// constructs class
	public server(int listen_port, webserver_ui to_whom_message_is_sent) {
		ui = to_whom_message_is_sent;
		port = listen_port;

		// starts thread
		this.start();
	}

	// prints message to UI
	private void s(String s2) {
		ui.send_message_to_window(s2);
	}
	
	// handles HTTP requests
	private void http_handler(BufferedReader input, DataOutputStream output) {
		// declares local variables
		int method = 0;
		String http = new String();
		String path = new String();
		String file = new String();
		String user_agent = new String();
		String line = new String();
		byte [] encoded_line;
		
		try {
			// reads from the stream
			String temp = input.readLine();
			String temp2 = new String(temp);
			
			// converts HTTP header to upper case
			temp.toUpperCase();
			
			// sets method: (1) get, (2) head, (0) not supported
			if (temp.startsWith("GET")) {
				method = 1;
			}
			if (temp.startsWith("HEAD")) {
				method = 2;
			}
			if (method == 0) {
				try {
					output.writeBytes(construct_http_header(501, 0));
					output.close();
					return;
				}
				// catches any errors then prints it to UI window
				catch (Exception e3) {
					s("Error: " + e3.getMessage());
				}
			}
			
			// gets path request
			int start = 0;
			int end = 0;
			for (int i = 0; i < temp2.length(); i++) {
				if (temp2.charAt(i) == ' ' && start != 0) {
					end = i;
					break;
				}
				if (temp2.charAt(i) == ' ' && start == 0) {
					start = i;
				}
			}
			path = temp2.substring(start + 2, end);
		}
		// catches any exception
		catch (Exception e) {
			s("Error: " + e.getMessage());
		}

		// print to UI the path of the requested file
		s("Client requested: " + new File(path).getAbsolutePath() + "\n");
		FileInputStream requested_file = null;

		// tries to open the file at the specified path
		try {
			requested_file = new FileInputStream(path);
		}
		// produces HTTP status code 404 if opening the file was unsuccessful
		catch (Exception e) {
			try {
				output.writeBytes(construct_http_header(404, 0));
				output.close();
			}
			catch (Exception e2) {}
			s("Error: " + e.getMessage());
		}
		
		try {
			int type_is = 0;
			// sets the file extension of the requested file
			if (path.endsWith(".zip") ) {
				type_is = 3;
			}
			if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
				type_is = 1;
			}
			if (path.endsWith(".gif")) {
				type_is = 2;      
			}
			if (path.endsWith(".ico")) {
				type_is = 3;
			}
			
			// HTTP status code = 200 OK
			output.writeBytes(construct_http_header(200, 5));
	
			// creates a file scanner
			Scanner scanner = new Scanner(new File(path));
			
			// creates patterns matching CSS and script declarations
			Pattern css_pattern = Pattern.compile("(\\<link)(.*?)(href=\")(.*)(\".*?\\>)");
			Pattern script_pattern = Pattern.compile("(\\<script)(.*?)(src=\")(.*)(\")(.*?\\>.*?\\<\\/script\\>)");
			
			// creates lists where CSS and script file names could be stored
			List<String> list_of_css = new ArrayList<String>();
			List<String> list_of_script = new ArrayList<String>();

			// if the request is GET, add body; otherwise, skip
			if (method == 1) {
				byte [] buffer = new byte[1024];
				while (true) {
					// read the file from file stream
					int b = requested_file.read(buffer, 0,1024);
					
					// if the end of file is reached
					if (b == -1) {
						break;
					}
					// print the read file through the client output stream on a byte per byte base
					output.write(buffer,0,b);
				}
			}
			
			// traverses the requested file
			while(scanner.hasNextLine()){
				line = scanner.nextLine();
				
				// gets the path of the referenced CSS files then adds them to the CSS list
				Matcher css_matcher = css_pattern.matcher(line);
				while(css_matcher.find()){
					int end = 0;
					for (int i = 0; i < css_matcher.group(4).length(); i++) {
						if (css_matcher.group(4).charAt(i) == '"') {
							end = i;
							break;
						}
					}
					if(end!=0){
						list_of_css.add(css_matcher.group(4).substring(0, end));
					}
					else{
						list_of_css.add(css_matcher.group(4));
					}
				}
				
				// gets the path of the referenced script files then adds them to the script list
				Matcher script_matcher = script_pattern.matcher(line);
				while(script_matcher.find()){
					int end = 0;
					for (int i = 0; i < script_matcher.group(4).length(); i++) {
						if (script_matcher.group(4).charAt(i) == '"') {
							end = i;
							break;
						}
					}
					if(end!=0){
						list_of_script.add(script_matcher.group(4).substring(0, end));
					}
					else{
						list_of_script.add(script_matcher.group(4));
					}
				}
			}
			
			// loads the CSS files in the CSS list
			for(String css_file : list_of_css){
				if(css_file.endsWith(".css")){
					Scanner css_scanner = new Scanner(new File(css_file));
					
					// appends the content of each CSS files to the requested web page
					line = new String("<style>");
					encoded_line = line.getBytes("UTF-8");
					output.write(encoded_line);
					while(css_scanner.hasNextLine()){
						line = css_scanner.nextLine();
						encoded_line = line.getBytes("UTF-8");
						output.write(encoded_line);
					}
					line = new String("</style>");
					encoded_line = line.getBytes("UTF-8");
					output.write(encoded_line);
				}
			}
			
			// loads the script files in the script list
			for(String script_file : list_of_script){
				if(script_file.endsWith(".js")){
					Scanner script_scanner = new Scanner(new File(script_file));
					
					// appends the content of the script files to the requested web page
					line = new String("<script>");
					encoded_line = line.getBytes("UTF-8");
					output.write(encoded_line);
					while(script_scanner.hasNextLine()){
						line = script_scanner.nextLine();
						encoded_line = line.getBytes("UTF-8");
						output.write(encoded_line);
					}
					line = new String("</script>");
					encoded_line = line.getBytes("UTF-8");
					output.write(encoded_line);
				}
			}
			
			// creates table for the keys and values of the query then append it to the bottom of the requested page
			line = new String("<table class ='key_value_pairs' width='100%'>");
			encoded_line = line.getBytes("UTF-8");
			output.write(encoded_line);
			
			while(line!=null){
				// read from the stream
				line = input.readLine();
				
				int colon_index = 0;
				for (int i = 0; i < line.length(); i++) {
					if (line.charAt(i) == ':') {
						colon_index = i;
						break;
					}
				}
				if(colon_index == 0){
					break;
				}
				String key = line.substring(0, colon_index);
				String value = line.substring(colon_index+1, line.length());
				
				// creates a row containing the key (1st column) and the value (2nd column) of the query
				line = new String("<tr><td>"+key+"</td><td>"+value+"</td></tr>");
				encoded_line = line.getBytes("UTF-8");
				output.write(encoded_line);
			}
			
			// styles the table
			line = new String("</table> <style> table, th, td { border: 1px solid black; border-collapse: collapse; padding: 10px; } </style>");
			encoded_line = line.getBytes("UTF-8");
			output.write(encoded_line);
			
			line = new String("</body></html>");
			encoded_line = line.getBytes("UTF-8");
			output.write(encoded_line);
			
			// close open handles
			output.close();
			requested_file.close();
		}

		catch (Exception e) {}

	}

	// constructs HTTP header for the response
	private String construct_http_header(int return_code, int file_type) {
		String s = "HTTP/1.0 ";	// response string
		
		// append http status code to string s
		switch (return_code) {
			case 200:
				s = s + "200 OK";
				break;
			case 400:
				s = s + "400 Bad Request";
				break;
			case 403:
				s = s + "403 Forbidden";
				break;
			case 404:
				s = s + "404 Not Found";
				break;
			case 500:
				s = s + "500 Internal Server Error";
				break;
			case 501:
				s = s + "501 Not Implemented";
				break;
		}

		// append other header fields
		s = s + "\r\n";
		s = s + "Connection: close\r\n";
		s = s + "Server: Mini-webserver v0\r\n";

		// append content-type
		switch (file_type) {
			case 0:
				break;
			case 1:
				s = s + "Content-Type: image/jpeg\r\n";
				break;
			case 2:
				s = s + "Content-Type: image/gif\r\n";
				break;
			case 3:
				s = s + "Content-Type: application/x-zip-compressed\r\n";
				break;
			case 4:
				s = s + "Content-Type: image/x-icon\r\n";
				break;
			default:
				s = s + "Content-Type: text/html\r\n";
				break;
		}

		// ends http header response then return response
		s = s + "\r\n";
		return s;
	}

	// starts thread
	public void run() {
		ServerSocket serversocket = null;
		
		// prints welcome message to UI
		s("----------------------------------------------------------------------------------\n" +
			"   Good day!\n   This is a mini-server that serves as CMSC 137 Project 2.\n" +
			"----------------------------------------------------------------------------------\n\n");
		
		// makes a server socket and binds it to the specified port
		try {
			s("Trying to bind to localhost on port " + Integer.toString(port) + "...");
			serversocket = new ServerSocket(port);
		}
		// catches errors and prints them to UI
		catch (Exception e) {
			s("\nFatal Error:" + e.getMessage());
			return;
		}
		
		s("OK!\n");
		/*
			goes in a infinite loop:
				(1) wait for connections
				(2) process request
				(3) send response
		*/
		while (true) {
			s("\nReady. Waiting for requests...\n\n");
			
			try {
				// waits/blocks until someone connects to the specified port
				Socket connectionsocket = serversocket.accept();
				
				// figures out what IP address the client comes from then prints it to UI
				InetAddress client = connectionsocket.getInetAddress();
				s(client.getHostName() + " connected to server.\n");
				
				// reads the http request of the client from the socket interface into a buffer
				BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
				
				// prepares an output stream from server to client for sending response
				//(header + requested file) to the client.
				DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
				
				// handles clients' http requests
				http_handler(input, output);
			}
			// catches errors and prints them to UI
			catch (Exception e) {
				s("\nError:" + e.getMessage());
			}

		} // loops back
	}

} // class ends
