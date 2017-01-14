import java.io.*;
import java.net.*;
import com.google.gson.Gson;


public class PullServer extends Server
{
	public final static String TAG = "PULL_SERVER";

	// the Pull
	private Pull pull;

	private PullServerListener pullServerListener;

	/*
	 * constructor 
	 * gets a reference to the object that implements the PullServerListener interface
	 * gets the port where the server listens
	 * initializes the secure random numbers
	 */
	public PullServer(PullServerListener pullServerListener, int server_port)
	{
		super(server_port);

		this.pullServerListener = pullServerListener;
	}


	protected void serverLogic(Socket connection)
	{
		try
		{
			// get an output stream for writing to socket (byte stream)
			OutputStream out = connection.getOutputStream();

			// get an output stream writer with utf-8 charset 
			//(characters written to it are encoded into bytes)
			Writer writer = new OutputStreamWriter(out, "UTF-8");

			// writes text to a character-output stream
			writer = new BufferedWriter(writer);

			statusChanged("Sending data to client");

			Gson gson = new Gson();

			// convert the Pull to the json string
			String jsonPull = gson.toJson(pull);  

			// write to socket
			writer.write(jsonPull);

			// make sure to send over the network
			writer.flush();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}

	protected void statusChanged(String status)
	{
		pullServerListener.onThreadStatusChanged(TAG, status);
	}


	public void setPull(Pull pull)
	{
		this.pull = pull;
	}

}


