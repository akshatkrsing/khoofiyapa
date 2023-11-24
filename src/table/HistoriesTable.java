package table;

public class HistoriesTable {
    public static final String TABLE_NAME="histories";
    public static final String COLUMN_H_ID="h_id";
    public static final String COLUMN_FILE_PATH="filepath";
    public static final String COLUMN_ACTION_TIME="actiontime";
    public static final String QUERY_RETRIEVE_FROM_HISTORIES_TABLE="SELECT * FROM "+TABLE_NAME+";";
    public static final int COLUMN_FILE_NAME = 2;
    public static final int COLUMN_ACTION_TYPE = 3;
    public static final int COLUMN_ALGO_USED = 4;
}
