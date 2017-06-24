package br.com.jmedia.demo.services;

import br.com.jmedia.demo.dao.DemoPreferencesDao;
import br.com.jwheel.xml.model.FromXmlPreferences;
import br.com.jmedia.demo.model.DemoPreferences;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class WindowSizeChangeService
{
    private @Inject Logger             logger;
    private @Inject DemoPreferencesDao dao;

    private @Inject @FromXmlPreferences DemoPreferences demoPreferences;

    private void sizeChanged (int width, int height)
    {
        demoPreferences.setWidth(width);
        demoPreferences.setHeight(height);
        try
        {
            dao.merge(demoPreferences);
        }
        catch (IOException e)
        {
            logger.debug("Could not persist DemoPreferences!", e);
        }
    }

    public void widthChanged (int width)
    {
        sizeChanged(width, demoPreferences.getHeight());
    }

    public void heightChanged (int height)
    {
        sizeChanged(demoPreferences.getWidth(), height);
    }
}
