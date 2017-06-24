package br.com.jmedia.ffmpeg.factory;

import br.com.jmedia.ffmpeg.service.DirectShow;
import br.com.jwheel.utils.SystemUtils;
import br.com.jwheel.weld.WeldContext;
import br.com.jmedia.ffmpeg.service.AvFoundation;
import br.com.jmedia.ffmpeg.service.MediaFramework;

import javax.enterprise.inject.Produces;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
final class MediaFrameworkFactory
{
    @Produces
    private MediaFramework produce ()
    {
        if (SystemUtils.isOsX())
        {
            return WeldContext.getInstance().getAny(AvFoundation.class);
        }
        else
        {
            return WeldContext.getInstance().getAny(DirectShow.class);
        }
    }
}
