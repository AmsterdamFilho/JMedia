package br.com.jmedia.core.model;

import javafx.scene.image.Image;

import java.nio.file.Path;

/**
 * Object that holds information about a photo. The information is held in either a byte array or a Path. One of them
 * will always be null, and the other will not.
 *
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class Photo
{
    /**
     * The image of the photo. Will be null if the object was initialized with a path, since the photo was
     * already in the file system.
     */
    private Image image;
    /**
     * The path to the photo. Will be null if the photo image were captured but the image was not saved to the
     * filesystem, either because of an error or because the saving process is not finished yet. When the image is
     * finally saved, the byte array will be null and the path will be set.
     */
    private Path  path;

    public Photo (Image image)
    {
        this.image = image;
    }

    public Photo (Path path)
    {
        this.path = path;
    }

    public Image getImage ()
    {
        return image;
    }

    public Path getPath ()
    {
        return path;
    }

    /**
     * Sets the path while setting the image to null.
     *
     * @param path the path to the photo file
     */
    public void configure (Path path)
    {
        if (path == null)
        {
            throw new NullPointerException();
        }
        this.path = path;
        image = null;
    }
}
