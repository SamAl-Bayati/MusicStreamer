import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface MusicService extends Remote {
    List<String> getTrackList() throws RemoteException;
    byte[] getTrack(String trackName) throws RemoteException;
    Map<String, String> getTrackMetadata(String trackName) throws RemoteException;
    List<String> searchTracks(String query) throws RemoteException;
    void rateTrack(String trackName, int rating) throws RemoteException;
}
