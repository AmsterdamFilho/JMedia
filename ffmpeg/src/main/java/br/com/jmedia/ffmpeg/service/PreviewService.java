package br.com.jmedia.ffmpeg.service;

import javafx.scene.image.WritableImage;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public interface PreviewService extends CaptureServiceClient
{
    WritableImage snapshot ();
}
