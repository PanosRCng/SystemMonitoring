/*
 * callback interface 
 * 
 * calls back from PullWorker
 */

public interface PullWorkerListener
{
	public final static String TAG = "PULL_WORKER";

	// calls back when PushServer status changed
	void onPullWorkerStatusChanged(String status);

	// calls back when PullWorker receives a Pull
	void onPullReceived(Pull pull);
}
