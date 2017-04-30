package TextRelateProject.SQL;

public class MainSQL {
    public static void main(String[] args) throws Exception {
        MySQLAccess dao = new MySQLAccess();
        dao.setConnection("tomer_db");
//        dao.addTextToDB("yes","title-tomer");
        //dao.deletePostByID(3);
        dao.deleteAllPosts();
        dao.close();
    }

}