import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import javax.swing.*;
import java.net.InetSocketAddress;
import java.util.*;

public class MyWebSocket extends WebSocketServer {

    private static final int PORT = 7777;

    DataBase db;
    Set<WebSocket> cons;
    ArrayList<User> users;
    ArrayList<Message> messages = new ArrayList<>();

    MyWebSocket() {
        super(new InetSocketAddress(PORT));
        cons = new HashSet<>();
        db = new DataBase();
        users = db.getAllUsers();
        messages = db.getAllMessages();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        cons.add(webSocket);
        System.out.println("New connection from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        cons.remove(webSocket);
        System.out.println("Closed connection to " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        JSONObject obj = JSON.parseObject(message);
        String type = obj.getString("type");

        JSONObject answer = new JSONObject();

        if (type.equals("reg")) {
            String login = obj.getString("name");
            String pass = obj.getString("pass");
            if (getUserByLogin(login) == null) {
                String token = generateToken();
                User user = new User(login, pass, token);
                users.add(user);
                db.writeNewUser(user);

                answer.put("type", "reg_success");
                answer.put("message", "Registration completed successfully. Please, log in now");
            } else {
                answer.put("type", "reg_error");
                answer.put("message", "This user already exists");
            }
            webSocket.send(answer.toString());
        } else if (type.equals("login")) {
            String login = obj.getString("name");
            String pass = obj.getString("pass");
            User user = getUserByLogin(login);

            if (user != null) {
                if (user.getPass().equals(pass)) {
                    answer.put("type", "login_success");
                    answer.put("token", user.getToken());
                    answer.put("message", "Log in completed successfully");
                } else {
                    answer.put("type", "login_error");
                    answer.put("message", "Password invalid");
                }
            } else {
                answer.put("type", "login_error");
                answer.put("message", "User invalid");
            }
            webSocket.send(answer.toString());
        } else if (type.equals("token_auth")) {
            String token = obj.getString("token");
            User user = getUserByToken(token);
            answer.put("type", "token_auth");
            answer.put("name", user.getLogin());
            webSocket.send(answer.toString());

            String allMessagesStr = createJsonFromAllMessages(user.getToken());
            webSocket.send(allMessagesStr);
        } else if (type.equals("chat_message")) {

            String token = obj.getString("token");
            String msg = obj.getString("text");
            long label = obj.getLong("label");

            User user = getUserByToken(token);
            if (user == null) return;

            Message mes = new Message(token, msg, new Date());
            messages.add(mes);

            //save to db
            db.writeNewMessage(mes);

            //send to all
            JSONObject sendMessage = new JSONObject();
            sendMessage.put("type", "chat_message");
            sendMessage.put("isMine", "false");
            sendMessage.put("text", msg);
            sendMessage.put("sender", user.getLogin());
            for(WebSocket socket : cons) {
                if (!socket.equals(webSocket)) {
                    socket.send(sendMessage.toString());
                }
            }

            answer.put("type", "sending_result");
            answer.put("result", "success");
            answer.put("label", Long.toString(label));
            webSocket.send(answer.toString());
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {

    }


    private String generateToken() {
        StringBuilder temp = new StringBuilder();
        String sym = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();

        for(int i = 0; i < 32; i++) {
            temp.append(sym.charAt(random.nextInt(sym.length())));
        }
        return temp.toString();
    }

    User getUserByLogin(String login) {
        for (User usr: users) {
            if (usr.getLogin().equals(login)) return usr;
        }
        return null;
    }

    User getUserByToken(String token) {
        for (User usr: users) {
            if (usr.getToken().equals(token)) return usr;
        }
        return null;
    }

    String createJsonFromAllMessages(String token) {
        JSONObject tempObj = new JSONObject();
        tempObj.put("type", "chat_history");
        JSONArray array = new JSONArray();
        for (Message mes : messages) {
            JSONObject temp = new JSONObject();
            if (token.equals(mes.getToken())) {
                temp.put("isMine", "true");
            } else {
                temp.put("isMine", "false");
            }
            temp.put("text", mes.getText());
            User user = getUserByToken(mes.getToken());
            temp.put("sender", user.getLogin());
            array.add(temp);
        }
        tempObj.put("messages", array);
        return tempObj.toString();
    }

}
