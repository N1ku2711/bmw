public class Main {
    public static void main(String[] args) {
        MyWebSocket server = new MyWebSocket();
        server.start();
        System.out.println("Server started on address " + server.getAddress());
    }
}
