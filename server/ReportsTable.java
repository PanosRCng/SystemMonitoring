

/*
 * Reports database table
 */
public class ReportsTable
{
	/*
	 *  Reports table structure
	 */
	public static final String TABLE_REPORTS = "reports";

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_DATETIME = "date_time";
	public static final String COLUMN_MACHINE = "machine";
	public static final String COLUMN_CPU_USAGE = "cpu_usage";
	public static final String COLUMN_TOTAL_MEM = "total_mem";
	public static final String COLUMN_USED_MEM = "used_mem";
	public static final String COLUMN_TOTAL_FILESYSTEM_SPACE = "total_filesystem_place";
	public static final String COLUMN_USED_FILESYSTEM_SPACE = "used_filesystem_place";
	public static final String COLUMN_NET_RX_KBPS = "net_rx_Kbps";
	public static final String COLUMN_NET_TX_KBPS = "net_tx_Kbps";

	
	/*
	 *  reports table creation SQL statement
	 */
	public static final String CREATE_REPORTS_TABLE = "create table " 
			+ "if not exists "
			+ TABLE_REPORTS
			+ "(" 
			+ COLUMN_ID + " int auto_increment primary key, " 
			+ COLUMN_DATETIME + " BIGINT, "
			+ COLUMN_MACHINE + " text(64), " 
			+ COLUMN_CPU_USAGE + " int, "
			+ COLUMN_TOTAL_MEM + " int, "
			+ COLUMN_USED_MEM + " int, "
			+ COLUMN_TOTAL_FILESYSTEM_SPACE + " int, "
			+ COLUMN_USED_FILESYSTEM_SPACE + " int, "
			+ COLUMN_NET_RX_KBPS + " int, "
			+ COLUMN_NET_TX_KBPS + " int"
			+ ");";


}
