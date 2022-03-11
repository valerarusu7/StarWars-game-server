package Model.mediator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class was implemented by Steffen.
 */
public class Database {
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost/";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private String url;
    private String user;
    private String pw;
    private Connection connection;

    public Database(String driver, String url, String user, String pw) throws ClassNotFoundException {
        this.url = url;
        this.user = user;
        this.pw = pw;
        this.connection = null;
        Class.forName(driver);
    }

    public Database(String databaseName, String user, String pw) throws ClassNotFoundException {
        this("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/" + databaseName, user, pw);
    }

    public Database(String databaseName) throws ClassNotFoundException {
        this("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/" + databaseName, "root", "");
    }

    public Database() throws ClassNotFoundException {
        this("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/", "root", "");
    }

    private void openDatabase() throws SQLException {
        this.connection = DriverManager.getConnection(this.url, this.user, this.pw);
    }

    private void closeDatabase() throws SQLException {
        this.connection.close();
    }

    public ArrayList<Object[]> query(String sql, Object... statementElements) throws SQLException {
        this.openDatabase();
        PreparedStatement statement = null;
        ArrayList<Object[]> list = null;
        ResultSet resultSet = null;
        if (sql != null && statement == null) {
            statement = this.connection.prepareStatement(sql);
            if (statementElements != null) {
                for (int i = 0; i < statementElements.length; ++i) {
                    statement.setObject(i + 1, statementElements[i]);
                }
            }
        }

        resultSet = statement.executeQuery();
        list = new ArrayList();

        while (resultSet.next()) {
            Object[] row = new Object[resultSet.getMetaData().getColumnCount()];

            for (int i = 0; i < row.length; ++i) {
                row[i] = resultSet.getObject(i + 1);
            }

            list.add(row);
        }

        if (resultSet != null) {
            resultSet.close();
        }

        if (statement != null) {
            statement.close();
        }

        this.closeDatabase();
        return list;
    }

    public int update(String sql, Object... statementElements) throws SQLException {
        this.openDatabase();
        PreparedStatement statement = this.connection.prepareStatement(sql);
        int i;
        if (statementElements != null) {
            for (i = 0; i < statementElements.length; ++i) {
                statement.setObject(i + 1, statementElements[i]);
            }
        }

        i = statement.executeUpdate();
        this.closeDatabase();
        return i;
    }

    public int updateWithID(String sql, Object... statementElements) throws SQLException {
        this.openDatabase();

        PreparedStatement statement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        if (statementElements != null) {
            for (int i = 0; i < statementElements.length; ++i) {
                statement.setObject(i + 1, statementElements[i]);
            }
        }

        int affectedRows = statement.executeUpdate();

        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            else {
                throw new SQLException("Creating object failed, no ID obtained.");
            }
        }
    }

    public int[] updateAll(ArrayList<String> sqlList) throws SQLException {
        if (sqlList == null) {
            return null;
        } else {
            this.openDatabase();
            int[] results = new int[sqlList.size()];

            for (int i = 0; i < sqlList.size(); ++i) {
                PreparedStatement statement = this.connection.prepareStatement(sqlList.get(i));
                results[i] = statement.executeUpdate();
            }

            this.closeDatabase();
            return results;
        }
    }

    public int[] updateAll(String fileName) throws SQLException, FileNotFoundException {
        ArrayList<String> sqlList = this.readFile(fileName, ";");
        return this.updateAll(sqlList);
    }

    private ArrayList<String> readFile(String filename, String deliminator) throws FileNotFoundException {
        Scanner input = new Scanner(new FileInputStream(filename));
        ArrayList<String> list = new ArrayList();
        String sql = "";

        while (true) {
            while (input.hasNext()) {
                sql = sql + input.nextLine();
                if (deliminator != null && !sql.trim().endsWith(deliminator)) {
                    if (sql.length() > 0) {
                        sql = sql + "\n";
                    }
                } else {
                    list.add(sql);
                    sql = "";
                }
            }

            input.close();
            return list;
        }
    }
}
