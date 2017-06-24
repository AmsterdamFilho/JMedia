package br.com.jmedia.ffmpeg.model;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class NullFrameListener implements FrameListener
{
    @Override
    public boolean frameReceived ()
    {
        return true;
    }
}
