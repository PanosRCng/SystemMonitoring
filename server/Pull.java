import java.util.ArrayList;
import java.util.Map;

public class Pull
{
	private Map<String, ArrayList<Report>> machinesReports;

	// constructor
	public Pull(Map<String, ArrayList<Report>> machinesReports)
	{
		this.machinesReports = machinesReports;
	}


	public Map<String, ArrayList<Report>> getMachinesReports()
	{
		return this.machinesReports;
	}
}
