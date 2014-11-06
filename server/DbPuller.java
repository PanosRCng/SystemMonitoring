import java.util.TimerTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.GregorianCalendar;


public class DbPuller extends TimerTask
{
	// the object that implements the DbPullerListener interface
	private static DbPullerListener dbPullerListener;

	private DBHelper dbHelper;	
	

	/*
	 * constructor 
	 * gets a reference to the object that implements the DbPullerListener interface
	 */
	DbPuller(DbPullerListener dbPullerListener)
	{
		this.dbPullerListener = dbPullerListener;

		dbHelper = new DBHelper();
	}

	
	@Override
	public void run()
	{
		dbPullerListener.onDbPullerStatusChanged("pulling machines' records from database");

		// get and init a calendar
		GregorianCalendar gCalendar = new GregorianCalendar();

		// set time a minute back
		int minute = gCalendar.get(GregorianCalendar.MINUTE);
		minute = minute - 3;
		gCalendar.set(GregorianCalendar.MINUTE, minute);

		// get the one minute back date-time as milliseconds
		long dateTimeMillis = gCalendar.getTimeInMillis();

		ArrayList<Report> db_reports = new ArrayList<Report>();

		// fetch report records from database
		db_reports = dbHelper.fetchRecords(dateTimeMillis);

		// construct the Pull
		Map<String, ArrayList<Report>> machinesReports = new HashMap<String, ArrayList<Report>>();

		for(int i=0; i<db_reports.size(); i++)
		{
			Report report = db_reports.get(i);
			String machine = report.getMachine();

			if( machinesReports.containsKey(machine) )
			{
				ArrayList<Report> machineReportList = machinesReports.get(machine);

				machineReportList.add(report);
			}
			else
			{
				ArrayList<Report> machineReportList = new ArrayList<Report>();

				machineReportList.add(report);

				machinesReports.put(machine, machineReportList);
			}
		}

		Pull pull = new Pull(machinesReports);

		dbPullerListener.onPullFetched(pull);
	}

}
