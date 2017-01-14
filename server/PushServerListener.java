/*
 * callback interface 
 * 
 * calls back from PushServer
 */

public interface PushServerListener extends ThreadListener
{
	// calls back when PushServer receives a Push from client
	void onPushReceived(Push push);
}
