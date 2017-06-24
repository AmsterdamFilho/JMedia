package br.com.jmedia.core.control;

import br.com.jmedia.core.model.MediaControllerState;
import br.com.jmedia.core.model.MediaControllerStateListener;
import br.com.jmedia.core.model.NoMediaDeviceException;
import br.com.jmedia.core.service.MediaManager;
import br.com.jmedia.core.service.MediaMessagesCourier;
import br.com.jmedia.core.service.MediaResourcesProvider;
import javafx.scene.Node;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller for Media events from the view. This class is responsible for managing the various states of the media
 * application. It is a bridge between the view and the MediaFacade. This bridge protects the MediaFacade from
 * receiving requests in an inconsistent time or with inconsistent parameters, only transmitting those requests if the
 * MediaControllerState is compatible and the parameter is acceptable.
 * <p>
 * This controller is also responsible for notifying the user if anything goes wrong and logging eventual errors.
 *
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public abstract class MediaController<T extends Node> implements MediaFacadeClient
{
    protected @Inject Logger logger;

    protected @Inject MediaResourcesProvider messagesProvider;
    protected @Inject MediaMessagesCourier   messagesCourier;
    protected @Inject MediaManager           mediaManager;

    private MediaControllerState state    = MediaControllerState.IDLE;
    private Delegate             delegate = new IdleDelegate();

    private final List<MediaControllerStateListener> listeners = new ArrayList<>();

    public MediaControllerState getState ()
    {
        return state;
    }

    public void addStateListener (MediaControllerStateListener listener)
    {
        listeners.add(listener);
    }

    private void changeState (MediaControllerState newState)
    {
        if (!Objects.equals(state, newState))
        {
            delegate = getDelegate(newState);
            logger.info("MediaController state changed from: " + state.name() + " to " + newState.name() + ".");
            this.state = newState;
            listeners.forEach(listener -> listener.stateChanged(newState));
        }
    }

    private Delegate getDelegate (MediaControllerState newState)
    {
        if (newState == null)
        {
            logger.error("Unexpected MediaControllerState: null!");
            throw new AssertionError();
        }
        switch (newState)
        {
            case IDLE:
                return new IdleDelegate();
            case PREVIEWING:
                return new PreviewingDelegate();
            case PAUSED_PREVIEWING:
                return new PausedPreviewingDelegate();
            case RECORDING:
                return new RecordingDelegate();
            case PAUSED_RECORDING:
                return new PausedRecordingDelegate();
        }
        logger.error("Unexpected MediaControllerState: " + newState.name());
        throw new AssertionError();
    }

    /**
     * Sets the enabled property. It will only work if there is no media being recorded in the moment. If newEnabled is
     * false, the media stops being previewed. Automatically persists the preference if its return value is true. If
     * newEnabled is true, the media will start being previewed automatically.
     *
     * @param newEnabled the value to set
     * @return if newEnabled if false, return true. If newEnabled is true, return true if media could be enabled.
     */
    public boolean setEnabled (boolean newEnabled)
    {
        return delegate.setEnabled(newEnabled);
    }

    /**
     * Validates and starts previewing in the MediaFacade media panel.
     *
     * @see MediaFacade#startPreviewing
     */
    public void startPreview ()
    {
        delegate.startPreview();
    }

    /**
     * Validates and shows the media settings.
     *
     * @see MediaFacade#showSettingsDialog
     */
    public void showSettings ()
    {
        delegate.showSettings();
    }

    /**
     * Validates and, if necessary, dispose the MediaFacade.
     */
    public void dispose ()
    {
        delegate.dispose();
    }

    /**
     * Validates and, if possible, invokes the MediaFacade corresponding method.
     *
     * @see MediaFacade#startRecording
     */
    public void startOrStopRecording ()
    {
        delegate.startStopRecording();
    }

    /**
     * Validates and, if possible, invokes the MediaFacade corresponding method.
     *
     * @see MediaFacade#resumeRecording
     */
    public void pauseOrResumeRecording ()
    {
        delegate.pauseResumeRecording();
    }

    /**
     * If state is adequate, invokes the MediaFacade pausePreview method. Ignores the request otherwise. This method is
     * not intended to be requested by the user. The application is who decides when to pause preview to save resources.
     *
     * @see MediaFacade#pausePreview()
     */
    public void pausePreview ()
    {
        delegate.pausePreview();
    }

    /**
     * If state is adequate, invokes the MediaFacade resumePreview method. Ignores the request otherwise. This method is
     * not intended to be requested by the user. The application is who decides when to resume the preview that was
     * paused to save resources.
     *
     * @see MediaFacade#resumePreview()
     */
    public void resumePreview ()
    {
        delegate.resumePreview();
    }

    //<editor-fold desc="Media Facade Client methods">

    @Override
    public void deviceConnectionLost ()
    {
        delegate.deviceConnectionLost();
    }

    @Override
    public void deviceNotFound (String device)
    {
        delegate.deviceNotFound(device);
    }

    @Override
    public void outOfDiskSpaceWhileRecording ()
    {
        delegate.outOfDiskSpaceWhileRecording();
    }

    @Override
    public void lostFileAccessWhileRecording ()
    {
        delegate.lostFileAccessWhileRecording();
    }

    @Override
    public void recordingException ()
    {
        delegate.recordingException();
    }

    @Override
    public void recordingFinished ()
    {
        delegate.recordingFinished();
    }

    @Override
    public void previewingException ()
    {
        delegate.previewingException();
    }

    //</editor-fold>

    //<editor-fold desc="Template design pattern">

    abstract MediaFacade<T> getMediaFacade ();

    abstract boolean isMediaEnabled ();

    abstract void setMediaEnabledAndMerge (boolean mediaEnabled);

    abstract Path suggestNewMediaFile () throws IOException;

    abstract void notifyMediaAdded (Path mediaFile);

    abstract T getMediaPanel ();

    abstract Runnable getMediaHasBeenDisabledListener ();

    //</editor-fold>

    /**
     * State design pattern.
     */
    private abstract class Delegate implements MediaFacadeClient
    {
        abstract boolean setEnabled (boolean newEnabled);

        abstract void startPreview ();

        abstract void showSettings ();

        abstract void dispose ();

        abstract void startStopRecording ();

        abstract void pauseResumeRecording ();

        abstract void pausePreview ();

        abstract void resumePreview ();

        //<editor-fold desc="Media Facade Client">

        @Override
        public void deviceConnectionLost ()
        {
            logUnexpectedMethodCall("deviceConnectionLost");
        }

        @Override
        public void deviceNotFound (String device)
        {
            logUnexpectedMethodCall("deviceNotFound");
        }

        @Override
        public void outOfDiskSpaceWhileRecording ()
        {
            logUnexpectedMethodCall("outOfDiskSpaceWhileRecording");
        }

        @Override
        public void lostFileAccessWhileRecording ()
        {
            logUnexpectedMethodCall("lostFileAccessWhileRecording");
        }

        @Override
        public void recordingException ()
        {
            logUnexpectedMethodCall("recordingException");
        }

        @Override
        public void recordingFinished ()
        {
            logUnexpectedMethodCall("recordingFinished");
        }

        @Override
        public void previewingException ()
        {
            logUnexpectedMethodCall("previewingException");
        }

        //</editor-fold>

        /**
         * Logs that a method was called in an inadequate time, probably because the view did not block access to a
         * function according to the controller state.
         */
        void logUnexpectedMethodCall (String method)
        {
            logger.warn("Unexpected method call!", new IllegalStateException(method + " invoked in " +
                    getClass().getSimpleName() + "!"));
        }
    }

    private class IdleDelegate extends Delegate
    {
        @Override
        boolean setEnabled (boolean newEnabled)
        {
            boolean oldEnabled = isMediaEnabled();
            if (oldEnabled != newEnabled)
            {
                if (oldEnabled)
                {
                    setMediaEnabledAndMerge(false);
                    return true;
                }
                else
                {
                    if (startPreviewingOk())
                    {
                        changeState(MediaControllerState.PREVIEWING);
                        setMediaEnabledAndMerge(true);
                        return true;
                    }
                    return false;
                }
            }
            return true;
        }

        @Override
        void startPreview ()
        {
            if (startPreviewingOk())
            {
                changeState(MediaControllerState.PREVIEWING);
            }
        }

        @Override
        void showSettings ()
        {
            getMediaFacade().showSettingsDialog();
        }

        @Override
        void dispose ()
        {
        }

        @Override
        void startStopRecording ()
        {
            logUnexpectedMethodCall("startStopRecording");
        }

        @Override
        void pauseResumeRecording ()
        {
            logUnexpectedMethodCall("pauseResumeRecording");
        }

        @Override
        void pausePreview ()
        {
            // clients may call this method trying to save resources unaware that the Controller is already idle.
            // So, just ignore the request
        }

        @Override
        void resumePreview ()
        {
            // clients may call this method trying to resume the preview they paused trying to save resources.
            // So, just ignore the request
        }

        /**
         * Requests that the MediaFacade start previewing.
         *
         * @return true if the nothing went wrong. False otherwise.
         */
        private boolean startPreviewingOk ()
        {
            try
            {
                logger.info("Request to start previewing...");
                getMediaFacade().startPreviewing(MediaController.this, getMediaPanel());
                return true;
            }
            catch (NoMediaDeviceException e)
            {
                logger.warn("No device detected!", e);
                messagesCourier.showErrorMessage(messagesProvider.noMediaDeviceException());
            }
            catch (IOException e)
            {
                logger.error("IOException while trying to start preview!", e);
                messagesCourier.showErrorMessage(messagesProvider.internalException());
            }
            catch (InterruptedException e)
            {
                logger.error("InterruptedException while trying to start preview!", e);
                messagesCourier.showErrorMessage(messagesProvider.internalException());
            }
            return false;
        }
    }

    private class PreviewingDelegate extends Delegate
    {
        @Override
        boolean setEnabled (boolean newEnabled)
        {
            if (newEnabled)
            {
                // ignores, since it is already previewing
                return true;
            }
            else
            {
                setMediaEnabledAndMerge(false);
                getMediaFacade().stopPreviewing();
                changeState(MediaControllerState.IDLE);
                return true;
            }
        }

        @Override
        void startPreview ()
        {
            logUnexpectedMethodCall("startPreview");
        }

        @Override
        void showSettings ()
        {
            getMediaFacade().showSettingsDialog();
        }

        @Override
        void dispose ()
        {
            getMediaFacade().stopPreviewing();
        }

        /**
         * Checks if mediaManager is managing. If it is, requests the mediaFacade to start recording. Logs and
         * notifies the user in case of any problems. Changes the state if succeeded.
         */
        @Override
        void startStopRecording ()
        {
            if (!mediaManager.hasSelection())
            {
                messagesCourier.showInfoMessage(messagesProvider.attemptToRecordWithoutSettingProcedure());
                return;
            }
            try
            {
                Path mediaFile = suggestNewMediaFile();
                Files.createDirectories(mediaFile.getParent());
                getMediaFacade().startRecording(mediaFile);
                notifyMediaAdded(mediaFile);
                changeState(MediaControllerState.RECORDING);
            }
            catch (IOException | SecurityException e)
            {
                logger.error("IOException when started recording!", e);
                messagesCourier.showErrorMessage(messagesProvider.internalException());
            }
        }

        @Override
        void pauseResumeRecording ()
        {
            logUnexpectedMethodCall("pauseResumeRecording");
        }

        @Override
        void pausePreview ()
        {
            getMediaFacade().pausePreview();
            changeState(MediaControllerState.PAUSED_PREVIEWING);
        }

        @Override
        void resumePreview ()
        {
            logUnexpectedMethodCall("resumePreview");
        }

        @Override
        public void deviceConnectionLost ()
        {
            handleMediaException(messagesProvider.deviceConnectionLost());
        }

        @Override
        public void previewingException ()
        {
            handleMediaException(messagesProvider.internalException());
        }

        @Override
        public void deviceNotFound (String device)
        {
            handleMediaException(messagesProvider.mediaDeviceNotFoundException(device));
        }

        void handleMediaException (String message)
        {
            setMediaEnabledAndMerge(false);
            messagesCourier.showErrorMessage(message);
            getMediaHasBeenDisabledListener().run();
            changeState(MediaControllerState.IDLE);
        }
    }

    private class PausedPreviewingDelegate extends PreviewingDelegate
    {
        @Override
        void startPreview ()
        {
            logUnexpectedMethodCall("startPreview");
            resumePreview();
        }

        @Override
        void showSettings ()
        {
            logUnexpectedMethodCall("showSettings");
            resumePreview();
            MediaController.this.showSettings();
        }

        @Override
        void dispose ()
        {
            getMediaFacade().stopPreviewing();
        }

        @Override
        void pausePreview ()
        {
            logUnexpectedMethodCall("pausePreview");
        }

        @Override
        void resumePreview ()
        {
            getMediaFacade().resumePreview();
            changeState(MediaControllerState.PREVIEWING);
        }
    }

    private class RecordingDelegate extends Delegate
    {
        private MediaControllerState previewState         = MediaControllerState.PREVIEWING;
        private boolean              stopHasBeenRequested = false;

        @Override
        boolean setEnabled (boolean newEnabled)
        {
            if (newEnabled != isMediaEnabled())
            {
                messagesCourier.showInfoMessage(messagesProvider.attemptToChangePreferencesWhileRecording());
                return false;
            }
            return true;
        }

        @Override
        void startPreview ()
        {
            // already previewing!!
            logUnexpectedMethodCall("startPreview");
        }

        @Override
        void showSettings ()
        {
            messagesCourier.showInfoMessage(messagesProvider.attemptToChangePreferencesWhileRecording());
        }

        @Override
        void dispose ()
        {
            logUnexpectedMethodCall("dispose");
            startStopRecording();
            MediaController.this.dispose();
        }

        @Override
        void startStopRecording ()
        {
            if (stopHasBeenRequested)
            {
                messagesCourier.showInfoMessage(messagesProvider.recordIsFinishing());
            }
            else
            {
                getMediaFacade().stopRecording();
                stopHasBeenRequested = true;
            }
        }

        @Override
        void pauseResumeRecording ()
        {
            getMediaFacade().pauseRecording();
            changeState(MediaControllerState.PAUSED_RECORDING);
        }

        @Override
        void pausePreview ()
        {
            getMediaFacade().pausePreview();
            previewState = MediaControllerState.PAUSED_PREVIEWING;
        }

        @Override
        void resumePreview ()
        {
            getMediaFacade().resumePreview();
            previewState = MediaControllerState.PREVIEWING;
        }

        @Override
        public void outOfDiskSpaceWhileRecording ()
        {
            messagesCourier.showErrorMessage(messagesProvider.outOfDiskSpaceWhileRecording());
        }

        @Override
        public void lostFileAccessWhileRecording ()
        {
            messagesCourier.showErrorMessage(messagesProvider.lostFileAccessWhileRecording());
        }

        @Override
        public void recordingException ()
        {
            messagesCourier.showErrorMessage(messagesProvider.internalException());
        }

        @Override
        public void deviceConnectionLost ()
        {
            handlePreviewException(messagesProvider.deviceConnectionLost());
        }

        @Override
        public void previewingException ()
        {
            handlePreviewException(messagesProvider.internalException());
        }

        @Override
        public void deviceNotFound (String device)
        {
            handlePreviewException(messagesProvider.mediaDeviceNotFoundException(device));
        }

        @Override
        public void recordingFinished ()
        {
            changeState(previewState);
        }

        private void handlePreviewException (String message)
        {
            getMediaFacade().stopRecording();
            messagesCourier.showErrorMessage(message);
        }
    }

    private class PausedRecordingDelegate extends RecordingDelegate
    {
        @Override
        void pauseResumeRecording ()
        {
            getMediaFacade().resumeRecording();
            changeState(MediaControllerState.RECORDING);
        }
    }
}
