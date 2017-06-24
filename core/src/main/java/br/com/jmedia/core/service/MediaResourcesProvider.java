package br.com.jmedia.core.service;

import br.com.jwheel.utils.ResourceProvider;

/**
 * A provider for media messages that the MediaController might need to deliver to the user. Intended for i18n.
 *
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class MediaResourcesProvider extends ResourceProvider
{
    public String mediaDeviceNotFoundException (String mediaDevice)
    {
        return getI18nProperty("mediaDeviceNotFoundException") + " " + mediaDevice;
    }

    public String noMediaDeviceException ()
    {
        return getI18nProperty("noMediaDeviceException");
    }

    public String attemptToTakePhotoWhenIdle ()
    {
        return getI18nProperty("attemptToTakePhotoWhenIdle");
    }

    public String attemptToTakePhotoWithoutSettingProcedure ()
    {
        return getI18nProperty("attemptToTakePhotoWithoutSettingProcedure");
    }

    public String deviceConnectionLost ()
    {
        return getI18nProperty("deviceConnectionLost");
    }

    public String attemptToRecordWithoutSettingProcedure ()
    {
        return getI18nProperty("attemptToRecordWithoutSettingProcedure");
    }

    public String attemptToChangePreferencesWhileRecording ()
    {
        return getI18nProperty("attemptToChangePreferencesWhileRecording");
    }

    public String outOfDiskSpaceWhileRecording ()
    {
        return getI18nProperty("outOfDiskSpaceWhileRecording");
    }

    public String lostFileAccessWhileRecording ()
    {
        return getI18nProperty("lostFileAccessWhileRecording");
    }

    public String recordIsFinishing ()
    {
        return getI18nProperty("recordIsFinishing");
    }

    public String internalException ()
    {
        return getI18nProperty("internalException");
    }

    @Override
    protected String root ()
    {
        return "jmedia";
    }
}
