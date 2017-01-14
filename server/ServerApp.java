import java.util.Timer;
import java.util.ArrayList;
import java.util.List;


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
	 * calls back when a Thread's status changed
	 */
	@Override
	public void onThreadStatusChanged(String TAG, String status)
	{
		System.out.println(TAG + " : " + status);
	}


	/*
	 * calls back when PushServer receives a Push from a client
	 */
	@Override
	public void onPushReceived(Push push)
	{
		// add Push to pushList
		pushList.add(push);

		System.out.println(PushServer.TAG + " : " + "Push received from " + push.getHeader());
	}



	/*
	 * calls back when DbPusher pushes PushList to database
	 */
	@Override
	public void onPush()
	{
		pushList.clear();

		System.out.println(DbPusher.TAG + " : " + "PushList pushed to database");
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


	private static boolean setupKeys()
	{	
		if( ServerKeyManager.checkSetup() )
		{
			System.out.println("can not setup keys / another set of keys is already in place");		
	
			return false;
		}

		ServerKeyManager serverKeyManager = new ServerKeyManager();

		if( serverKeyManager.getKeyPair() )
		{
			if( serverKeyManager.exportCertificate() )
			{
				if( serverKeyManager.setClientsKeystore() )
				{
					return true;
				}
			}
		}

		return false;
	}


	private static boolean showClients()
	{
		if( !ServerKeyManager.checkSetup() )
		{
			System.out.println("keys not found / run setup_keys first to create keys");		
	
			return false;
		}

		ServerKeyManager serverKeyManager = new ServerKeyManager();

		List<String> clients = serverKeyManager.getClients();

		if(clients.size() == 0)
		{
			System.out.println("no clients to show");
		}

		for (String client : clients)
		{
			System.out.println(client);
		}

		return true;
	}


	private static boolean addClient(String cert_filepath, String client_alias)
	{
		if( !ServerKeyManager.checkSetup() )
		{
			System.out.println("keys not found / run setup_keys first to create keys");		
	
			return false;
		}

		ServerKeyManager serverKeyManager = new ServerKeyManager();

		if( serverKeyManager.addClientCertificate(cert_filepath, client_alias) )
		{
			return true;
		}

		System.out.println("could not import client / or the given alias already exists");

		return false;
	}

	
	private static boolean deleteClient(String client_alias)
	{
		if( !ServerKeyManager.checkSetup() )
		{
			System.out.println("keys not found / run setup_keys first to create keys");		
	
			return false;
		}

		ServerKeyManager serverKeyManager = new ServerKeyManager();

		if( serverKeyManager.removeClientCertificate(client_alias) )
		{
			return true;
		}

		System.out.println("could not remove client / or the alias does not exist");

		return false;
	}


	private static void printUsage()
	{
		System.out.println("usage");
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
				if( setupKeys() )
				{
					return true;
				}
			}
			else if( args[0].equals("show_clients") )
			{
				if( showClients() )
				{
					return true;
				}
			}				
		}
		else if(args.length == 2)
		{
			if( args[0].equals("delete_client") )
			{
				if( deleteClient(args[1]) )
				{
					return true;
				}
			}
		}
		else if(args.length == 3)
		{
			if( args[0].equals("add_client") )
			{
				if(addClient(args[1], args[2]))
				{
					return true;
				}
			}
		}

		return false;
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


		if( !ServerKeyManager.checkSetup() )
		{
			System.out.println("keys not found / run setup_keys first to create keys");		
	
			return;
		}

		ServerApp serverApp = new ServerApp();

		serverApp.checkDatabase();

		serverApp.startPushServer();

		serverApp.startPullServer();

		serverApp.startDbPusher();

		serverApp.startDbPuller();
	}

}
