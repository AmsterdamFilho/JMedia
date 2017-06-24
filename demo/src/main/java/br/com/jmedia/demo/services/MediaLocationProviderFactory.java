package br.com.jmedia.demo.services;

import br.com.jwheel.xml.model.FromXmlPreferences;
import br.com.jwheel.xml.model.PathPreferences;
import br.com.jwheel.xml.service.PreferencesFactoryFromXml;
import br.com.jmedia.demo.dao.MediaLocationProviderDao;
import br.com.jmedia.demo.model.MediaLocationProvider;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.nio.file.Path;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public final class MediaLocationProviderFactory implements PreferencesFactoryFromXml<MediaLocationProvider>
{
    private @Inject MediaLocationProviderDao dao;
    private @Inject PathPreferences          pathPreferences;

    @Produces
    @FromXmlPreferences
    public MediaLocationProvider produce ()
    {
        return produce(dao);
    }

    @Override
    public MediaLocationProvider produceDefault ()
    {
        MediaLocationProvider mediaLocationProvider = new MediaLocationProvider();
        Path appDataDirectory = pathPreferences.getAppDataDirectory();
        mediaLocationProvider.setAlternativePhotoRoot(null);
        mediaLocationProvider.setAudioRoot(appDataDirectory.toString());
        mediaLocationProvider.setDefaultPhotoRoot(appDataDirectory.toString());
        mediaLocationProvider.setVideoRoot(appDataDirectory.toString());
        return mediaLocationProvider;
    }
}