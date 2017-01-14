import java.io.*;
import java.net.*;
import com.google.gson.Gson;


public class PushServer extends Server
{
	public final static String TAG = "PUSH_SERVER";

	private PushServerListener pushServerListener;

	/*
	 * constructor 
	 * gets a reference to the object that implements the PushServerListener interface
	 * gets the port where the server listens
	 * initializes the secure random numbers
	 */
	public PushServer(PushServerListener pushServerListener, int server_port)
	{
		super(server_port);

		this.pushServerListener = pushServerListener;
	}


	protected void serverLogic(Socket connection)
	{
		StringBuilder jsonPush = new StringBuilder();

		try
		{
			// get an input stream for reading bytes from socket (byte stream)
			InputStream in = connection.getInputStream();

			// get an input stream reader with utf-8 charset
			// reads bytes and decodes them into characters using a utf-8 charset
			InputStreamReader reader = new InputStreamReader(in, "UTF-8");

			// read from socket and build the string
			for(int c=reader.read(); c!=-1; c=reader.read())
			{
				jsonPush.append((char) c);
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return;
		}

		Gson gson = new Gson();

		// construct the report Push from the json string
		Push push = gson.fromJson(jsonPush.toString(), Push.class);   

		// inform PushServerListener about the Push
		pushServerListener.onPushReceived(push);
	}


	protected void statusChanged(String status)
	{
		pushServerListener.onThreadStatusChanged(TAG, status);
	}


}


