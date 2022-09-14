import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

/**
 * Class having various methods to interact with connection pool.
 * Following lazy initialization principle.
 */
public class BasicConnectionPool implements ConnectionPool {
    private final Driver driver;
    private final String jdbcUrl;
    private final String userName;
    private final String password;
    private int size;
    private final int maximumPoolSize;
    private final Queue<Connection> connectionsPoll;
    
    private static BasicConnectionPool basicConnectionPoolInstance = null;

    private BasicConnectionPool(String driverClassName, String jdbcUrl, String userName, String password, int maximumPoolSize)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        Class<?> c = Class.forName(driverClassName);
        this.driver = (Driver) c.newInstance();
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.password = password;
        this.size = 0;
        this.maximumPoolSize = maximumPoolSize;
        this.connectionsPoll = new LinkedList<>();
    }

    /**
     * Public method to get an instance of connection pool class.
     * Following Singelton design pattern so that only one instance of connection pool exists
     **/
    public static BasicConnectionPool newFixedThreadPool(String driverClassName, String jdbcUrl, String userName,
                                                         String password, int maximumPoolSize)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            
            if(basicConnectionPoolInstance == null) {
                synchronized(BasicConnectionPool.class) {
                    if(basicConnectionPoolInstance == null) {
                        basicConnectionPoolInstance = new BasicConnectionPool(driverClassName, jdbcUrl, userName, password, maximumPoolSize);
                    }
                }
            }
        
        return basicConnectionPoolInstance;
    }

    @Override
    public Connection getConnection() throws SQLException, InterruptedException {
        return getConnection(0L);
    }

    @Override
    public Connection getConnection(long timeout) throws SQLException, InterruptedException {
        long timeStamp = System.currentTimeMillis() + timeout;
        boolean createNewConnection = false;

        synchronized (this) {
            while (connectionsPoll.isEmpty()) {
                if (size < maximumPoolSize) {
                    size++;
                    createNewConnection = true;
                    break;
                } else {
                    this.wait(Math.max(timeStamp - System.currentTimeMillis(), 1L));
                    if (timeStamp <= System.currentTimeMillis()) {
                        throw new SQLException("Timeout, connection not availiable");
                    }
                }
            }
            if (!createNewConnection) {
                return connectionsPoll.poll();
            }
        }
        return createNewConnection();
    }

    /**
     * Create a new JDBC connection object
     *
     * @return Connection
     * @throws SQLException
     */
    private Connection createNewConnection() throws SQLException {
        try {

            Properties properties = new Properties();
            properties.put("user", userName);
            properties.put("password", password);
            return driver.connect(jdbcUrl, properties);
        } catch (Throwable t) {
            synchronized (this) {
                size--;
                this.notifyAll();
            }
            throw new SQLException("Not able to create exception ", t);
        }
    }

    @Override
    public void releaseConnection(Connection connection) {
        synchronized (this) {
            connectionsPoll.offer(connection);
            this.notifyAll();
        }
    }
}
