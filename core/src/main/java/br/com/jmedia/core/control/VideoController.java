package br.com.jmedia.core.control;

import br.com.jmedia.core.model.MediaControllerState;
import br.com.jmedia.core.model.VideoPreferences;
import br.com.jmedia.core.service.MediaManager;
import br.com.jwheel.xml.model.FromXmlPreferences;
import br.com.jmedia.core.dao.VideoPreferencesDao;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class VideoController extends MediaController<ImageView>
{
    private @Inject VideoFacade         videoFacade;
    private @Inject MediaManager        mediaManager;
    private @Inject VideoPreferencesDao dao;
    private @Inject Logger              logger;

    private @Inject @FromXmlPreferences VideoPreferences preferences;

    private ImageView imageView;
    private Runnable  disabledListener;

    /**
     * Sets the imageView where the video will be previewed and the disabledListener, that will be notified when
     * an error occurs while previewing
     *
     * @param imageView        the imageView
     * @param disabledListener the disabledListener
     */
    public void setView (ImageView imageView, Runnable disabledListener)
    {
        this.imageView = imageView;
        this.disabledListener = disabledListener;
    }

    public void takePhoto ()
    {
        if (MediaControllerState.IDLE.equals(getState()))
        {
            messagesCourier.showInfoMessage(messagesProvider.attemptToTakePhotoWhenIdle());
        }
        else
        {
            if (mediaManager.hasSelection())
            {
                mediaManager.photoCaptured(videoFacade.takePhotoFromPreview());
            }
            else
            {
                messagesCourier.showInfoMessage(messagesProvider.attemptToTakePhotoWithoutSettingProcedure());
            }
        }
    }

    @Override
    public boolean isMediaEnabled ()
    {
        return preferences.isEnabled();
    }

    //<editor-fold desc="Template design pattern">

    @Override
    ImageView getMediaPanel ()
    {
        return imageView;
    }

    @Override
    Runnable getMediaHasBeenDisabledListener ()
    {
        return disabledListener;
    }

    @Override
    MediaFacade<ImageView> getMediaFacade ()
    {
        return videoFacade;
    }

    @Override
    void setMediaEnabledAndMerge (boolean mediaEnabled)
    {
        preferences.setEnabled(mediaEnabled);
        try
        {
            dao.merge(preferences);
        }
        catch (IOException e)
        {
            logger.error("Could not persist video capture preferences!", e);
        }
    }

    @Override
    Path suggestNewMediaFile () throws IOException
    {
        return mediaManager.suggestNewVideoFilePath();
    }

    @Override
    void notifyMediaAdded (Path mediaFile)
    {
        mediaManager.videoAdded(mediaFile);
    }

    //</editor-fold>
}
