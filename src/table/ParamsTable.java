package table;

public class ParamsTable {
    public static final String TABLE_NAME="secrets";
    public static final String COLUMN_PARAM_NAME = "paramName";
    public static final String COLUMN_PARAM_VALUE = "paramValue";
    public static final String QUERY_FETCH_ROOT_FOLDER_PATH = "select "+COLUMN_PARAM_VALUE+
            " from "+TABLE_NAME+" where "+COLUMN_PARAM_NAME+"=?;";
}
