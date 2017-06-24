package br.com.jmedia.ffmpeg.factory;

import br.com.jmedia.ffmpeg.dao.MediaFrameworkSettingsDao;
import br.com.jmedia.ffmpeg.model.MediaFrameworkSettings;
import br.com.jwheel.xml.model.FromXmlPreferences;
import br.com.jwheel.xml.service.PreferencesFactoryFromXml;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
@Singleton
public final class MediaFrameworkSettingsFactory implements PreferencesFactoryFromXml<MediaFrameworkSettings>
{
    private @Inject MediaFrameworkSettingsDao dao;

    @Produces
    @FromXmlPreferences
    private MediaFrameworkSettings produce ()
    {
        return produce(dao);
    }

    @Override
    public MediaFrameworkSettings produceDefault ()
    {
        return new MediaFrameworkSettings("");
    }
}
