package br.com.jmedia.ffmpeg.service;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
interface ProcessClient
{
    /**
     * Invoked then the process waitFor method throws an Exception
     *
     * @param e the exception
     */
    void handleWaitForException (Exception e);

    /**
     * Returns the name of the process of the client
     *
     * @return the process name
     */
    String processName ();

    /**
     * Invoked when the method waitFor of the process returns
     *
     * @param exitCode the waitFor result
     */
    void processExited (int exitCode);
}
