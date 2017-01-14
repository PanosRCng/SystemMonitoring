/*
 * callback interface 
 * 
 * calls back from DbPuller
 */

public interface DbPullerListener extends ThreadListener
{
	// calls back when DbPuller fetched a new Pull from database
	void onPullFetched(Pull pull);
}
