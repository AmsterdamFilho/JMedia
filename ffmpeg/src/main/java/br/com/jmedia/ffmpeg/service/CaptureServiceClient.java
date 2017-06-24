package br.com.jmedia.ffmpeg.service;

import java.io.IOException;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public interface CaptureServiceClient
{
    void receive (int bytes, int index) throws IOException;

    void frameReceived ();

    void captureStopped ();
}
