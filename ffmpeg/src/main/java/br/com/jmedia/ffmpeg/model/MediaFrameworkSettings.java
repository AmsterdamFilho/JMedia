package br.com.jmedia.ffmpeg.model;

import br.com.jwheel.utils.StringUtils;
import br.com.jwheel.utils.SystemUtils;
import br.com.jmedia.core.model.InvalidPixelFormatException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class MediaFrameworkSettings
{
    private final String executableFolder;

    private String crf;
    private String frameRate;
    private int    height;
    private String pinNumber;
    /**
     * The pixel format being provided by the device
     */
    private String pixelFormat;
    private String preset;
    private String videoDevice;
    private int    width;

    private List<String> captureCommand;
    private List<String> encodeCommand;
    private List<String> recordCommand;

    //<editor-fold desc="Parameters IDs">

    public static final String CRF            = "{crf}";
    public static final String FILE_TO_RECORD = "{fileToRecord}";
    public static final String FRAME_RATE     = "{frameRate}";
    public static final String PIN_NUMBER     = "{pinNumber}";
    public static final String PIXEL_FORMAT   = "{pixelFormat}";
    public static final String PRESET         = "{preset}";
    public static final String VIDEO_DEVICE   = "{videoDevice}";
    public static final String VIDEO_SIZE     = "{videoSize}";

    //</editor-fold>

    public MediaFrameworkSettings (String executableFolder)
    {
        this.executableFolder = executableFolder;
    }

    public String getCrf ()
    {
        return crf;
    }

    public void setCrf (String crf)
    {
        this.crf = crf;
    }

    public String getFrameRate ()
    {
        return frameRate;
    }

    public void setFrameRate (String frameRate)
    {
        this.frameRate = frameRate;
    }

    public int getHeight ()
    {
        return height;
    }

    public void setHeight (int height)
    {
        this.height = height;
    }

    public String getPinNumber ()
    {
        return pinNumber;
    }

    public void setPinNumber (String pinNumber)
    {
        this.pinNumber = pinNumber;
    }

    public String getPixelFormat ()
    {
        return pixelFormat;
    }

    public void setPixelFormat (String pixelFormat)
    {
        this.pixelFormat = pixelFormat;
    }

    public String getPreset ()
    {
        return preset;
    }

    public void setPreset (String preset)
    {
        this.preset = preset;
    }

    public String getVideoDevice ()
    {
        return videoDevice;
    }

    public void setVideoDevice (String videoDevice)
    {
        this.videoDevice = videoDevice;
    }

    public int getWidth ()
    {
        return width;
    }

    public void setWidth (int width)
    {
        this.width = width;
    }

    public List<String> getCaptureCommand ()
    {
        return interpretCommandsList(captureCommand, null);
    }

    public void setCaptureCommand (List<String> captureCommand)
    {
        this.captureCommand = captureCommand;
    }

    public List<String> getEncodeCommand ()
    {
        return interpretCommandsList(encodeCommand, null);
    }

    public void setEncodeCommand (List<String> encodeCommand)
    {
        this.encodeCommand = encodeCommand;
    }

    public List<String> getRecordCommand (Path videoFilePath)
    {
        return interpretCommandsList(recordCommand, videoFilePath);
    }

    public void setRecordCommand (List<String> videoRecordCommand)
    {
        this.recordCommand = videoRecordCommand;
    }

    public List<String> initialCommandsList ()
    {
        List<String> response = new ArrayList<>();
        String executable = "ffmpeg";
        if (SystemUtils.isWindows())
        {
            executable += ".exe";
        }
        if (StringUtils.isNullOrEmpty(executableFolder))
        {
            response.add(executable);
        }
        else
        {
            // TODO: 01/04/17 test this situation
            response.add(Paths.get(executableFolder).resolve(executable).toString());
        }
        return response;
    }

    public int bytesPerFrame () throws InvalidPixelFormatException
    {
        int bytesPerPixel;
        if (getPixelFormat() == null)
        {
            throw new InvalidPixelFormatException();
        }
        switch (getPixelFormat())
        {
            //noinspection SpellCheckingInspection
            case "yuyv422":
                // TODO: 06/04/17 set the correct value
                bytesPerPixel = 4;
                break;
            case "bgr0":
                bytesPerPixel = 4;
                break;
            default:
                throw new InvalidPixelFormatException(getPixelFormat());
        }
        return getWidth() * getHeight() * bytesPerPixel;
    }

    private List<String> interpretCommandsList (List<String> commandsList, Path videoFilePath)
    {
        List<String> response = initialCommandsList();
        if (commandsList != null)
        {
            for (String command : commandsList)
            {
                command = replace(command, CRF, crf);
                command = replace(command, FILE_TO_RECORD, videoFilePath);
                command = replace(command, FRAME_RATE, frameRate);
                command = replace(command, PIN_NUMBER, pinNumber);
                command = replace(command, PIXEL_FORMAT, pixelFormat);
                command = replace(command, PRESET, preset);
                command = replace(command, VIDEO_DEVICE, videoDevice);
                command = replace(command, VIDEO_SIZE, width + "x" + height);
                response.add(command);
            }
        }
        return response;
    }

    private String replace (String command, String id, Object value)
    {
        if (value == null)
        {
            return command;
        }
        else
        {
            return command.replace(id, value.toString());
        }
    }
}
