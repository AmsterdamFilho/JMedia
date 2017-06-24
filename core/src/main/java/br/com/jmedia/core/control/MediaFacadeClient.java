package br.com.jmedia.core.control;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public interface MediaFacadeClient
{
    void deviceConnectionLost ();

    void deviceNotFound (String device);

    void outOfDiskSpaceWhileRecording ();

    void lostFileAccessWhileRecording ();

    void recordingFinished ();

    void previewingException ();

    void recordingException ();
}
