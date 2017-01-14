/*
 * callback interface 
 * 
 * calls back from DbPusher
 */

public interface DbPusherListener extends ThreadListener
{
	// calls back when DbPusher pushes PushList to database
	void onPush();
}
