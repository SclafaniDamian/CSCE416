// Made By Damian Sclafani

/*
 * Psuedocode for implementation of a group chat server in java
 * By Srihari Nelakuditi for CSCE 416
 */

// Package for I/O related stuff
import java.io.*;

// Package for socket related stuff
import java.net.*;

// Package for list related stuff
import java.util.*;

/*
 * This class does all the group chat server's job
 *
 * It consists of parent thread (code inside main method) which accepts
 * new client connections and then spawns a thread per connection
 *
 * Each child thread (code inside run method) reads messages
 * from its socket and relays the message to the all active connections
 *
 * Since a thread is being created with this class object,
 * this class declaration includes "implements Runnable"
 */
public class GroupChatServer implements Runnable
{
	// Each instance has a separate socket
	private Socket clientSock;

	// The class keeps track of active clients
	private static List<PrintWriter> clientList;

	// Constructor sets the socket for the child thread to process
	public GroupChatServer(Socket sock)
	{
		clientSock = sock;
	}

	// Add the given client to the active clients list
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized boolean addClient(PrintWriter toClientWriter)
	{
		return(clientList.add(toClientWriter));
	}

	// Remove the given client from the active clients list
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized boolean removeClient(PrintWriter toClientWriter)
	{
		return(clientList.remove(toClientWriter));
	}

	// Relay the given message to all the active clients
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized void relayMessage(
			PrintWriter fromClientWriter, String mesg)
	{
		// Iterate through the client list and
		// relay message to each client (but not the sender)
		for (PrintWriter toClientWriter : clientList) {
            if (!toClientWriter.equals(fromClientWriter)) {
                toClientWriter.println(mesg);
            }
        }
	}

	// The child thread starts here
	public void run()
	{
		// Read from the client and relay to other clients
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
            String clientName = in.readLine();
            PrintWriter out = new PrintWriter(clientSock.getOutputStream(), true);
            addClient(out);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                relayMessage(out, clientName + ": " + inputLine);
            }

            removeClient(out);
            clientSock.close();
		}
		catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}

	/*
	 * The group chat server program starts from here.
	 * This main thread accepts new clients and spawns a thread for each client
	 * Each child thread does the stuff under the run() method
	 */
	public static void main(String args[])
	{
		// Server needs a port to listen on
		if (args.length != 1) {
			System.out.println("usage: java GroupChatServer <server port>");
			System.exit(1);
		}

		// Be prepared to catch socket related exceptions
		try {
			// Create a server socket with the given port
			int portNumber = Integer.parseInt(args[0]);
        	ServerSocket serverSocket = new ServerSocket(portNumber);
			// Keep accepting/serving new clients
			while (true) {
				// Wait to accept another client
				Socket clientSocket = serverSocket.accept();
           		new Thread(new GroupChatServer(clientSocket)).start();
				// Spawn a thread to read/relay messages from this client
			}
		}
		catch(Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}
}