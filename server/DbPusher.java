import java.util.TimerTask;
import java.util.ArrayList;


public class DbPusher extends TimerTask
{
	public final static String TAG = "DB PUSHER";

	// the object that implements the DbPusherListener interface
	private static DbPusherListener dbPusherListener;

	private ArrayList<Push> pushList;

	private DBHelper dbHelper;	
	

	/*
	 * constructor 
	 * gets a reference to the object that implements the DbPusherListener interface
	 * gets the Push list to push to database
	 */
	public DbPusher(DbPusherListener dbPusherListener, ArrayList<Push> pushList)
	{
		this.dbPusherListener = dbPusherListener;
		this.pushList = pushList;

		dbHelper = new DBHelper();
	}

	
	@Override
	public void run()
	{
		if( pushList.size() > 2 )
		{
			dbPusherListener.onThreadStatusChanged(TAG, "pushing pushList to database");

			ArrayList<Report> db_reports = new ArrayList<Report>();

			// construct the reports list to save to database, from the Push items in PushList
			for(int i=0; i<pushList.size(); i++)
			{
				Push push = pushList.get(i);

				String header = push.getHeader();
				ArrayList<Report> reports = push.getReports();

				for(int j=0; j<reports.size(); j++)
				{
					Report report = reports.get(j);

					report.setMachine(header);

					db_reports.add(report);
				}
			}

			// insert report records to database
			dbHelper.insertRecords(db_reports);

			dbPusherListener.onPush();
		}
	}

}
