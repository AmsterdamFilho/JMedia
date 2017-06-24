package br.com.jmedia.demo.view;

import br.com.jmedia.core.control.VideoController;
import br.com.jmedia.core.model.MediaControllerState;
import br.com.jmedia.core.model.MediaControllerStateListener;
import br.com.jmedia.core.model.MediaListener;
import br.com.jmedia.core.model.Photo;
import br.com.jmedia.core.service.MediaManager;
import br.com.jmedia.core.service.MediaMessagesCourier;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class VideoDemo extends BorderPane implements MediaControllerStateListener, MediaListener
{
    private @Inject VideoController      videoController;
    private @Inject MediaMessagesCourier messagesCourier;
    private @Inject MediaManager         mediaManager;

    private final ObservableList<String> photoFiles   = FXCollections.observableArrayList();
    private final List<Photo>            photoObjects = new ArrayList<>();

    private final CheckBox chkVideoEnabled = new CheckBox("Video enabled");

    private final Button btnStartStopRecording   = new Button();
    private final Button btnPauseResumeRecording = new Button();

    private final Button btnPhoto = new Button("Take a photo");

    private final ListView<String> proceduresView = new ListView<>();
    private final ListView<String> photosView     = new ListView<>(photoFiles);
    private final ListView<String> videoView      = new ListView<>();

    @PostConstruct
    private void init ()
    {
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-background-color: black");
        ImageView imageView = new ImageView();
        stackPane.getChildren().add(imageView);

        videoController.setView(imageView, () -> Platform.runLater(() -> chkVideoEnabled.setSelected(false)));

        setTop(createPageStartComponent());
        setCenter(stackPane);
        setRight(createLineEndComponent());

        videoController.addStateListener(this);
        stateChanged(videoController.getState());

        mediaManager.addListener(this);

        configureComponents();

        if (videoController.isMediaEnabled())
        {
            videoController.startPreview();
        }

        proceduresView.setItems(FXCollections.observableArrayList(Arrays.asList("1", "2", "3")));
        proceduresView.getSelectionModel().select(0);
    }

    private void configureComponents ()
    {
        chkVideoEnabled.setSelected(videoController.isMediaEnabled());

        chkVideoEnabled.setOnAction(e ->
        {
            if (!videoController.setEnabled(chkVideoEnabled.isSelected()))
            {
                chkVideoEnabled.setSelected(!chkVideoEnabled.isSelected());
            }
        });

        btnStartStopRecording.setOnAction(e -> videoController.startOrStopRecording());
        btnPauseResumeRecording.setOnAction(e -> videoController.pauseOrResumeRecording());

        btnPhoto.setOnAction(e -> videoController.takePhoto());

        proceduresView.getSelectionModel().selectedItemProperty().addListener(new ProcedureSelectionListener());
    }

    private Node createPageStartComponent ()
    {
        TitledPanel recordingPanel = new TitledPanel("Recording", false);
        recordingPanel.addToGrid(btnStartStopRecording);
        recordingPanel.addToGrid(btnPauseResumeRecording);

        Button btnDiagnosis = new Button("Diagnosis");
        btnDiagnosis.setOnAction(e -> diagnosisAction());

        Button btnSettings = new Button("Settings");
        btnSettings.setOnAction(e -> videoController.showSettings());

        HBox hBox = new HBox(8, chkVideoEnabled, recordingPanel, btnPhoto,
                btnDiagnosis, btnSettings);
        hBox.setFillHeight(true);
        return hBox;
    }

    private Node createLineEndComponent ()
    {
        TitledPanel proceduresPanel = new TitledPanel("Procedures list", true);
        proceduresPanel.addToGrid(proceduresView);

        TitledPanel photosPanel = new TitledPanel("Photos", true);
        photosPanel.addToGrid(photosView);

        TitledPanel videoPanel = new TitledPanel("Video panel", true);
        videoPanel.addToGrid(videoView);

        VBox vBox = new VBox(8, proceduresPanel, photosPanel, videoPanel);
        vBox.setFillWidth(true);
        return vBox;
    }

    @Override
    public void stateChanged (MediaControllerState newState)
    {
        Platform.runLater(() ->
        {
            switch (newState)
            {
                case IDLE:

                    btnStartStopRecording.setDisable(true);
                    btnStartStopRecording.setText("Start");
                    btnPauseResumeRecording.setDisable(true);
                    btnPauseResumeRecording.setText("Pause");

                    break;

                case PREVIEWING:

                    btnStartStopRecording.setDisable(false);
                    btnStartStopRecording.setText("Start");
                    btnPauseResumeRecording.setDisable(true);
                    btnPauseResumeRecording.setText("Pause");

                    break;

                case PAUSED_PREVIEWING:

                    btnStartStopRecording.setDisable(false);
                    btnStartStopRecording.setText("Start");
                    btnPauseResumeRecording.setDisable(true);
                    btnPauseResumeRecording.setText("Pause");

                    break;

                case RECORDING:

                    btnStartStopRecording.setDisable(false);
                    btnStartStopRecording.setText("Stop");
                    btnPauseResumeRecording.setDisable(true);
                    btnPauseResumeRecording.setText("Pause");

                    break;

                case PAUSED_RECORDING:

                    btnStartStopRecording.setDisable(false);
                    btnStartStopRecording.setText("Stop");
                    btnPauseResumeRecording.setDisable(false);
                    btnPauseResumeRecording.setText("Resume");

                    break;
            }
        });
    }

    @Override
    public void deselected ()
    {
        Platform.runLater(() ->
        {
            photoFiles.clear();
            photoObjects.clear();
            videoView.getItems().clear();
        });
    }

    @Override
    public void photoCaptured (Photo photo)
    {
        Platform.runLater(() ->
        {
            photoFiles.add("...");
            photoObjects.add(photo);
        });
    }

    @Override
    public void audioAdded (Path audioFile)
    {
    }

    @Override
    public void videoAdded (Path videoFile)
    {
        Platform.runLater(() -> videoView.getItems().add(videoFile.toString()));
    }

    @Override
    public void photoSaved (Photo photo)
    {
        Platform.runLater(() -> photoFiles.set(photoObjects.indexOf(photo), photo.getPath().toString()));
    }

    @Override
    public void photoCouldNotBeSaved (Photo photo)
    {
        Platform.runLater(() ->
        {
            Platform.runLater(() -> photoFiles.remove(photoObjects.indexOf(photo)));
            messagesCourier.showErrorMessage("Could not save photo to any location!");
        });
    }

    @Override
    public void photoLoaded (List<Photo> photos)
    {
        photoObjects.addAll(photos);
        Platform.runLater(() -> photoFiles.addAll(photos.stream().map(photo -> photo.getPath().toString()).collect
                (Collectors.toList())));
    }

    @Override
    public void audioLoaded (List<Path> audioFiles)
    {
    }

    @Override
    public void videoLoaded (List<Path> videoFiles)
    {
        List<String> videoPaths = new ArrayList<>();
        videoFiles.forEach(path -> videoPaths.add(path.toString()));
        Platform.runLater(() -> videoView.getItems().addAll(videoPaths));
    }

    private void diagnosisAction ()
    {
        videoController.pausePreview();
        new FxDiagnosisDialog().showAndWait();
        videoController.resumePreview();
    }

    public void exitEvent ()
    {
        videoController.dispose();
    }

    private class TitledPanel extends BorderPane
    {
        private final Pane componentsPanel;

        private TitledPanel (String title, boolean vertical)
        {
            if (vertical)
            {
                componentsPanel = new VBox(5);
            }
            else
            {
                componentsPanel = new HBox(5);
            }
            Label label = new Label();
            label.setAlignment(Pos.BASELINE_CENTER);
            label.setStyle("-fx-font-weight: bold");
            label.setAlignment(Pos.BASELINE_CENTER);
            label.setText(title);

            setTop(label);
            setCenter(componentsPanel);
        }

        private void addToGrid (Node node)
        {
            componentsPanel.getChildren().add(node);
        }
    }

    private class ProcedureSelectionListener implements ChangeListener<String>
    {
        private boolean enabled = true;

        @Override
        public void changed (ObservableValue<? extends String> observable, String oldValue, String newValue)
        {
            if (enabled)
            {
                switch (videoController.getState())
                {
                    case IDLE:
                    case PREVIEWING:
                    case PAUSED_PREVIEWING:
                        if (newValue == null)
                        {
                            mediaManager.deselect();
                        }
                        else
                        {
                            mediaManager.setSelected(Integer.valueOf(newValue));
                        }
                        break;
                    case RECORDING:
                    case PAUSED_RECORDING:
                        messagesCourier.showWarningMessage("Can not change procedure while recording!");
                        enabled = false;
                        Platform.runLater(() -> proceduresView.getSelectionModel().select(oldValue));
                }
            }
            else
            {
                enabled = true;
            }
        }
    }

    private class FxDiagnosisDialog extends Stage
    {
        private FxDiagnosisDialog ()
        {
            initModality(Modality.APPLICATION_MODAL);
            setTitle("Diagnosis");

            StackPane contentPane = new StackPane();
            contentPane.getChildren().add(new HTMLEditor());
            setScene(new Scene(contentPane));
        }
    }
}
