package TextRelateProject.SQL;

/**
 * Created by Admin on 09-Mar-17.
 */

    /**
     * Mysql Utilities
     *
     * @author Ralph Ritoch <rritoch@gmail.com>
     * @copyright Ralph Ritoch 2011 ALL RIGHTS RESERVED
     * @link http://www.vnetpublishing.com
     *
     */

    public class MySQLUtils {

        /**
         * Escape string to protected against SQL Injection
         * <p>
         * You must add a single quote ' around the result of this function for data,
         * or a backtick ` around table and row identifiers.
         * If this function returns null than the result should be changed
         * to "NULL" without any quote or backtick.
         *
         * @param str
         * @return
         * @throws Exception
         */

        public static String mysql_real_escape_string(String str)
                throws Exception {
            if (str == null) {
                return null;
            }

            if (str.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/? ]", "").length() < 1) {
                return str;
            }

            String clean_string = str;
            clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
            clean_string = clean_string.replaceAll("\\n", "\\\\n");
            clean_string = clean_string.replaceAll("\\r", "\\\\r");
            clean_string = clean_string.replaceAll("\\t", "\\\\t");
            clean_string = clean_string.replaceAll("\\00", "\\\\0");
            clean_string = clean_string.replaceAll("'", "\\\\'");
            clean_string = clean_string.replaceAll("\\\"", "\\\\\"");

            if (clean_string.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/?\\\\\"' ]"
                    , "").length() < 1) {
                return clean_string;
            }
            return clean_string;
        }

    }

