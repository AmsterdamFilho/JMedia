package br.com.jmedia.core.model;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public interface MediaListener
{
    /**
     * Notifies that a photo has been captured from the device. The persistence location may have not been defined yet,
     * but the photo image is already available.
     *
     * @param photo the photo object containing the photo bytes.
     */
    void photoCaptured (Photo photo);

    /**
     * Notifies that an audio file started being recorded.
     *
     * @param audioFile the audio File
     */
    void audioAdded (Path audioFile);

    /**
     * Notifies that a video file started being recorded.
     *
     * @param videoFile the video File
     */
    void videoAdded (Path videoFile);

    /**
     * Notifies that no media is being managed anymore.
     */
    void deselected ();

    /**
     * Notifies that a photo has been saved successfully to disk.
     */
    void photoSaved (Photo photo);

    /**
     * Notifies that a photo could not be saved to disk. This method is running on a separate thread.
     */
    void photoCouldNotBeSaved (Photo photo);

    /**
     * Notifies that the photo media for a procedure has been loaded. Will be running on its own thread.
     *
     * @param photos the photos
     */
    void photoLoaded (List<Photo> photos);

    /**
     * Notifies that the audio media for a procedure has been loaded. Will be running on its own thread.
     *
     * @param audioFiles the audio files
     */
    void audioLoaded (List<Path> audioFiles);

    /**
     * Notifies that the video media for a procedure has been loaded. Will be running on its own thread.
     *
     * @param videoFiles the video files
     */
    void videoLoaded (List<Path> videoFiles);
}
