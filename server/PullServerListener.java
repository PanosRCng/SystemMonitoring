/*
 * callback interface 
 * 
 * calls back from PullServer
 */

public interface PullServerListener
{
	public final static String TAG = "PULL_SERVER";

	// calls back when PullServer status changed
	void onPullServerStatusChanged(String status);
}
