package br.com.jmedia.ffmpeg.service;

import br.com.jmedia.ffmpeg.model.MediaFrameworkSettings;
import br.com.jmedia.core.control.MediaFacadeClient;
import br.com.jmedia.core.model.NoMediaDeviceException;
import br.com.jwheel.weld.Custom;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
@Custom
public class AvFoundation extends MediaFramework
{
    private @Inject Logger logger;

    @Override
    public void parsePreviewError (String previewErrorMessage, MediaFacadeClient mediaFacadeClient)
    {
        Pattern pattern = Pattern.compile("([0-9]+)(:.+: Input/output error)");
        Matcher matcher = pattern.matcher(previewErrorMessage);
        if (matcher.find() && previewErrorMessage.contains("Invalid device index"))
        {
            mediaFacadeClient.deviceNotFound(matcher.group(1));
        }
        else
        {
            mediaFacadeClient.previewingException();
        }
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
        HashMap<Integer, String> videoDevices = new HashMap<>();
        boolean listingVideoDevices = false;
        for (String line : listDevicesCommandResult.split("\n"))
        {
            logger.debug("Parsing line: " + line);
            if (line.contains("AVFoundation audio devices:"))
            {
                break;
            }
            else if (listingVideoDevices)
            {
                if (line.matches("\\[AVFoundation input device @ [^]]+] \\[[0-9]+] [^]]+"))
                {
                    videoDevices.put(
                            Integer.valueOf(line.substring(line.indexOf("]") + 3, line.lastIndexOf("]"))),
                            line.substring(line.lastIndexOf("]") + 2));
                }
            }
            else if (line.contains("AVFoundation video devices:"))
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
        listDevicesCommand.add("\"\"");
        return listDevicesCommand;
    }

    @Override
    String filterName ()
    {
        return "avfoundation";
    }

    @Override
    String inputVideoDeviceCommand ()
    {
        return MediaFrameworkSettings.VIDEO_DEVICE + ":none";
    }

    @Override
    String crossbarVideoInputPinNumberCommand ()
    {
        // TODO: 04/04/17 implement this method
        return "";
    }

    //</editor-fold>

    private String guessDesiredVideoDevice (HashMap<Integer, String> videoDevices) throws NoMediaDeviceException
    {
        if (videoDevices.isEmpty())
        {
            throw new NoMediaDeviceException();
        }
        videoDevices.forEach((index, description) -> logger.debug("Found video device " + index + ": " + description));
        // TODO: 20/03/17 implement this method (find most likely desired device and choose it)
        return "0";
    }

    private void guessAndSetDesiredSettings (MediaFrameworkSettings settings, String videoDevice) throws IOException
    {
        // TODO: 03/04/17 implement this method
        settings.setFrameRate("30");
        settings.setHeight(720);
        //noinspection SpellCheckingInspection
        settings.setPixelFormat("yuyv422");
        settings.setVideoDevice(videoDevice);
        settings.setWidth(1280);
    }
}
