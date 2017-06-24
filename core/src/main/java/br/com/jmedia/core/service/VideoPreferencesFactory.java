package br.com.jmedia.core.service;

import br.com.jmedia.core.model.VideoPreferences;
import br.com.jwheel.xml.model.FromXmlPreferences;
import br.com.jwheel.xml.service.PreferencesFactoryFromXml;
import br.com.jmedia.core.dao.VideoPreferencesDao;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class VideoPreferencesFactory implements PreferencesFactoryFromXml<VideoPreferences>
{
    private @Inject VideoPreferencesDao dao;

    @Produces
    @FromXmlPreferences
    private VideoPreferences produce ()
    {
        return produce(dao);
    }

    @Override
    public VideoPreferences produceDefault ()
    {
        return new VideoPreferences();
    }
}
