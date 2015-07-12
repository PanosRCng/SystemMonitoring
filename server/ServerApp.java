import java.util.Timer;
import java.util.ArrayList;


public class ServerApp implements PushServerListener, PullServerListener, DbPusherListener, DbPullerListener
{
	private static final String TAG = "APP";

	private Timer timer;
	private DbPusher dbPusher;
	private DbPuller dbPuller;

	// for scheduling DbPusher thread 
	private final static int DB_PUSHER_DELAY = 1000;
	private final static int DB_PUSHER_PERIOD = 5000;

	// for scheduling PullServer thread 
	private final static int DB_PULLER_DELAY = 1000;
	private final static int DB_PULLER_PERIOD = 3000;

	// the port where the PushServer listens
	private final static int PUSH_SERVER_PORT = 6003;

	// the port where the PullServer listens
	private final static int PULL_SERVER_PORT = 6004;


	// keeps the PushServer status
	private String pushServerStatus;

	// keeps the PullServer status
	private String pullServerStatus;

	// keeps the DbPusher status
	private String dbPusherStatus;

	// keeps the PullServer object
	private PullServer pullServer; 


	private ArrayList<Push> pushList;

	private Pull pull;


	/*
	 * constructor
	 */
	public ServerApp()
	{
		timer = new Timer();

		pushList = new ArrayList<Push>();
	}


	/*
	 * calls back when PushServer status changed
	 */
	@Override
	public void onPushServerStatusChanged(String status)
	{
		pushServerStatus = status;

		System.out.println(PushServerListener.TAG + " : " + pushServerStatus);
	}


	/*
	 * calls back when PushServer receives a Push from a client
	 */
	@Override
	public void onPushReceived(Push push)
	{
		// add Push to pushList
		pushList.add(push);

		System.out.println(PushServerListener.TAG + " : " + "Push received from " + push.getHeader());
	}


	/*
	 * calls back when PullServer status changed
	 */
	@Override
	public void onPullServerStatusChanged(String status)
	{
		pullServerStatus = status;

		System.out.println(PullServerListener.TAG + " : " + pullServerStatus);
	}


	/*
	 * calls back when DbPusher status changed
	 */
	@Override
	public void onDbPusherStatusChanged(String status)
	{
		dbPusherStatus = status;

		System.out.println(DbPusherListener.TAG + " : " + dbPusherStatus);
	}


	/*
	 * calls back when DbPusher pushes PushList to database
	 */
	@Override
	public void onPush()
	{
		pushList.clear();

		System.out.println(DbPusherListener.TAG + " : " + "PushList pushed to database");
	}


	/*
	 * calls back when DbPuller status changed
	 */
	@Override
	public void onDbPullerStatusChanged(String status)
	{
		System.out.println(DbPullerListener.TAG + " : " + status);
	}


	/*
	 * calls back when DbPuller fetched a new Pull from database
	 */
	@Override
	public void onPullFetched(Pull pull)
	{
		this.pull = pull;

		// pass the updated Pull to PullServer
		if(pullServer != null)
		{
			pullServer.setPull(pull);
		}
	}


	/*
	 * starts PushServer in a new thread
	 */
	private void startPushServer()
	{
		PushServer pushServer = new PushServer(this, PUSH_SERVER_PORT);

		Thread pushServerThread = new Thread(pushServer);

		pushServerThread.start();
	}


	/*
	 * starts PullServer in a new thread
	 */
	private void startPullServer()
	{
		pullServer = new PullServer(this, PULL_SERVER_PORT);

		Thread pullServerThread = new Thread(pullServer);

		pullServerThread.start();
	}


	/*
	 * set DbPusher as a scheduled thread with DB_PUSHER_DELAY and DB_PUSHER_PERIOD
	 */
	private void startDbPusher()
	{
		dbPusher = new DbPusher(this, pushList);

		timer.schedule(dbPusher, DB_PUSHER_DELAY, DB_PUSHER_PERIOD);
	}


	/*
	 * set DbPuller as a scheduled thread with DB_PULLER_DELAY and DB_PULLER_PERIOD
	 */
	private void startDbPuller()
	{
		dbPuller = new DbPuller(this);

		timer.schedule(dbPuller, DB_PULLER_DELAY, DB_PULLER_PERIOD);
	}


	/*
	 * checks if database connection is ok
	 * checks is reports table exists, if not, it creates it
	 */
	private void checkDatabase()
	{
		DBHelper dbHelper = new DBHelper();

		dbHelper.checkDatabase();
	}




	public static void main(String[] args)
	{
		ServerApp serverApp = new ServerApp();

		KeyManager keyManager = new KeyManager();

		if (keyManager.getKeyPair() )
		{
			System.out.println("all cool");
		}

	//	serverApp.checkDatabase();

	//	serverApp.startPushServer();

	//	serverApp.startPullServer();

	//	serverApp.startDbPusher();

	//	serverApp.startDbPuller();
	}

}
