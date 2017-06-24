package br.com.jmedia.ffmpeg.service;

import br.com.jmedia.ffmpeg.model.MediaFrameworkSettings;
import br.com.jmedia.core.control.MediaFacadeClient;
import br.com.jmedia.ffmpeg.model.FrameListener;
import br.com.jmedia.ffmpeg.model.NullFrameListener;
import br.com.jmedia.core.model.InvalidPixelFormatException;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class CaptureService implements ProcessClient
{
    private @Inject Logger         logger;
    private @Inject ProcessUtils   processUtils;
    private @Inject MediaFramework mediaFramework;

    private final List<CaptureServiceClient> clients       = new ArrayList<>();
    private       FrameListener              frameListener = new NullFrameListener();

    private ProcessUtils.ProcessManager processManager;
    private MediaFrameworkSettings      settings;
    private MediaFacadeClient           mediaFacadeClient;

    public void start (MediaFrameworkSettings settings, MediaFacadeClient facadeClient) throws IOException
    {
        this.mediaFacadeClient = facadeClient;
        this.settings = settings;
        processManager = processUtils.createManager(settings.getCaptureCommand(), this);
        new CaptureServiceThread().start();
    }

    public void stop ()
    {
        frameListener = new StopProcessFrameListener();
    }

    public void addClient (CaptureServiceClient client)
    {
        frameListener = new AddClientFrameListener(client);
    }

    public void removeClient (CaptureServiceClient client)
    {
        frameListener = new RemoveClientFrameListener(client);
    }

    @Override
    public String processName ()
    {
        return "Video capture process";
    }

    @Override
    public void processExited (int exitCode)
    {
        // 141 means the process used input or output stream, but it was closed. It is a normal way to stop a process.
        if (exitCode == 0 || exitCode == 141)
        {
            logger.info(processName() + " exited successfully. Exit code = " + exitCode);
            String stdErrOutput = processManager.getStdErrOutput();
            if (stdErrOutput != null)
            {
                logger.debug("Process stdErr output: " + stdErrOutput);
            }
        }
        else
        {
            logger.error(processName() + " did not exit normally! Exit code = " + exitCode);
            String stdErrOutput = processManager.getStdErrOutput();
            if (stdErrOutput != null)
            {
                logger.error("Process stdErr output: " + stdErrOutput);
                mediaFramework.parsePreviewError(stdErrOutput, mediaFacadeClient);
            }
        }
    }

    @Override
    public void handleWaitForException (Exception e)
    {
        logger.error(processName() + " waitFor Exception!", e);
    }

    private class CaptureServiceThread extends Thread
    {
        private CaptureServiceThread ()
        {
            super(processName() + " thread");
        }

        @Override
        public void run ()
        {
            try (InputStream is = processManager.getProcessOutput())
            {
                int bytesPerFrame = settings.bytesPerFrame();
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
                        for (CaptureServiceClient client : clients)
                        {
                            client.receive(byteReceived, index);
                        }
                    }
                    clients.forEach(CaptureServiceClient::frameReceived);
                    if (!frameListener.frameReceived())
                    {
                        break;
                    }
                }
            }
            catch (InvalidPixelFormatException e)
            {
                logger.error("An invalid pixel format has been set!" + e.getPixelFormat(), e);
                mediaFacadeClient.previewingException();
            }
            catch (Exception e)
            {
                logger.error("Error in " + processName(), e);
                mediaFacadeClient.previewingException();
            }
            clients.forEach(CaptureServiceClient::captureStopped);
        }
    }

    private class AddClientFrameListener implements FrameListener
    {
        private final CaptureServiceClient client;

        private AddClientFrameListener (CaptureServiceClient client)
        {
            this.client = client;
        }

        @Override
        public boolean frameReceived ()
        {
            clients.add(client);
            frameListener = new NullFrameListener();
            return true;
        }
    }

    private class RemoveClientFrameListener implements FrameListener
    {
        private final CaptureServiceClient client;

        private RemoveClientFrameListener (CaptureServiceClient client)
        {
            this.client = client;
        }

        @Override
        public boolean frameReceived ()
        {
            clients.remove(client);
            frameListener = new NullFrameListener();
            return true;
        }
    }

    private class StopProcessFrameListener implements FrameListener
    {
        @Override
        public boolean frameReceived ()
        {
            return false;
        }
    }
}
