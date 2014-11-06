


public class Report
{
	private long id;

	private long dateTime;

	private String machine;

	private long cpuUsage;

	private long totalMemory;
	private long usedMemory;

	private long totalFileSystemSpace;
	private long usedFileSystemSpace;		

	private long rxKbps;
	private long txKbps;


	// constructor
	public Report()
	{
		//
	}


	public void setId(long id)
	{
		this.id = id;
	}


	public void setDateTime(long dateTime)
	{
		this.dateTime = dateTime;
	}


	public void setMachine(String machine)
	{
		this.machine = machine;
	}


	public void setCpuUsage(long cpuUsage)
	{
		this.cpuUsage = cpuUsage;
	}


	public void setTotalMemory(long totalMemory)
	{
		this.totalMemory = totalMemory;
	}


	public void setUsedMemory(long usedMemory)
	{
		this.usedMemory = usedMemory;
	}


	public void setTotalFileSystemSpace(long totalFileSystemSpace)
	{
		this.totalFileSystemSpace = totalFileSystemSpace;
	}


	public void setUsedFileSystemSpace(long usedFileSystemSpace)
	{
		this.usedFileSystemSpace = usedFileSystemSpace;
	}


	public void setRxKbps(long rxKbps)
	{
		this.rxKbps = rxKbps;
	}


	public void setTxKbps(long txKbps)
	{
		this.txKbps = txKbps;
	}


	public long getId()
	{
		return this.id;
	}


	public long getDateTime()
	{
		return this.dateTime;
	}
	

	public String getMachine()
	{
		return this.machine;
	}


	public long getCpuUsage()
	{
		return this.cpuUsage;
	}


	public long getTotalMemory()
	{
		return this.totalMemory;
	}


	public long getUsedMemory()
	{
		return this.usedMemory;
	}


	public long getTotalFileSystemSpace()
	{
		return this.totalFileSystemSpace;
	}


	public long getUsedFileSystemSpace()
	{
		return this.usedFileSystemSpace;
	}


	public long getRxKbps()
	{
		return this.rxKbps;
	}


	public long getTxKbps()
	{
		return this.txKbps;
	}
}
