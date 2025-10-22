import java.io.*;
import java.net.*;
import java.util.*;

// ================================================
// 💻 ChatSphere – Simple Chat Application in Java
// ================================================
public class ChatSphere {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== ChatSphere ===");
        System.out.println("1. Start Server");
        System.out.println("2. Start Client");
        System.out.print("Enter choice: ");
        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) {
            new ChatServer().startServer();
        } else if (choice == 2) {
            new ChatClient().startClient();
        } else {
            System.out.println("Invalid choice!");
        }
    }
}

// ================================================
// 🖥 SERVER CLASS
// ================================================
class ChatServer {
    private static final int PORT = 1234;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public void startServer() {
        System.out.println("🔵 ChatSphere Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("✅ New client connected: " + socket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(socket, clientHandlers);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("❌ Server Error: " + e.getMessage());
        }
    }
}

// ================================================
// 🧩 CLIENT HANDLER CLASS (used by server threads)
// ================================================
class ClientHandler implements Runnable {
    private Socket socket;
    private Set<ClientHandler> clientHandlers;
    private PrintWriter writer;
    private String userName;

    public ClientHandler(Socket socket, Set<ClientHandler> clientHandlers) {
        this.socket = socket;
        this.clientHandlers = clientHandlers;
    }

    @Override
    public void run() {
        try (
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
        ) {
            this.writer = writer;

            writer.println("Enter your name: ");
            userName = reader.readLine();
            broadcast("🟢 " + userName + " has joined the chat!");

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("exit")) {
                    break;
                }
                broadcast("💬 " + userName + ": " + clientMessage);
            }

            socket.close();
            clientHandlers.remove(this);
            broadcast("🔴 " + userName + " has left the chat.");

        } catch (IOException e) {
            System.out.println("⚠ Error in ClientHandler: " + e.getMessage());
        }
    }

    private void broadcast(String message) {
        for (ClientHandler handler : clientHandlers) {
            handler.writer.println(message);
        }
    }
}

// ================================================
// 💬 CLIENT CLASS
// ================================================
class ChatClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;

    public void startClient() {
        try (
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        ) {
            System.out.println("🟢 Connected to ChatSphere Server!");

            // Thread to read messages from server
            new Thread(() -> {
                String response;
                try {
                    while ((response = reader.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.out.println("❌ Connection closed.");
                }
            }).start();

            // Sending messages to server
            String text;
            while ((text = input.readLine()) != null) {
                writer.println(text);
                if (text.equalsIgnoreCase("exit")) {
                    System.out.println("👋 You left the chat.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ Error connecting to server: " + e.getMessage());
        }
    }
}