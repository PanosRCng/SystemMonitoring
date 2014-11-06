import java.util.TimerTask;
import java.util.GregorianCalendar;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
	
public class CollectorWorker extends TimerTask
{
	// the object that implements the CollectorWorkerListener interface
	private static CollectorWorkerListener collectorWorkerListener;

	private final Sigar sigar;

	private long prevRxBytes;
	private long prevTxBytes;		


	/*
	 * constructor 
	 * gets a reference to the object that implements the CollectorWorkerListener interface
	 * creates a Sigar object
	 */
	public CollectorWorker(CollectorWorkerListener collectorWorkerListener)
	{
		this.collectorWorkerListener = collectorWorkerListener;

		sigar = new Sigar();

		prevRxBytes = 0;
		prevTxBytes = 0;
	}

  	@Override 
	public void run()
	{
		Report report = new Report();

		// get and init a calendar, and get date-time as milliseconds
		GregorianCalendar gCalendar = new GregorianCalendar();
		long dateTimeMillis = gCalendar.getTimeInMillis();

		// get stats
		long[] memoryStats = getMemoryStats();
		long[] fileSystemSpaceStats = getFileSystemStats();
		long[] networkStats = getNetworkStats();

		long cpuUsage = getCpuStats();
		long totalMemory = memoryStats[0];
		long usedMemory = memoryStats[1];
		long totalFileSystemSpace = fileSystemSpaceStats[0];
		long usedFileSystemSpace = fileSystemSpaceStats[1];
		long rxKbps = networkStats[0];
		long txKbps = networkStats[1];

		// fill report
		report.setDateTime( dateTimeMillis );
		report.setCpuUsage( cpuUsage );
		report.setTotalMemory( totalMemory );
		report.setUsedMemory( usedMemory );
		report.setTotalFileSystemSpace( totalFileSystemSpace );
		report.setUsedFileSystemSpace( usedFileSystemSpace );
		report.setRxKbps( rxKbps );
		report.setTxKbps( txKbps );

    		collectorWorkerListener.onReceiveReport(report);
	}


	private long getCpuStats()
	{
		CpuPerc cpuperc = null;

		try
		{
			cpuperc = sigar.getCpuPerc();
		}
		catch(SigarException se)
		{
			se.printStackTrace();
		}	

		// Sum of User + Sys + Nice + Wait cpu percentage 
		return (long)( Math.round(cpuperc.getCombined()*100) );
	}


	private long[] getMemoryStats()
	{
		Mem mem = null;

		try
		{
        		mem = sigar.getMem();
        	}
		catch(SigarException se)
		{
            		se.printStackTrace();
        	}

		// in megabytes
		return new long[]{mem.getTotal()/1024/1024, mem.getActualUsed()/1024/1024};
	}


	private long[] getFileSystemStats()
	{
		FileSystemUsage fileSystemUsage = null;

		try
		{
        		FileSystem[] fileSystemList = sigar.getFileSystemList();

		//	for(int i=0; i<fileSystemList.length; i++)
		//	{
		//		System.out.println(fileSystemList[i]);
		//	}

			if( fileSystemList.length > 0 )
			{
				FileSystem fileSystem = fileSystemList[0];

				fileSystemUsage = sigar.getFileSystemUsage( fileSystem.getDirName() );
			}
        	}
		catch(SigarException se)
		{
            		se.printStackTrace();
        	}

		// in gigabytes
		return new long[]{fileSystemUsage.getTotal()/1024/1024/1024, fileSystemUsage.getUsed()/1024/1024/1024};
	}


	private long[] getNetworkStats()
	{
		NetInterfaceStat netInterfaceStat = null;

		try
		{
        		String[] netInterfaceList = sigar.getNetInterfaceList();

			if( netInterfaceList.length > 0 )
			{
				netInterfaceStat = sigar.getNetInterfaceStat( netInterfaceList[0] );
			}
        	}
		catch(SigarException se)
		{
            		se.printStackTrace();
        	}


		long currRxBytes = netInterfaceStat.getRxBytes();
		long currTxBytes = netInterfaceStat.getTxBytes();

		long deltaRxBytes;
		long deltaTxBytes;

		// ignore the first sampling
		if( (prevTxBytes != 0) && (prevRxBytes != 0) )
		{
			deltaRxBytes = currRxBytes - prevRxBytes;
			deltaTxBytes = currTxBytes - prevTxBytes;			
		}
		else
		{
			deltaRxBytes = 0;
			deltaTxBytes = 0;
		}

		prevRxBytes = currRxBytes;
		prevTxBytes = currTxBytes;

		// in Kbps
		return new long[]{(deltaRxBytes*8)/1024/5, (deltaTxBytes*8)/1024/5};
	}

}
