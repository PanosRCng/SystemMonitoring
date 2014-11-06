/*
 * callback interface 
 * 
 * calls back from DbPusher
 */

public interface DbPusherListener
{
	public final static String TAG = "DB PUSHER";

	// calls back when DbPusher status changed
	void onDbPusherStatusChanged(String status);

	// calls back when DbPusher pushes PushList to database
	void onPush();
}
