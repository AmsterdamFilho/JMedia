package br.com.jmedia.ffmpeg.dao;

import br.com.jmedia.ffmpeg.model.MediaFrameworkSettings;
import br.com.jwheel.xml.dao.GenericXStreamDao;
import com.thoughtworks.xstream.XStream;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class MediaFrameworkSettingsDao extends GenericXStreamDao<MediaFrameworkSettings>
{
    @Override
    protected XStream createXStream ()
    {
        XStream xStream = super.createXStream();
        xStream.alias("mediaFrameworkSettings", MediaFrameworkSettings.class);
        return xStream;
    }
}
