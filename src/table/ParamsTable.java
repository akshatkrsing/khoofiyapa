package table;

public class ParamsTable {
    public static final String TABLE_NAME="params";
    public static final String COLUMN_PARAM_NAME = "paramName";
    public static final String COLUMN_PARAM_VALUE = "paramValue";
    public static final String QUERY_FETCH_PARAM_VALUE = "select "+COLUMN_PARAM_VALUE+
            " from "+TABLE_NAME+" where "+COLUMN_PARAM_NAME+"=?;";
    public static final String QUERY_UPDATE_PARAM_VALUE="UPDATE "+TABLE_NAME+" SET "+COLUMN_PARAM_VALUE+"=? WHERE "+COLUMN_PARAM_NAME+" =?;";


    public static String QUERY_VERIFY="SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_PARAM_NAME+" =? AND "+ COLUMN_PARAM_VALUE+"= ?;";;

    public static final String QUERY_CHANGE_PASSWORD= "UPDATE "+ TABLE_NAME + " SET "+ COLUMN_PARAM_VALUE +
            " = ? WHERE "+ COLUMN_PARAM_NAME + " = ? AND "+ COLUMN_PARAM_VALUE +" = ?;";
}