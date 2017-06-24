package br.com.jmedia.core.control;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public interface VideoFacade extends MediaFacade<ImageView>
{
    /**
     * Takes a photo from the video being previewed. The image format should be PNG 24.
     *
     * @return the photo as a JavaFX Image
     */
    Image takePhotoFromPreview ();
}
