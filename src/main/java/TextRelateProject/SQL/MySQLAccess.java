package TextRelateProject.SQL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MySQLAccess {
    private String db_name = null;
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    public void setConnection(String db_name){
        this.db_name = db_name;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/"+db_name+"?"
                            + "user=root&password=");
            statement = connect.createStatement();
        }
        catch(Exception e){

        }
    }
    public void deleteOldestPost() {
        try {
            resultSet = statement
                    .executeQuery("select ID from "+db_name+".wp_posts");
            int id = -1;
            while (resultSet.next()) {
                int current = resultSet.getInt("ID");
                if (id == -1 || current < id) {
                    id = current;
                }
            }
            deletePostByID(id);
        } catch (Exception e) {

        }
    }
    public void deletePostByID(int id){
        String SQL_line = "DELETE FROM `"+db_name+"`.`wp_posts` WHERE `ID`='"+id+"'";
        try {
            preparedStatement = connect.prepareStatement((SQL_line));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void deleteAllPosts(){
        try {
            resultSet = statement
                    .executeQuery("select ID from "+db_name+".wp_posts");

            while (resultSet.next()) {
                deletePostByID(resultSet.getInt("ID"));
            }

        } catch (Exception e) {

        }

    }

    public void addTextToDB(String text, String title) throws Exception {
        try {
//            // Result set get the result of the SQL query
            resultSet = statement
                    .executeQuery("select MAX(ID) as ID from "+db_name+".wp_posts");
            int id = -1;
            while (resultSet.next()) {
                id =  resultSet.getInt("ID");
            }
            if (id ==-1){
                id =0; //there is plus 1, (id+1)
            }

            String new_link = "http://localhost:8080/wordpress/?p="+(id+1);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            dtf.format(now);
            String SQL_line = "INSERT INTO `"+db_name+"`.`wp_posts` (`ID`, `post_author`, `post_date`, `post_date_gmt`, `post_content`, `post_title`, `post_excerpt`, `post_status`, `comment_status`, `ping_status`, `post_password`, `post_name`, `to_ping`, `pinged`, `post_modified`, `post_modified_gmt`, `post_content_filtered`, `post_parent`, `guid`, `menu_order`, `post_type`, `post_mime_type`, `comment_count`) VALUES ('"+(id+1)+"', '1', '"+now+"', '"+now+"', '"+text+"', '"+title+"', '', 'publish', 'open', 'open', '', '"+"post-"+(id+1)+"', '', '', '"+now+"', '"+now+"', '', '0', '"+new_link+"', '0', 'post','','0')";
            preparedStatement = connect.prepareStatement((SQL_line));
            preparedStatement.executeUpdate();
//            writeResultSet(resultSet);


        } catch (Exception e) {
            throw e;
        }
    }

    // You need to close the resultSet
    public void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {

        }
    }

}