package br.com.jmedia.core.service;

import br.com.jwheel.javafx.utils.ImageUtils;
import br.com.jmedia.core.model.MediaListener;
import br.com.jmedia.core.model.MediaLocation;
import br.com.jmedia.core.model.Photo;
import javafx.scene.image.Image;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Manages the media files of a medical procedure. Anyone interested in what happens to them should register himself as
 * a listener. The manager has utility methods regarding the directories he manages.
 * <p>
 * The MediaManager is responsible for saving the photos to the file system. If it was not possible to save the photo on
 * the default location, it will try to save in an alternative on. When the same media folder is loaded again, or the
 * current folder is deselected, or upon an external request to do so, the MediaManager will try to move the photos to
 * the default location.
 *
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
@Singleton
public class MediaManager
{
    private @Inject MediaLocation mediaLocation;
    private @Inject Logger        logger;

    private final MediaPathProvider audioMediaPathProvider = new AudioPathProvider();
    private final MediaPathProvider videoMediaPathProvider = new VideoPathProvider();
    private final SimpleDateFormat  simpleDateFormat       = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final String            photoExtension         = "png";
    private final String            audioExtension         = "mp3";
    private final String            videoExtension         = "mp4";

    private String procedureId = null;

    private final List<MediaListener> listeners = new ArrayList<>();

    public void addListener (MediaListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener (MediaListener listener)
    {
        listeners.remove(listener);
    }

    public boolean hasSelection ()
    {
        return procedureId != null;
    }

    public void deselect ()
    {
        setSelected(0);
    }

    public void setSelected (int procedureId)
    {
        // if current procedure changed
        if (!Objects.equals(this.procedureId, String.valueOf(procedureId)))
        {
            if (this.procedureId != null)
            {
                new Thread(() -> recoverImages(this.procedureId), "Recover images thread").start();
            }
            this.procedureId = null;
            listeners.forEach(MediaListener::deselected);
            if (procedureId > 0)
            {
                this.procedureId = String.valueOf(procedureId);
                loadMedia(this.procedureId);
            }
        }
    }

    public Path suggestNewAudioFilePath () throws IOException, SecurityException
    {
        if (hasSelection())
        {
            return audioMediaPathProvider.provide(procedureId);
        }
        return null;
    }

    public Path suggestNewVideoFilePath () throws IOException, SecurityException
    {
        if (hasSelection())
        {
            return videoMediaPathProvider.provide(procedureId);
        }
        return null;
    }

    public void photoCaptured (Image image)
    {
        Photo photo = new Photo(image);
        listeners.forEach(listener -> listener.photoCaptured(photo));
        new Thread(new PhotoFileSaver(photo, procedureId), "Photo file saver thread").start();
    }

    public void audioAdded (Path path)
    {
        listeners.forEach(listener -> listener.audioAdded(path));
    }

    public void videoAdded (Path path)
    {
        listeners.forEach(listener -> listener.videoAdded(path));
    }

    /**
     * Recover the images that were previously saved in an alternative directory and update the photo objects.
     */
    public void recoverImages ()
    {
        // TODO: 15/01/17 implement this method (this is recover on demand, not automatically)
    }

    private void loadMedia (String procedureId)
    {
        new PhotoLoader(procedureId).start();
        new AudioLoader(procedureId).start();
        new VideoLoader(procedureId).start();
    }

    private void recoverImages (String procedureId)
    {
        Path alternativePhotoDirectory = alternativePhotoDirectoryPath(procedureId);
        if (alternativePhotoDirectory == null || Files.notExists(alternativePhotoDirectory))
        {
            return;
        }
        Path defaultPhotoDirectory = defaultPhotoDirectoryPath(procedureId);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(alternativePhotoDirectory))
        {
            for (Path backupPhoto : stream)
            {
                Path newPhotoPath = defaultPhotoDirectory.resolve(backupPhoto.getFileName());
                if (Files.notExists(newPhotoPath))
                {
                    Files.createDirectories(newPhotoPath.getParent());
                    Files.move(backupPhoto, newPhotoPath);
                }
                else
                {
                    logger.error("Could not move the photo to default location, for it already existed or its " +
                            "existence could not be determined!", new IllegalArgumentException());
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error on recover images routine!", e);
        }
    }

    private Path defaultPhotoDirectoryPath (String procedureId)
    {
        return buildMediaFolder(mediaLocation.defaultPhotoRoot(), mediaLocation.photoFolderName(), procedureId);
    }

    private Path alternativePhotoDirectoryPath (String procedureId)
    {
        Path root = mediaLocation.alternativePhotoRoot();
        if (root == null)
        {
            return null;
        }
        return buildMediaFolder(root, mediaLocation.photoFolderName(), procedureId);
    }

    private Path audioDirectoryPath (String procedureId)
    {
        return buildMediaFolder(mediaLocation.audioRoot(), mediaLocation.audioFolderName(), procedureId);
    }

    private Path videoDirectoryPath (String procedureId)
    {
        return buildMediaFolder(mediaLocation.videoRoot(), mediaLocation.videoFolderName(), procedureId);
    }

    private Path buildMediaFolder (Path root, String mediaFolderName, String procedureId)
    {
        return Paths.get(root.toString(), mediaLocation.proceduresFolderName(), procedureId, mediaFolderName);
    }

    private abstract class MediaLoader<T> extends Thread
    {
        private final String procedureId;

        private MediaLoader (String procedureId, String threadName)
        {
            super(threadName);
            this.procedureId = procedureId;
        }

        @Override
        public void run ()
        {
            beforeStart(procedureId);
            Path mediaFolder = getMediaFolder(procedureId);
            try
            {
                if (Files.exists(mediaFolder))
                {
                    List<T> mediaFiles = new ArrayList<>();
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(mediaFolder, getFilter()))
                    {
                        for (Path mediaFile : stream)
                        {
                            mediaFiles.add(getFromPath(mediaFile));
                        }
                        if (Objects.equals(procedureId, MediaManager.this.procedureId))
                        {
                            listeners.forEach(getConsumer(mediaFiles));
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Could not create a directory stream for the mediaFolder!", e);
                    }
                }
                else //noinspection StatementWithEmptyBody
                    if (Files.notExists(mediaFolder))
                    {
                        // there is no media, no problem with that
                    }
                    else
                    {
                        logger.error("Could not access mediaFolder!", new IllegalArgumentException());
                    }
            }
            catch (SecurityException e)
            {
                logger.error("Error checking media folder existence!", new IllegalArgumentException());
            }
        }

        void beforeStart (String procedureId)
        {
            // does nothing by default.
        }

        abstract Path getMediaFolder (String procedureId);

        abstract String getFilter ();

        abstract T getFromPath (Path path);

        abstract Consumer<MediaListener> getConsumer (List<T> mediaFiles);
    }

    private class PhotoLoader extends MediaLoader<Photo>
    {
        private PhotoLoader (String procedureId)
        {
            super(procedureId, "Photo loader thread");
        }

        @Override
        void beforeStart (String procedureId)
        {
            recoverImages(procedureId);
        }

        @Override
        Path getMediaFolder (String procedureId)
        {
            return defaultPhotoDirectoryPath(procedureId);
        }

        @Override
        String getFilter ()
        {
            return "*." + photoExtension;
        }

        @Override
        Photo getFromPath (Path path)
        {
            return new Photo(path);
        }

        @Override
        Consumer<MediaListener> getConsumer (List<Photo> mediaFiles)
        {
            return mediaListener -> mediaListener.photoLoaded(mediaFiles);
        }
    }

    private class AudioLoader extends MediaLoader<Path>
    {
        private AudioLoader (String procedureId)
        {
            super(procedureId, "Audio loader thread");
        }

        @Override
        Path getMediaFolder (String procedureId)
        {
            return audioDirectoryPath(procedureId);
        }

        @Override
        String getFilter ()
        {
            return "*." + audioExtension;
        }

        @Override
        Path getFromPath (Path path)
        {
            return path;
        }

        @Override
        Consumer<MediaListener> getConsumer (List<Path> mediaFiles)
        {
            return mediaListener -> mediaListener.audioLoaded(mediaFiles);
        }
    }

    private class VideoLoader extends MediaLoader<Path>
    {
        private VideoLoader (String procedureId)
        {
            super(procedureId, "Video loader thread");
        }

        @Override
        Path getMediaFolder (String procedureId)
        {
            return videoDirectoryPath(procedureId);
        }

        @Override
        Path getFromPath (Path path)
        {
            return path;
        }

        @Override
        String getFilter ()
        {
            return "*." + videoExtension;
        }

        @Override
        Consumer<MediaListener> getConsumer (List<Path> mediaFiles)
        {
            return mediaListener -> mediaListener.videoLoaded(mediaFiles);
        }
    }

    private class PhotoFileSaver implements Runnable
    {
        private final Photo  photo;
        private final String procedureId;

        private PhotoFileSaver (Photo photo, String procedureId)
        {
            this.photo = photo;
            this.procedureId = procedureId;
        }

        @Override
        public void run ()
        {
            try
            {
                Path defaultPhotoPath = new DefaultPhotoPathProvider().provide(procedureId);
                Files.createDirectories(defaultPhotoPath.getParent());
                ImageUtils.saveToFile(defaultPhotoPath, photo.getImage());
                photo.configure(defaultPhotoPath);
                notifyListeners(listener -> listener.photoSaved(photo));
            }
            catch (IOException e)
            {
                logger.error("Error saving photo to default directory!", e);
                if (alternativePhotoDirectoryPath(MediaManager.this.procedureId) == null)
                {
                    notifyListeners(listener -> listener.photoCouldNotBeSaved(photo));
                }
                else
                {
                    try
                    {
                        Path alternativePhotoPath = new AlternativePhotoPathProvider().provide(procedureId);
                        Files.createDirectories(alternativePhotoPath.getParent());
                        ImageUtils.saveToFile(alternativePhotoPath, photo.getImage());
                    }
                    catch (IOException e1)
                    {
                        logger.error("Error saving photo to alternative directory!", e1);
                        notifyListeners(listener -> listener.photoCouldNotBeSaved(photo));
                    }
                }
            }
        }

        private void notifyListeners (Consumer<MediaListener> consumer)
        {
            if (Objects.equals(MediaManager.this.procedureId, procedureId))
            {
                listeners.forEach(consumer);
            }
        }
    }

    private abstract class MediaPathProvider
    {
        Path provide (String procedureId) throws IOException, SecurityException
        {
            if (procedureId == null)
            {
                return null;
            }
            Path mediaFolder = getMediaFolder(procedureId);
            Files.createDirectories(mediaFolder.getParent());
            String dateTime = simpleDateFormat.format(new Date());
            int counter = 1;
            Path response = Paths.get(mediaFolder.toString(), dateTime + "." + getExtension());
            // the condition Files.notExists(response) must return true so the program can suggest a name.
            // If Files.exists were used, returning false wouldn't mean that the file does not exist
            while (!Files.notExists(response))
            {
                response = Paths.get(mediaFolder.toString(), dateTime + "-" + counter + "." + getExtension());
                counter++;
                if (counter > 7)
                {
                    throw new IOException("Could not provide a name for the media!");
                }
            }
            return response;
        }

        abstract String getExtension ();

        abstract Path getMediaFolder (String procedureId);
    }

    private abstract class PhotoPathProvider extends MediaPathProvider
    {
        @Override
        String getExtension ()
        {
            return photoExtension;
        }
    }

    private class DefaultPhotoPathProvider extends PhotoPathProvider
    {
        @Override
        Path getMediaFolder (String procedureId)
        {
            return defaultPhotoDirectoryPath(procedureId);
        }
    }

    private class AlternativePhotoPathProvider extends PhotoPathProvider
    {
        @Override
        Path getMediaFolder (String procedureId)
        {
            return alternativePhotoDirectoryPath(procedureId);
        }
    }

    private class AudioPathProvider extends MediaPathProvider
    {
        @Override
        String getExtension ()
        {
            return audioExtension;
        }

        @Override
        Path getMediaFolder (String procedureId)
        {
            return audioDirectoryPath(procedureId);
        }
    }

    private class VideoPathProvider extends MediaPathProvider
    {
        @Override
        String getExtension ()
        {
            return videoExtension;
        }

        @Override
        Path getMediaFolder (String procedureId)
        {
            return videoDirectoryPath(procedureId);
        }
    }
}
