/*
 * callback interface 
 * 
 * calls back from PushServer
 */

public interface PushServerListener
{
	public final static String TAG = "PUSH_SERVER";

	// calls back when PushServer status changed
	void onPushServerStatusChanged(String status);

	// calls back when PushServer receives a Push from client
	void onPushReceived(Push push);
}
