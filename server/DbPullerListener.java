/*
 * callback interface 
 * 
 * calls back from DbPuller
 */

public interface DbPullerListener
{
	public final static String TAG = "DB PULLER";

	// calls back when DbPuller status changed
	void onDbPullerStatusChanged(String status);

	// calls back when DbPuller fetched a new Pull from database
	void onPullFetched(Pull pull);
}
