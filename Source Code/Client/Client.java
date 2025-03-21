import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Client extends Application {
    private MusicService musicService;
    private ListView<String> trackListView;
    private Button playPauseButton;
    private Button nextButton, prevButton;
    private Slider progressBar;
    private Label currentTimeLabel, totalTimeLabel;
    private List<String> tracks;
    private String currentTrack;
    private MediaPlayer mediaPlayer;
    private Path tempFilePath;
    private boolean atEndOfMedia = false;

    @Override
    public void start(Stage primaryStage) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            musicService = (MusicService) registry.lookup("MusicService");
            tracks = musicService.getTrackList();
            createAndShowGUI(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to connect to the music service.", Alert.AlertType.ERROR);
        }
    }

    private void createAndShowGUI(Stage stage) {
        trackListView = new ListView<>();
        trackListView.getItems().addAll(tracks);

        playPauseButton = new Button("Play");
        nextButton = new Button("Next");
        prevButton = new Button("Previous");

        progressBar = new Slider();
        progressBar.setMin(0);
        progressBar.setValue(0);

        currentTimeLabel = new Label("00:00");
        totalTimeLabel = new Label("00:00");

        HBox controls = new HBox(10, prevButton, playPauseButton, nextButton);
        controls.setAlignment(Pos.CENTER);

        HBox progressPane = new HBox(10, currentTimeLabel, progressBar, totalTimeLabel);
        progressPane.setAlignment(Pos.CENTER);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        VBox mainLayout = new VBox(10, trackListView, progressPane, controls);
        mainLayout.setPadding(new Insets(10));

        playPauseButton.setOnAction(e -> handlePlayPause());
        nextButton.setOnAction(e -> playNextTrack());
        prevButton.setOnAction(e -> playPreviousTrack());

        progressBar.setOnMousePressed(e -> {
            if (mediaPlayer != null) {
                progressBar.setValueChanging(true);
            }
        });

        progressBar.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                progressBar.setValueChanging(false);
                mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
            }
        });

        trackListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(currentTrack)) {
                playTrack(newValue);
            }
        });

        Scene scene = new Scene(mainLayout, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Music Player");
        stage.show();
    }

    // Toggles between playing and pausing the current track.
    private void handlePlayPause() {
        if (mediaPlayer == null) {
            if (!tracks.isEmpty()) {
                trackListView.getSelectionModel().select(0);
                playSelectedTrack();
            }
            return;
        }

        MediaPlayer.Status status = mediaPlayer.getStatus();

        if (status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
        } else if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.STOPPED || status == MediaPlayer.Status.READY) {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
        }
    }

    // Plays the selected track from the list.
    private void playSelectedTrack() {
        String trackName = trackListView.getSelectionModel().getSelectedItem();
        if (trackName != null) {
            playTrack(trackName);
        }
    }

    /**
     * Plays the specified track and resets playback position if a different track is selected.
     * @param trackName: The name of the track to play.
     */
    private void playTrack(String trackName) {
        stopTrack();
        currentTrack = trackName;
        playPauseButton.setText("Pause");

        try {
            byte[] trackData = musicService.getTrack(trackName);
            Map<String, String> metadata = musicService.getTrackMetadata(trackName);

            tempFilePath = Files.createTempFile("tempTrack", ".mp3");
            Files.write(tempFilePath, trackData, StandardOpenOption.WRITE);

            Media media = new Media(tempFilePath.toUri().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                Duration total = media.getDuration();
                progressBar.setMax(total.toSeconds());
                totalTimeLabel.setText(formatTime(total));
                mediaPlayer.play();
            });

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!progressBar.isValueChanging()) {
                    progressBar.setValue(newValue.toSeconds());
                    currentTimeLabel.setText(formatTime(newValue));
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                atEndOfMedia = true;
                playNextTrack();
            });

            progressBar.valueProperty().addListener((Observable ov) -> {
                if (progressBar.isValueChanging()) {
                    mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
                }
            });

            mediaPlayer.setOnPlaying(() -> playPauseButton.setText("Pause"));
            mediaPlayer.setOnPaused(() -> playPauseButton.setText("Play"));
            mediaPlayer.setOnStopped(() -> playPauseButton.setText("Play"));
            mediaPlayer.setOnError(() -> {
                String errorMessage = mediaPlayer.getError().getMessage();
                System.out.println("MediaPlayer Error: " + errorMessage);
                showAlert("Playback Error", "Cannot play the selected track.", Alert.AlertType.ERROR);
                stopTrack();
            });

            atEndOfMedia = false;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to play the selected track.", Alert.AlertType.ERROR);
        }
    }

    // Stops the current track and releases resources.

    private void stopTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        progressBar.setValue(0);
        currentTimeLabel.setText("00:00");

        if (currentTrack == null) {
            playPauseButton.setText("Play");
        }

        if (tempFilePath != null) {
            Path fileToDelete = tempFilePath;
            tempFilePath = null;
            deleteTempFileWithDelay(fileToDelete, 500);
        }
    }

    // Plays the next track in the list.
    private void playNextTrack() {
        int currentIndex = trackListView.getSelectionModel().getSelectedIndex();
        if (currentIndex < trackListView.getItems().size() - 1) {
            trackListView.getSelectionModel().select(currentIndex + 1);
            playSelectedTrack();
        } else {
            stopTrack();
        }
    }

    // Plays the previous track in the list.
    private void playPreviousTrack() {
        int currentIndex = trackListView.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            trackListView.getSelectionModel().select(currentIndex - 1);
            playSelectedTrack();
        } else {
            playSelectedTrack();
        }
    }

    /**
     * Formats a Duration object into a mm:ss string.
     * @param duration: The Duration to format.
     * @return A string in mm:ss format.
     */
    private String formatTime(Duration duration) {
        int intDuration = (int) Math.floor(duration.toSeconds());
        int minutes = intDuration / 60;
        int seconds = intDuration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Attempts to delete the temporary file after a specified delay.
     * @param path:  The path to the temporary file.
     * @param delay: The delay in milliseconds before attempting deletion.
     */
    private void deleteTempFileWithDelay(Path path, long delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                deleteTempFileWithRetries(path, 5, 200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Attempts to delete the temporary file with retries.
     * @param path:      The path to the temporary file.
     * @param attempts:  The number of attempts to delete the file.
     * @param waitTime:  The wait time in milliseconds between attempts.
     */
    private void deleteTempFileWithRetries(Path path, int attempts, long waitTime) {
        for (int i = 0; i < attempts; i++) {
            try {
                Files.deleteIfExists(path);
                System.out.println("Deleted temp file: " + path);
                return;
            } catch (IOException e) {
                System.err.println("Attempt " + (i + 1) + " to delete temp file failed: " + e.getMessage());
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    // Ignore interruption during sleep
                }
            }
        }
        System.err.println("Could not delete temp file after " + attempts + " attempts: " + path);
    }

    /**
     * Displays an alert dialog with the specified title, message, and alert type.
     * @param title:     The title of the alert.
     * @param message:   The message content of the alert.
     * @param alertType: The type of alert (e.g., ERROR, INFORMATION).
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType, message, ButtonType.OK);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
