package br.com.jmedia.ffmpeg.control;

import br.com.jmedia.ffmpeg.dao.MediaFrameworkSettingsDao;
import br.com.jmedia.ffmpeg.model.MediaFrameworkSettings;
import br.com.jmedia.ffmpeg.service.CaptureService;
import br.com.jmedia.ffmpeg.service.EncodeService;
import br.com.jmedia.ffmpeg.service.MediaFramework;
import br.com.jmedia.core.control.MediaFacadeClient;
import br.com.jmedia.core.control.VideoFacade;
import br.com.jmedia.core.model.NoMediaDeviceException;
import br.com.jwheel.weld.WeldContext;
import br.com.jwheel.xml.model.FromXmlPreferences;
import br.com.jmedia.ffmpeg.service.PreviewService;
import com.google.common.base.Strings;
import com.thoughtworks.xstream.XStreamException;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;

/**
 * A Media Facade implementation based on an ffmpeg
 *
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
@Singleton
public class FfmpegFacade implements VideoFacade
{
    private @Inject Logger                    logger;
    private @Inject MediaFrameworkSettingsDao dao;
    private @Inject MediaFramework            mediaFramework;

    private CaptureService captureService;
    private PreviewService previewService;

    public static final String SUPPORTED_PIXEL_FORMAT = "bgr0";

    @Override
    public void startPreviewing (MediaFacadeClient client, ImageView view) throws IOException,
            NoMediaDeviceException, InterruptedException
    {
        MediaFrameworkSettings settings = getCurrentSettings();
        // check if a video device has been chosen
        if (Strings.isNullOrEmpty(settings.getVideoDevice()))
        {
            // it has not, choose one or throw an exception
            mediaFramework.loadInitialSettings(settings);
            try
            {
                logger.info("Video device {" + settings.getVideoDevice() + "} has been chosen. Persisting...");
                dao.merge(settings);
            }
            catch (XStreamException | IOException e)
            {
                logger.warn("Could not persist MediaFrameworkSettings!", e);
            }
        }
        logger.info("A video device has been chosen. Starting capture...");
        captureService = WeldContext.getInstance().getAny(CaptureService.class);
        captureService.start(settings, client);
        VideoPreview videoPreview = new VideoPreview(view, settings.getWidth(), settings.getHeight());
        if (SUPPORTED_PIXEL_FORMAT.equals(settings.getPixelFormat()))
        {
            logger.info("Captured pixel format is supported. Preview directly...");
            previewService = videoPreview;
        }
        else
        {
            logger.info("Captured pixel format is not supported! Creating encode process...");
            EncodeService encodeService = WeldContext.getInstance().getAny(EncodeService.class);
            encodeService.start(settings, videoPreview);
            previewService = encodeService;
        }
        captureService.addClient(previewService);
    }

    @Override
    public void stopPreviewing ()
    {
        captureService.stop();
        captureService = null;
        previewService = null;
    }

    @Override
    public void pausePreview ()
    {
        captureService.removeClient(previewService);
    }

    @Override
    public void resumePreview ()
    {
        captureService.addClient(previewService);
    }

    @Override
    public WritableImage takePhotoFromPreview ()
    {
        return previewService.snapshot();
    }

    //<editor-fold desc="Later">

    @Override
    public void showSettingsDialog ()
    {

    }

    @Override
    public void startRecording (Path videoFilePath) throws IOException
    {
    }

    @Override
    public void pauseRecording ()
    {
    }

    @Override
    public void stopRecording ()
    {
    }

    @Override
    public void resumeRecording ()
    {
    }

    //</editor-fold>

    private MediaFrameworkSettings getCurrentSettings ()
    {
        return WeldContext.getInstance().getWithQualifiers(MediaFrameworkSettings.class, new
                AnnotationLiteral<FromXmlPreferences>() {});
    }
}
