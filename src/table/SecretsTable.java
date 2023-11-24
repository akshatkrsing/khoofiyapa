package table;

public class SecretsTable {
    public static final String TABLE_NAME="secrets";
    public static final String COLUMN_FILE_NAME="filename";
    public static final String COLUMN_FILE_SECRET_KEY="filesecretkey";
    public static final String COLUMN_FILE_EXTENSION="fileextension";
    public static final String COLUMN_FILE_PATH="filepath";
    public static final String COLUMN_FILE_ENCRYPTION="fileencryption";
    public static final String QUERY_INSERT_TO_SECRETS_TABLE= "insert into "+TABLE_NAME+"("+COLUMN_FILE_NAME+","+COLUMN_FILE_SECRET_KEY +")VALUES(?,?)";

}
