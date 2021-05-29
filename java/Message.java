import java.util.Date;

public class Message {
    String token;
    String text;
    Date date;

    public Message(String token, String text, Date date) {
        this.token = token;
        this.text = text;
        this.date = date;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
