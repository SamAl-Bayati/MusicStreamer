import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            MusicServiceImpl obj = new MusicServiceImpl();

            Registry registry = LocateRegistry.createRegistry(1099); // Default RMI port
            registry.bind("MusicService", obj);

            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }
}
