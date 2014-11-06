import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.ArrayList;


public class DBHelper
{
	// the database url
	private final static String DATABASE_URL = "jdbc:mysql://localhost:3306/databaseName";

	// the database username
	private final static String DATABASE_USERNAME = "username";	

	// the database password
	private final static String DATABASE_PASSWORD = "password";

	private final static String TAG = "DB_HELPER";


	/*
	 * constructor 
	 * gets the database url, username and password
	 * loads the JDBC driver
	 */
	public DBHelper()
	{
		// try to load the JDBC driver
		try
		{
			System.out.println(TAG + " : " + "Loading JDBC driver...");

    			Class.forName("com.mysql.jdbc.Driver");

			System.out.println(TAG + " : " + "JDBC driver loaded");
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println(TAG + " : " + "Cannot load the JDBC driver");
		}
	}


	/*
	 * insert report records to database
	 */
	public void insertRecords(ArrayList<Report> db_reports)
	{
		Connection connection = null;

		try
		{
	    		System.out.println(TAG + " : " + "Connecting database...");

    			connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

    			System.out.println(TAG + " : " + "Database connected");

			Statement statement = connection.createStatement();

			for(int i=0; i<db_reports.size(); i++)
			{
				// insert report record to reports table
				int result = statement.executeUpdate( getInsertionQuery( db_reports.get(i) ) );
			}

			statement.close();
		}
		catch(SQLException ex)
		{
			System.out.println(TAG + " : " + "Cannot connect the database");
		}
		finally
		{
			if(connection != null)
			{
				try
				{
					connection.close();

					System.out.println(TAG + " : " + "Database connection closed");
				}
				catch(SQLException ex)
				{
					//
				}
			}
		}
	}


	/*
	 * fetch report records from database
	 */
	public ArrayList<Report> fetchRecords(long dateTimeMillis)
	{
		ArrayList<Report> db_reports = new ArrayList<Report>();

		Connection connection = null;

		try
		{
	    		System.out.println(TAG + " : " + "Connecting database...");

    			connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

    			System.out.println(TAG + " : " + "Database connected");

			Statement statement = connection.createStatement();

			ResultSet rec = statement.executeQuery( getFetchQuery( dateTimeMillis ) );

			while( rec.next() )
			{
				Report report = new Report();

				report.setDateTime( rec.getLong(ReportsTable.COLUMN_DATETIME) );
				report.setMachine( rec.getString(ReportsTable.COLUMN_MACHINE) );
				report.setCpuUsage( rec.getLong(ReportsTable.COLUMN_CPU_USAGE) );
				report.setTotalMemory( rec.getLong(ReportsTable.COLUMN_TOTAL_MEM) );
				report.setUsedMemory( rec.getLong(ReportsTable.COLUMN_USED_MEM) );
				report.setTotalFileSystemSpace( rec.getLong(ReportsTable.COLUMN_TOTAL_FILESYSTEM_SPACE) );
				report.setUsedFileSystemSpace( rec.getLong(ReportsTable.COLUMN_USED_FILESYSTEM_SPACE) );
				report.setRxKbps( rec.getLong(ReportsTable.COLUMN_NET_RX_KBPS) );
				report.setTxKbps( rec.getLong(ReportsTable.COLUMN_NET_TX_KBPS) );

				db_reports.add(report);
			}

			statement.close();
		}
		catch(SQLException ex)
		{
			System.out.println(TAG + " : " + "Cannot connect the database");
		}
		finally
		{
			if(connection != null)
			{
				try
				{
					connection.close();

					System.out.println(TAG + " : " + "Database connection closed");
				}
				catch(SQLException ex)
				{
					//
				}
			}
		}

		return db_reports;
	}


	/*
	 * checks if database connection is ok
	 * checks is reports table exists, if not, it creates it
	 */
	public void checkDatabase()
	{
		Connection connection = null;

		try
		{
	    		System.out.println(TAG + " : " + "Connecting database...");

    			connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

    			System.out.println(TAG + " : " + "Database connected");

			Statement statement = connection.createStatement();

			// create reports table 
			int result = statement.executeUpdate(ReportsTable.CREATE_REPORTS_TABLE);
			System.out.println(TAG + " : " + "Reports table exists/created");

			statement.close();
		}
		catch(SQLException ex)
		{
			System.out.println(TAG + " : " + "Cannot connect the database");
		}
		finally
		{
			if(connection != null)
			{
				try
				{
					connection.close();

					System.out.println(TAG + " : " + "Database connection closed");
				}
				catch(SQLException ex)
				{
					//
				}
			}
		}
	}


	private String getInsertionQuery(Report report)
	{
		String query = "insert into " + ReportsTable.TABLE_REPORTS
					      + " (" 
					      + ReportsTable.COLUMN_DATETIME + ", "
					      + ReportsTable.COLUMN_MACHINE + ", " 
					      + ReportsTable.COLUMN_CPU_USAGE + ", "
					      + ReportsTable.COLUMN_TOTAL_MEM + ", "
					      + ReportsTable.COLUMN_USED_MEM + ", "
					      + ReportsTable.COLUMN_TOTAL_FILESYSTEM_SPACE + ", "
					      + ReportsTable.COLUMN_USED_FILESYSTEM_SPACE + ", "
					      + ReportsTable.COLUMN_NET_RX_KBPS + ", "
					      + ReportsTable.COLUMN_NET_TX_KBPS
					      + ") values("
					      + report.getDateTime() + ", "
					      + "'" + report.getMachine() + "', "
					      + report.getCpuUsage() + ", "
					      + report.getTotalMemory() + ", "
					      + report.getUsedMemory() + ", "
					      + report.getTotalFileSystemSpace() + ", "
					      + report.getUsedFileSystemSpace() + ", "
					      + report.getRxKbps() + ", "
					      + report.getTxKbps()
					      + ");";

		return query;
	}


	private String getFetchQuery(long dateTimeMillis)
	{
		String query = "select * from reports where " + ReportsTable.COLUMN_DATETIME + " > " + dateTimeMillis + " ;";

		return query;
	}

	
}
