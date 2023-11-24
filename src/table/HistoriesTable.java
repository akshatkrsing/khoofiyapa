package table;

public class HistoriesTable {
    public static final String TABLE_NAME="histories";
    public static final String COLUMN_H_ID="h_id";
    public static final String COLUMN_FILE_ID="file_id";
    public static final String COLUMN_FILE_NAME="filename";
    public static final String COLUMN_ACTION_TIME="actiontime";
    public static final String COLUMN_ACTION_TYPE="actionType";
    public static final String COLUMN_ALGO_USED="algo_used";

   // public static final String QUERY_INSERT_TO_HISTORY_TABLE= "insert into "+TABLE_NAME+"("+COLUMN_FILE_NAME+","+COLUMN_ACTION_TIME +")VALUES(?,?)";
    public static final String QUERY_RETRIEVE_FROM_HISTORIES_TABLE="SELECT * FROM "+TABLE_NAME+";";
}
