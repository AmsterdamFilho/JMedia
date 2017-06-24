package br.com.jmedia.ffmpeg.service;

import br.com.jmedia.ffmpeg.model.MediaFrameworkSettings;
import br.com.jmedia.core.control.MediaFacadeClient;
import br.com.jwheel.weld.Custom;
import br.com.jmedia.core.model.NoMediaDeviceException;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
@Custom
public class DirectShow extends MediaFramework
{
    private @Inject Logger logger;

    @Override
    public void parsePreviewError (String previewErrorMessage, MediaFacadeClient mediaFacadeClient)
    {
        // TODO: 03/04/17 implement this method
        // (check if deviceNotFound can be thrown)
        mediaFacadeClient.previewingException();
    }

    @Override
    public void parseRecordError (String recordErrorMessage, MediaFacadeClient mediaFacadeClient)
    {
        // TODO: 03/04/17 implement this method
        // (check if lostFileAccessWhileRecording or outOfDiskSpaceWhileRecording can be thrown)
        mediaFacadeClient.recordingException();
    }

    //<editor-fold desc="Template methods">

    @Override
    void guessDesiredVideoSettings (MediaFrameworkSettings settings, String listDevicesCommandResult) throws
            IOException, NoMediaDeviceException
    {
        HashMap<String, String> videoDevices = new HashMap<>();
        boolean listingVideoDevices = false;
        String lastVideoDevice = null;
        for (String line : listDevicesCommandResult.split("\n"))
        {
            logger.debug("Parsing line: " + line);
            if (line.contains("DirectShow audio devices"))
            {
                break;
            }
            else if (listingVideoDevices)
            {
                if (lastVideoDevice == null)
                {
                    if (line.matches("\\[dshow @ [^\"]+][^\"]+\"[^\"]+\""))
                    {
                        lastVideoDevice = line.substring(line.indexOf("\"") + 1, line.length() - 1);
                    }
                }
                else
                {
                    if (line.matches("\\[dshow @ [^\"]+][^\"]+Alternative name[^\"]+\"[^\"]+\""))
                    {
                        videoDevices.put(lastVideoDevice, line.substring(line.indexOf("\"") + 1, line.length() - 1));
                        logger.debug("Video device added: " + lastVideoDevice);
                        lastVideoDevice = null;
                    }
                }
            }
            else if (line.contains("DirectShow video devices"))
            {
                listingVideoDevices = true;
            }
        }
        guessAndSetDesiredSettings(settings, guessDesiredVideoDevice(videoDevices));

    }

    @Override
    List<String> getListDevicesCommand (MediaFrameworkSettings settings)
    {
        List<String> listDevicesCommand = settings.initialCommandsList();
        listDevicesCommand.add("-f");
        listDevicesCommand.add(filterName());
        listDevicesCommand.add("-list_devices");
        listDevicesCommand.add("true");
        listDevicesCommand.add("-i");
        listDevicesCommand.add("dummy");
        return listDevicesCommand;
    }

    @Override
    String filterName ()
    {
        //noinspection SpellCheckingInspection
        return "dshow";
    }

    @Override
    String inputVideoDeviceCommand ()
    {
        return "video=\"" + MediaFrameworkSettings.VIDEO_DEVICE + "\"";
    }

    @Override
    String crossbarVideoInputPinNumberCommand ()
    {
        return "-crossbar_video_input_pin_number";
    }

    //</editor-fold>

    private String guessDesiredVideoDevice (HashMap<String, String> videoDevices) throws NoMediaDeviceException
    {
        if (videoDevices.isEmpty())
        {
            throw new NoMediaDeviceException();
        }
        videoDevices.forEach((name, alternativeName) ->
                logger.debug("Found video device " + name + ": " + alternativeName));
        //noinspection SpellCheckingInspection
        List<String> devicesOrderedByPriority =
                Arrays.asList("Conexant Polaris Video Capture", "AVerMedia HD Capture", "");
        for (String priorityDeviceName : devicesOrderedByPriority)
        {
            for (Map.Entry<String, String> videoDevice : videoDevices.entrySet())
            {
                if (videoDevice.getKey().contains(priorityDeviceName))
                {
                    return videoDevice.getValue();
                }
            }
        }
        logger.error("Assertion error", new AssertionError());
        return "";
    }

    private void guessAndSetDesiredSettings (MediaFrameworkSettings settings, String videoDevice) throws IOException
    {
        // TODO: 03/04/17 implement this method
        settings.setFrameRate("30");
        settings.setHeight(480);
        settings.setPinNumber("");
        //noinspection SpellCheckingInspection
        settings.setPixelFormat("yuyv422");
        settings.setVideoDevice(videoDevice);
        settings.setWidth(640);
    }
}
