import java.util.ArrayList;

public class Push
{
	private String header;
	private ArrayList<Report> reports;

	// constructor
	public Push(String header, ArrayList<Report> reports)
	{
		this.header = header;
		this.reports = reports;
	}

	public String getHeader()
	{
		return this.header;
	}

	public ArrayList<Report> getReports()
	{
		return this.reports;
	}
}
