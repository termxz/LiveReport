package io.termxz.spigot.database.mysql;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SQLRequest {

    private Connection connection;
    private String tableName;

    public SQLRequest(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = "`" + tableName + "`";
    }

    public void plainSubmit(String mySQL) {
        try (PreparedStatement ps = connection.prepareStatement(mySQL)){
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(connection.getMetaData().getURL());
        }
    }

    public Submit submit() {
        return new Submit();
    }

    public Retrieve retrieve() {
        return new Retrieve();
    }

    // Submit

    public class Retrieve {

        private StringBuilder statement = new StringBuilder();
        private String select;

        public Retrieve select(String select) {
            this.select = select;
            statement.append("SELECT ").append(select).append(" FROM ").append(tableName);
            return this;

        }

        public Retrieve where(String where, Object equals) {
            if(equals instanceof String) equals = "'" + equals + "'";
            statement.append(" WHERE ").append(where).append("=").append(equals);
            return this;
        }

        public Object execute() {
            try {
                PreparedStatement ps = connection.prepareStatement(statement.toString());
                ResultSet rs = ps.executeQuery();
                if(rs.next()) return rs.getObject(select);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Map<String, Object> selectAll(String where, Object equals) {
            Map<String, Object> map = new HashMap<>();

            if(equals instanceof String)equals = "'" + equals + "'";
            statement.append("SELECT * FROM ").append(tableName).append(" WHERE ").append(where).append("=").append(equals);

            try {

                PreparedStatement ps = connection.prepareStatement(statement.toString());
                ResultSet rs = ps.executeQuery();
                ResultSetMetaData rsm = rs.getMetaData();

                if(rs.next())
                    for (int i = 1; i <= rsm.getColumnCount(); i++) {
                        map.put(rsm.getColumnName(i), rs.getObject(i));
                    }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return map;
        }

        public List<Map<String, Object>> selectMulti() {
            List<Map<String, Object>> list = new ArrayList<>();

            statement.append("SELECT * FROM ").append(tableName);

            try {

                PreparedStatement ps = connection.prepareStatement(statement.toString());
                ResultSet rs = ps.executeQuery();
                ResultSetMetaData rsm = rs.getMetaData();

                while(rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 1; i <= rsm.getColumnCount(); i++)
                        map.put(rsm.getColumnName(i), rs.getObject(i));
                    list.add(map);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return list;
        }

    }


    public class Submit {

        private StringBuilder statement = new StringBuilder();

        public Submit insert(String format, List<Object> values) {
            insert(format);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < values.size(); i++) {
                Object obj = values.get(i);
                String value =
                        (obj instanceof String || obj instanceof ArrayList || obj instanceof LinkedList) ?
                                "'" + obj.toString() + "'" : obj.toString();
                sb.append(i != (values.size()-1) ? value + ", " : value);
            }

            statement.append("\nVALUES (").append(sb.toString()).append(")");
            return this;
        }

        public Submit insert(String format) {
            statement.append("INSERT INTO ").append(tableName).append(" ").append("(").append(format).append(")");
            return this;
        }

        public Submit update(Map<String, Object> map, String where, Object equals) {
            ConcurrentHashMap<String, Object> concMap = new ConcurrentHashMap<>(map);

            StringBuilder sb = new StringBuilder();

            concMap.entrySet().forEach(entry -> {

                String key = entry.getKey();
                String value = entry.getValue().toString();

                Object obj = entry.getValue();

                if(obj instanceof String || obj instanceof ArrayList ||
                        obj instanceof LinkedList) {

                    value = "'" + obj.toString() + "'";
                }

                concMap.remove(key);
                sb.append(key).append("=").append(value).append(concMap.size() != 0 ? ", " : "");
            });

            statement.append(String.format("UPDATE %s SET %s WHERE %s = %s",
                    tableName, sb.toString(), where,
                    equals instanceof String ? "'" + equals + "'" : equals.toString()));
            return this;
        }

        public void execute() {
            try (PreparedStatement ps = connection.prepareStatement(statement.toString())) {
                openConnection();
                //Bukkit.getLogger().info(statement.toString());
                ps.executeUpdate();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

}