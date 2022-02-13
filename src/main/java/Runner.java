import java.sql.Connection;

public class Runner {

    public static void main(String[] args) {

        ConnectionPool connectionPool = null;

        try {
            connectionPool = BasicConnectionPool.newFixedThreadPool("org.h2.Driver",
                    "jdbc:h2:mem:test", "user", "password", 3);
        } catch (Exception e) {
            System.out.println("Exception ocurred while creating an instanc of connection pool " + e);
        }

        try {
            assert connectionPool != null;
            Connection a = connectionPool.getConnection();
            System.out.println("Connection instance is " + a);
        } catch (Exception e) {
            System.out.println("Exception ocurred while getting instance " + e);
        }

        try {
            Connection b = connectionPool.getConnection();
            System.out.println("Connection instance is " + b);
        } catch (Exception e) {
            System.out.println("Exception ocurred while getting instance " + e);
        }

        try {
            Connection c = connectionPool.getConnection();
            System.out.println("Connection instance is " + c);
            ConnectionPool finalConnectionPool = connectionPool;
            new Thread(() -> {
                try {
                    Thread.sleep(990);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finalConnectionPool.releaseConnection(c);
            }).start();
        } catch (Exception e) {
            System.out.println("Exception ocurred while getting instance " + e);
        }

        try {
            Connection d = connectionPool.getConnection(1000);
            System.out.println("Connection instance is " + d);
        } catch (Exception e) {
            System.out.println("Exception ocurred while getting instance " + e);
        }
        try {
            Connection e = connectionPool.getConnection();
            System.out.println("Connection " + e);
        } catch (Exception e) {
            System.out.println("Exception ocurred while getting instance " + e);
        }

    }
}
