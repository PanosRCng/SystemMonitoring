/*
 * callback interface 
 * 
 * calls back from Thread
 */

public interface ThreadListener
{
	// calls back when Thread's status changed
	void onThreadStatusChanged(String Tag, String status);
}
