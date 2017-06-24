package br.com.jmedia.demo.model;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class MediaLocationProvider
{
    private String defaultPhotoRoot;
    private String alternativePhotoRoot;
    private String audioRoot;
    private String videoRoot;

    public String getDefaultPhotoRoot ()
    {
        return defaultPhotoRoot;
    }

    public void setDefaultPhotoRoot (String defaultPhotoRoot)
    {
        this.defaultPhotoRoot = defaultPhotoRoot;
    }

    public String getAlternativePhotoRoot ()
    {
        return alternativePhotoRoot;
    }

    public void setAlternativePhotoRoot (String alternativePhotoRoot)
    {
        this.alternativePhotoRoot = alternativePhotoRoot;
    }

    public String getAudioRoot ()
    {
        return audioRoot;
    }

    public void setAudioRoot (String audioRoot)
    {
        this.audioRoot = audioRoot;
    }

    public String getVideoRoot ()
    {
        return videoRoot;
    }

    public void setVideoRoot (String videoRoot)
    {
        this.videoRoot = videoRoot;
    }
}
