package br.com.jmedia.demo.model;

import br.com.jwheel.xml.model.PathPreferences;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class MyPathPreferences extends PathPreferences
{
    @Override
    public String getRootFolderName ()
    {
        return "jmedia-test";
    }
}
