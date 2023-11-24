package table;

public class HistoriesTable {
    public static final String TABLE_NAME="histories";
    public static final String COLUMN_FILE_PATH="filepath";
    public static final String COLUMN_ACTION_TIME="actiontime";
    public static final String COLUMN_ACTION_TYPE="actiontype";
    public static final String QUERY_RETRIEVE_FROM_HISTORIES_TABLE="SELECT * FROM "+SecretsTable.TABLE_NAME+" JOIN "+
            TABLE_NAME +" ON "+ SecretsTable.TABLE_NAME+"."+COLUMN_FILE_PATH+ "="+ TABLE_NAME+ "."+ COLUMN_FILE_PATH
            +" ORDER BY "+COLUMN_ACTION_TIME+" DESC";

    public static final String QUERY_INSERT_INTO_HISTORIES_TABLE="insert into "+TABLE_NAME+" ( "+COLUMN_FILE_PATH+
            ","+COLUMN_ACTION_TIME+","+COLUMN_ACTION_TYPE+") values (?,CURRENT_TIMESTAMP,?);";
}
