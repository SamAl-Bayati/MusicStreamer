import java.io.*;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import javax.sound.sampled.*;
import org.tritonus.share.sampled.file.TAudioFileFormat;

public class MusicServiceImpl extends UnicastRemoteObject implements MusicService {
    private static final String MUSIC_DIR = "music/";

    protected MusicServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<String> getTrackList() throws RemoteException {
        File musicDir = new File(MUSIC_DIR);
        File[] files = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        List<String> trackList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                trackList.add(file.getName());
            }
        }
        return trackList;
    }

    @Override
    public byte[] getTrack(String trackName) throws RemoteException {
        try {
            File file = new File(MUSIC_DIR + trackName);
            if (file.exists() && !file.isDirectory()) {
                return Files.readAllBytes(file.toPath());
            } else {
                throw new RemoteException("File not found");
            }
        } catch (IOException e) {
            throw new RemoteException("Error reading file", e);
        }
    }

    @Override
    public Map<String, String> getTrackMetadata(String trackName) throws RemoteException {
        Map<String, String> metadata = new HashMap<>();
        try {
            File file = new File(MUSIC_DIR + trackName);
            AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
            if (baseFileFormat instanceof TAudioFileFormat) {
                Map<String, Object> properties = ((TAudioFileFormat) baseFileFormat).properties();

                // Retrieve Duration
                Long microseconds = (Long) properties.get("duration");
                metadata.put("Duration", microseconds != null ? String.valueOf(microseconds / 1_000_000) : "180");

                // Retrieve Bitrate
                Object bitrateObj = properties.get("bitrate");
                if (bitrateObj == null) {
                    bitrateObj = properties.get("bitrate.nominal.bps");
                }
                metadata.put("Bitrate", bitrateObj != null ? bitrateObj.toString() : "192000");

                // Retrieve Total Bytes
                metadata.put("TotalBytes", String.valueOf(file.length()));
            } else {
                metadata.put("Duration", "180");
                metadata.put("Bitrate", "192000");
                metadata.put("TotalBytes", String.valueOf(file.length()));
            }
        } catch (Exception e) {
            metadata.put("Duration", "180");
            metadata.put("Bitrate", "192000");
            metadata.put("TotalBytes", "0");
        }
        metadata.put("TrackName", trackName);
        metadata.put("Artist", "Unknown Artist");
        metadata.put("Album", "Unknown Album");
        return metadata;
    }

    @Override
    public List<String> searchTracks(String query) throws RemoteException {
        List<String> allTracks = getTrackList();
        List<String> results = new ArrayList<>();
        for (String track : allTracks) {
            if (track.toLowerCase().contains(query.toLowerCase())) {
                results.add(track);
            }
        }
        return results;
    }

    @Override
    public void rateTrack(String trackName, int rating) throws RemoteException {
        System.out.println("Track " + trackName + " rated with " + rating + " stars.");
    }
}
