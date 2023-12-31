package table;

public class SecretsTable {
    public static final String TABLE_NAME = "secrets";
    public static final String COLUMN_FILE_NAME = "filename";
    public static final String COLUMN_FILE_SECRET_KEY = "filesecretkey";
    public static final String COLUMN_FILE_EXTENSION = "fileextension";
    public static final String COLUMN_FILE_PATH = "filepath";
    public static final String COLUMN_FILE_ENCRYPTION = "fileencryption";
    public static final String QUERY_REGISTER = "INSERT INTO " + TABLE_NAME + " (" + COLUMN_FILE_NAME + "," + COLUMN_FILE_SECRET_KEY + "," + COLUMN_FILE_EXTENSION + "," + COLUMN_FILE_PATH + "," + COLUMN_FILE_ENCRYPTION + ") VALUES " + " (?,?,?,?,?);";
    public static final String QUERY_FETCH_KEY = "SELECT " + COLUMN_FILE_SECRET_KEY + "," +COLUMN_FILE_ENCRYPTION +" FROM " + TABLE_NAME + " WHERE " + COLUMN_FILE_PATH + "= ?";
    public static final String QUERY_DELETE_FILE = "DELETE FROM "+TABLE_NAME+" WHERE "+COLUMN_FILE_PATH+" ?;";
}
