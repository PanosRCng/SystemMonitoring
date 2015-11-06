import java.util.Timer;
import java.util.ArrayList;


public class PushClientApp implements PushWorkerListener, CollectorWorkerListener
{
	private static final String TAG = "APP";

	private Timer timer;
	private CollectorWorker collectorWorker;

	// for scheduling CollectorWorker thread
	private final static int DELAY = 1000;
	private final static int PERIOD = 5000;

	private final static String REMOTE_SERVER = "127.0.0.1";
	private final static int REMOTE_PORT = 6003;

	// keeps the PushWorker status
	private String pushWorkerStatus;

	// keeps the CollectorWorker status
	private String collectorWorkerStatus;

	private ArrayList<Report> reports;

	// constructor
	public PushClientApp()
	{
    		timer = new Timer();

		reports = new ArrayList<Report>();
	}


	/*
	 * calls back when PushWorker status changed
	 */
	@Override
	public void onPushWorkerStatusChanged(String status)
	{
		pushWorkerStatus = status;

		System.out.println(PushWorkerListener.TAG + " : " + pushWorkerStatus);
	}


	/*
	 * calls back when CollectorWorker status changed
	 */
	@Override
	public void onCollectorWorkerStatusChanged(String status)
	{
		collectorWorkerStatus = status;

		System.out.println(CollectorWorkerListener.TAG + " : " + collectorWorkerStatus);
	}


	/*
	 * calls back when CollectorWorker has constructed a report
	 */
	@Override
	public void onReceiveReport(Report report)
	{
		reports.add(report);

		if( reports.size() >= 10 )
		{
			startPushWorker();

			reports.clear();

			System.out.println(this.TAG + " : " + "clear reports list");
		}
	}


	/*
	 * set CollectorWorker as a scheduled thread with DELAY and PERIOD
	 */
	private void startCollectorWorker()
	{
		collectorWorker = new CollectorWorker(this);

		timer.schedule(collectorWorker, DELAY, PERIOD);
	}


	/*
	 * starts PushWorker in a new thread
	 */
	private void startPushWorker()
	{
		PushWorker pushWorker = new PushWorker(this, REMOTE_SERVER, REMOTE_PORT, new ArrayList<Report>(reports));

		Thread pushThread = new Thread(pushWorker);

		pushThread.start();
	}


	private static boolean setupKeys()
	{
		ClientKeyManager clientKeyManager = new ClientKeyManager();
	
		if( clientKeyManager.getKeyPair() )
		{
			if( clientKeyManager.exportCertificate("push_client.cert") )
			{
				return true;
			}
		}

		return false;
	}


	private static boolean handleInput(String[] args)
	{
		for(int i=0; i<args.length; i++)
		{
			if( (args[i].length() > 256) && !(args[i] instanceof String) )
			{
				return false;
			}
		}

		if(args.length == 1)
		{
			if( args[0].equals("setup_keys") )
			{
				if(setupKeys())
				{
					return true;
				}
			}				
		}

		return false;
	}


	private static void printUsage()
	{
		System.out.println("usage");
	}


	public static void main(String[] args)
	{
		if(args.length > 0)
		{
			if( !handleInput(args) )
			{
				printUsage();
			}

			return;
		}

		PushClientApp pushClientApp = new PushClientApp();

		pushClientApp.startCollectorWorker();
  	}
}
