package br.com.jmedia.core.model;

import java.nio.file.Path;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public interface MediaLocation
{
    /**
     * The root folder where the photos will be stored. Example: Some folder in the local network
     * hard drive.
     *
     * @return the photos root folder
     */
    Path defaultPhotoRoot ();

    /**
     * The root folder where the photos will be stored saving in the default root fails. Example:
     * Local filesystem user app data directory
     *
     * @return the photos root folder
     */
    Path alternativePhotoRoot ();

    /**
     * The root folder where the audios will be stored. Example: Some directory in the local hard drive
     *
     * @return the audios root folder
     */
    Path audioRoot ();

    /**
     * The root folder where the videos will be stored. Example: Some directory in the local hard drive
     *
     * @return the video root folder
     */
    Path videoRoot ();

    /**
     * The name of the folder where the procedure media will be stored.
     *
     * @return the name of the procedures folder
     */
    String proceduresFolderName ();

    /**
     * The name of the folder where the photos will be stored. Example: photos
     *
     * @return the photo folder name
     */
    String photoFolderName ();

    /**
     * The name of the folder where the audios will be stored. Example: audio
     *
     * @return the audio folder name
     */
    String audioFolderName ();

    /**
     * The name of the folder where the videos will be stored. Example: videos
     *
     * @return the video folder name
     */
    String videoFolderName ();
}
