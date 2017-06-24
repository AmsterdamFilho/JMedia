package br.com.jmedia.ffmpeg.service;

import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class ProcessUtils
{
    private @Inject Logger logger;

    public String getStdOutAndStdErrOutput (List<String> command) throws IOException, InterruptedException
    {
        Process process = startProcess(command, true);
        ProcessOutputReader stdErrAndStdOutReader = startInputReader(process.getInputStream());
        process.waitFor();
        if (stdErrAndStdOutReader.exception == null)
        {
            logger.debug(stdErrAndStdOutReader.result);
            return stdErrAndStdOutReader.result;
        }
        throw stdErrAndStdOutReader.exception;
    }

    public ProcessManager createManager (List<String> command, ProcessClient client) throws IOException
    {
        Process process = startProcess(command, false);
        ProcessOutputReader stdErrReader = startInputReader(process.getErrorStream());
        ProcessManager processManager = new ProcessManager(client, stdErrReader, process);
        processManager.start();
        return processManager;
    }

    private ProcessOutputReader startInputReader (InputStream inputStream)
    {
        ProcessOutputReader reader = new ProcessOutputReader("StdOut reader", inputStream);
        reader.start();
        return reader;
    }

    private Process startProcess (List<String> command, boolean redirectErrorStream) throws IOException
    {
        logger.info("Executing process: " + String.join(" ", command));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (redirectErrorStream)
        {
            processBuilder.redirectErrorStream(true);
        }
        return processBuilder.start();
    }

    private class ProcessOutputReader extends Thread
    {
        private final InputStream inputStream;
        private       String      result;
        private       IOException exception;

        public ProcessOutputReader (String threadName, InputStream inputStream)
        {
            super(threadName);
            this.inputStream = inputStream;
        }

        @Override
        public void run ()
        {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    sb.append(line).append("\n");
                }
                result = sb.toString();
            }
            catch (IOException e)
            {
                exception = e;
            }
        }
    }

    public class ProcessManager extends Thread
    {
        private final ProcessClient       client;
        private final ProcessOutputReader stdErrReader;
        private final Process             process;
        private final Timer               destroyForciblyTimer;

        public ProcessManager (ProcessClient client, ProcessOutputReader stdErrReader, Process process)
        {
            super(client.processName() + " waitFor thread");
            this.client = client;
            this.stdErrReader = stdErrReader;
            this.process = process;
            destroyForciblyTimer = new Timer(client.processName() + " destroy forcibly timer");
        }

        public InputStream getProcessOutput ()
        {
            return process.getInputStream();
        }

        public OutputStream getProcessInput ()
        {
            return process.getOutputStream();
        }

        /**
         * The stdErr output of the process or null if it could not be read
         *
         * @return the stdErr output
         */
        public String getStdErrOutput ()
        {
            if (stdErrReader.exception == null)
            {
                return stdErrReader.result;
            }
            else
            {
                logger.error("Error reading process stdErr output!", stdErrReader.exception);
                return null;
            }
        }

        @Override
        public void run ()
        {
            Thread shutdownHook = new Thread(this::shutdownHookEvent, getName() + " shutdown hook");
            try
            {
                Runtime.getRuntime().addShutdownHook(shutdownHook);
                client.processExited(process.waitFor());
                logger.info(client.processName() + " exited");
                destroyForciblyTimer.cancel();
            }
            catch (Exception e)
            {
                logger.error(client.processName() + " waitFor threw and Exception!", e);
                client.handleWaitForException(e);
            }
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        private void shutdownHookEvent ()
        {
            logger.info("Shutdown hook request to stop process...");
            try
            {
                logger.info("Destroying " + client.processName() + "...");
                process.destroy();
                scheduleDestroyForciblyTask(3000);
            }
            catch (Exception e)
            {
                logger.warn("Destroy timer could not be scheduled!", e);
                scheduleDestroyForciblyTask(0);
            }
        }

        private void scheduleDestroyForciblyTask (int delay)
        {
            destroyForciblyTimer.schedule(new TimerTask()
            {
                @Override
                public void run ()
                {
                    logger.info("Destroying forcibly" + client.processName() + "...");
                    try
                    {
                        process.destroyForcibly();
                    }
                    catch (Exception e)
                    {
                        logger.warn("Could not destroy forcibly " + client.processName() + "!", e);
                    }
                    destroyForciblyTimer.cancel();
                }
            }, delay);
        }
    }
}
