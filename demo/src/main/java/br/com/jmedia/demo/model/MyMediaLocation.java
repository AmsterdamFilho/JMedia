package br.com.jmedia.demo.model;

import br.com.jmedia.core.model.MediaLocation;
import br.com.jwheel.xml.model.FromXmlPreferences;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class MyMediaLocation implements MediaLocation
{
    private @Inject @FromXmlPreferences MediaLocationProvider mediaLocationProvider;

    @Override
    public Path defaultPhotoRoot ()
    {
        return Paths.get(mediaLocationProvider.getDefaultPhotoRoot());
    }

    @Override
    public Path alternativePhotoRoot ()
    {
        String alternativePhotoRoot = mediaLocationProvider.getAlternativePhotoRoot();
        if (alternativePhotoRoot == null)
        {
            return null;
        }
        return Paths.get(alternativePhotoRoot);
    }

    @Override
    public Path audioRoot ()
    {
        return Paths.get(mediaLocationProvider.getAudioRoot());
    }

    @Override
    public Path videoRoot ()
    {
        return Paths.get(mediaLocationProvider.getVideoRoot());
    }

    @Override
    public String proceduresFolderName ()
    {
        return "procedures";
    }

    @Override
    public String photoFolderName ()
    {
        return "photos";
    }

    @Override
    public String audioFolderName ()
    {
        return "audio";
    }

    @Override
    public String videoFolderName ()
    {
        return "video";
    }
}
