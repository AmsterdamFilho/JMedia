package br.com.jmedia.core.model;

/**
 * Abstraction of the current state of the media controller.
 *
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public enum MediaControllerState
{
    /**
     * The controller is idle. That means there is no media being previewed, recorded or played. That
     * might be because the user does not want to use media or because there is no media available.
     */
    IDLE,
    /**
     * The MediaController has a device configured for it and is currently only previewing it.
     */
    PREVIEWING,
    /**
     * The MediaController has a device configured for it, but the previewing is currently pause.
     */
    PAUSED_PREVIEWING,
    /**
     * The MediaController is recording media to a file. It will also be previewing audio, video or both at this stage.
     */
    RECORDING,
    /**
     * The MediaController is recording media to a file, but the process is paused for now.
     */
    PAUSED_RECORDING,
}
