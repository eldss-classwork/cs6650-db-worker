package data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DataSource {

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;
    private static final int poolSize;

    static {
        Map<String, String> env = System.getenv();
        String dbUrl = "jdbc:mysql://" + env.get("DB_URL");
        String userName = env.get("DB_USER");
        String password = env.get("DB_PASS");
        poolSize = 60;

        config.setJdbcUrl(dbUrl);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername(userName);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("serverTimezone", "UTC");
        config.setMaximumPoolSize(poolSize);
        ds = new HikariDataSource(config);
    }

    private DataSource() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static int getPoolSize() {
        return poolSize;
    }
}
