package br.com.jmedia.ffmpeg.service;

import br.com.jmedia.ffmpeg.control.VideoPreview;
import br.com.jmedia.ffmpeg.model.MediaFrameworkSettings;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class EncodeService implements ProcessClient, PreviewService
{
    private @Inject Logger         logger;
    private @Inject ProcessUtils   processUtils;
    private @Inject MediaFramework mediaFramework;

    private ProcessUtils.ProcessManager processManager;
    private VideoPreview                videoPreview;
    private OutputStream                outputStream;

    private boolean exited = false;

    public void start (MediaFrameworkSettings settings, VideoPreview videoPreview) throws IOException
    {
        this.videoPreview = videoPreview;
        processManager = processUtils.createManager(settings.getEncodeCommand(), this);
        outputStream = processManager.getProcessInput();
        new OutputToPreviewThread().start();
    }

    @Override
    public WritableImage snapshot ()
    {
        return videoPreview.snapshot();
    }

    @Override
    public void handleWaitForException (Exception e)
    {
        logger.error(processName() + " waitFor exception!", e);
        exited = true;
    }

    @Override
    public String processName ()
    {
        return "Video encode process";
    }

    @Override
    public void processExited (int exitCode)
    {
        exited = true;
        // 141 means the process used input or output stream, but it was closed. It is a normal way to stop a process.
        if (exitCode == 0 || exitCode == 141)
        {
            logger.info(processName() + " exited successfully. Exit code: " + exitCode);
            String stdErrOutput = processManager.getStdErrOutput();
            if (stdErrOutput != null)
            {
                logger.debug("Process stdErr output: " + stdErrOutput);
            }
        }
        else
        {
            logger.error(processName() + " did not exit normally! Exit code: " + exitCode);
            String stdErrOutput = processManager.getStdErrOutput();
            if (stdErrOutput != null)
            {
                logger.error("Process stdErr output: " + stdErrOutput);
            }
        }
    }

    @Override
    public void receive (int bytes, int index) throws IOException
    {
        try
        {
            outputStream.write(bytes);
        }
        catch (IOException e)
        {
            logger.error(processName() + ": Could not write to output stream!", e);
            closeOutputStream();
            throw e;
        }
    }

    @Override
    public void frameReceived ()
    {
    }

    @Override
    public void captureStopped ()
    {
        closeOutputStream();
    }

    private void closeOutputStream ()
    {
        if (!exited)
        {
            try
            {
                outputStream.close();
            }
            catch (IOException e)
            {
                logger.error(processName() + ": Could not close output stream!", e);
            }
        }
    }

    private class OutputToPreviewThread extends Thread
    {
        private OutputToPreviewThread ()
        {
            super(processName() + " output to preview thread");
        }

        @Override
        public void run ()
        {
            try (InputStream is = processManager.getProcessOutput())
            {
                int bytesPerFrame = videoPreview.getBufferLength();
                whileLoop:
                while (true)
                {
                    for (int index = 0; index < bytesPerFrame; index++)
                    {
                        int byteReceived = is.read();
                        if (byteReceived == -1)
                        {
                            break whileLoop;
                        }
                        videoPreview.receive(byteReceived, index);
                    }
                    videoPreview.frameReceived();
                }
            }
            catch (Exception e)
            {
                logger.error("Error in " + processName(), e);
                closeOutputStream();
            }
        }
    }
}
