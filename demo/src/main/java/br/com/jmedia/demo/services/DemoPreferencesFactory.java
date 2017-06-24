package br.com.jmedia.demo.services;

import br.com.jmedia.demo.dao.DemoPreferencesDao;
import br.com.jwheel.xml.model.FromXmlPreferences;
import br.com.jwheel.xml.service.PreferencesFactoryFromXml;
import br.com.jmedia.demo.model.DemoPreferences;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public final class DemoPreferencesFactory implements PreferencesFactoryFromXml<DemoPreferences>
{
    private @Inject DemoPreferencesDao xStreamDao;

    @Produces
    @FromXmlPreferences
    private DemoPreferences produce ()
    {
        return produce(xStreamDao);
    }

    @Override
    public DemoPreferences produceDefault ()
    {
        DemoPreferences demoPreferences = new DemoPreferences();
        demoPreferences.setWidth(800);
        demoPreferences.setHeight(600);
        return demoPreferences;
    }
}
