/*
 * callback interface 
 * 
 * calls back from CollectorWorker
 */

public interface CollectorWorkerListener
{
	public final static String TAG = "COLLECTOR_WORKER";

	// calls back when CollectorWorker status changed
	void onCollectorWorkerStatusChanged(String status);

	// calls back when CollectorWorker has constructed a report
	void onReceiveReport(Report stat);
}
