package br.com.jmedia.core.control;

import br.com.jmedia.core.model.NoMediaDeviceException;
import javafx.scene.Node;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The MediaFacade is a high level class abstracting all media requests. Its methods are really state dependent, but
 * the class is not required to validate its state before executing a request, because there is a controller responsible
 * for that. The MediaFacade should log with great precision, from TRACE to ERROR messages. Logging is not
 * necessary if an expected Exception is thrown, for the client who invoked the method will handle it.
 *
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public interface MediaFacade<T extends Node>
{
    /**
     * Starts previewing the media.
     * <p>
     * For audio: The audio device used, if there are no saved preferences for that, should be the system's default. The
     * previewing of audio is just a visual effect, like in Skype Preferences for audio, showing the user that the
     * program is aware of the audio being captured by the device. The preview should occur in the facade MediaPanel.
     * <p>
     * For video: The video device used, if there are no saved preferences for that, should be the the first video
     * device found with channels / source feature. The previewing of video is the continuous image being captured from
     * the video device in a non UI blocking thread. This preview should occur in the facade MediaPanel. Low latency is
     * very important.
     * <p>
     * If any Exception occurs after the method returned successfully, the MediaFacadeClient should be notified
     * accordingly.
     *
     * @throws NoMediaDeviceException if no media device has been found
     * @throws IOException            if an IOException occurs
     */
    void startPreviewing (MediaFacadeClient client, T view) throws IOException, NoMediaDeviceException,
            InterruptedException;

    /**
     * Stops the preview. The media device is disconnected and all resources are released.
     */
    void stopPreviewing ();

    /**
     * Pauses the previewing. Pausing does not disconnect from the device, it just saves as much resources as possible
     * until resume is called.
     */
    void pausePreview ();

    /**
     * Resumes the previewing that was paused.
     */
    void resumePreview ();

    /**
     * Starts recording the media being previewed to the file determined by the mediaFilePath parameter. The file
     * should be
     * compressed simultaneously. The compression should favor maximum quality over file size, but the smaller the size
     * the better.
     * <p>
     * If any Exception occurs after the method returned successfully, the file should still be playable up to the point
     * it was possible to record. The MediaFacadeClient should be notified accordingly.
     *
     * @param mediaFilePath the file to be created
     * @throws IOException if an IOException occurs
     */
    void startRecording (Path mediaFilePath) throws IOException;

    /**
     * Pauses the recording happening at the moment. The preview continues normally.
     */
    void pauseRecording ();

    /**
     * Resumes the recording that was paused.
     */
    void resumeRecording ();

    /**
     * Stops the recording happening at the moment, closing the connection to the created file. The preview continues
     * normally.
     */
    void stopRecording ();

    /**
     * Shows the media devices settings dialog. If the MediaFacade had previous preferences saved, it should load them
     * and configure the dialog with it.
     * For video, there are at least controls for selecting the video device (Capture Card 1, Capture Card 2,
     * Webcam 1 etc.); the format (1280x720, 720x480 etc.); the channel, if the device supports it (HDMI, VGA, S-Video
     * etc.); brightness, contrast, hue, saturation and other supported filters; and FPS (25, 30 etc.) The more options
     * the settings dialog offers, the better.
     * <p>
     * For audio, there must be at least controls for selecting the device and format, but, again, the more the better.
     * <p>
     * The dialog should be modal, but it does not need to belong to the same view framework as the MediaPanel. The
     * MediaFacade might show a native OS dialog, for example. On the other hand, the MediaPanel should continue or
     * even start a previewing, so the user can see the modifications while making them, when changing the brightness
     * for example. The MediaFacade might be idle or previewing when this method is called, but it will not be
     * recording or playing a file.
     * <p>
     * Whatever changes the user makes in this dialog should be automatically persisted locally. When the user exits the
     * system or restarts the OS, the next time this MediaFacade is instantiated, it should load the preferences
     * and use
     * them automatically. The preferences persistence should use JWheel's xml preferences persistence API.
     */
    void showSettingsDialog ();
}
