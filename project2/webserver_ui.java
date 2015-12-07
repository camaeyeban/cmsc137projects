/**
	Author: Yeban, Erica Mae M.
	Section: CD - 2L
	File Name: webserver_ui.java
**/

// import statements
import javax.swing.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;

// class declaration for the mini-webserver's UI
public class webserver_ui extends JFrame {
	// UI elements declaration
	JPanel panel = new JPanel();
	JScrollPane scroll_pane = new JScrollPane();
	JTextArea output = new JTextArea();
	static Integer listen_port = null;

	// class constructor
	public webserver_ui() {
		try {
			setup_ui();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// UI set up
	private void setup_ui() throws Exception {
		output.setBackground(new Color(16, 12, 66));
		output.setForeground(new Color(151, 138, 255));
		output.setBorder(BorderFactory.createLoweredBevelBorder());
		output.setToolTipText("");
		output.setEditable(false);
		output.setColumns(50);
		output.setRows(30);
		
		this.setTitle("CMSC 137 Project 2 (Mini-webserver)");
		
		// window closing handler
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				this_windowClosed(e);
			}
		});
		
		// ui elements positioning
		scroll_pane.getViewport().add(output);
		panel.add(scroll_pane);
		this.getContentPane().add(panel, BorderLayout.EAST);

		// other ui options set up
		this.setVisible(true);
		this.setSize(573, 525);
		this.setResizable(false);
		this.validate();
		
		// creates the server
		new server(listen_port.intValue(), this);
	}

	// exits program when "X" is pressed.
	void this_windowClosed(WindowEvent e) {
		System.exit(1);
	}

	// shows messages from server to UI window
	public void send_message_to_window(String s) {
		output.append(s);
		output.setCaretPosition(output.getCaretPosition()+s.length());
	}

	// starts this class if run
	public static void main(String[] args) {
		// uses argument of main for what port to start on
		try {
			listen_port = new Integer(args[0]);
		}
		// if no port is specified, use port 80 (default)
		catch (Exception e) {
			listen_port = new Integer(80);
		}
		
		// creates an instance of this class
		webserver_ui ui = new webserver_ui();
	}

} // class ends
