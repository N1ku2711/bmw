import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DataBase {
    final String URL = "jdbc:postgresql://localhost:5432/WebChat";
    final String USER = "postgres";
    final String PASS = "Niku2711";

    public void writeNewUser(User user) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement statement = conn.createStatement()) {
            String sql = "INSERT INTO users (login, password, token) VALUES " +
                    "('" + user.getLogin() + "', '" + user.getPass() + "', '" + user.getToken() + "');";
            statement.execute(sql);
            System.out.println("User " + user.getLogin() + "was added to database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> result = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users")){

            while (resultSet.next()) {
                result.add(new User(resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getString(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void writeNewMessage(Message message) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement statement = conn.createStatement()) {
            String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(message.getDate());
            String sql = "INSERT INTO messages (text, token, date) VALUES " +
                    "('" + message.getText() + "', '" + message.getToken() + "', '" + date + "');";
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Message> getAllMessages() {
        ArrayList<Message> result = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM messages")){

            while (resultSet.next()) {
                result.add(new Message(resultSet.getString(3),
                        resultSet.getString(2),
                        new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(resultSet.getString(4))));
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    User getUserByToken(String token) {
        User user = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE token = '" + token + "'")){
            while (resultSet.next()) {
                user = new User(resultSet.getString("login"),
                                resultSet.getString("password"),
                                resultSet.getString("token"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}
