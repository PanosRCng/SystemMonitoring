/*
 * callback interface 
 * 
 * calls back from PushWorker
 */

public interface PushWorkerListener
{
	public final static String TAG = "PUSH_WORKER";

	void onPushWorkerStatusChanged(String status);
}
