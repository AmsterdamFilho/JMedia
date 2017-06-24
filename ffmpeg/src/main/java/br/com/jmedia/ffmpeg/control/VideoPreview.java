package br.com.jmedia.ffmpeg.control;

import br.com.jwheel.javafx.utils.ImageUtils;
import br.com.jmedia.ffmpeg.service.PreviewService;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.nio.ByteBuffer;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class VideoPreview implements PreviewService
{
    private final PixelWriter   pixelWriter;
    private final int           width;
    private final int           height;
    private final byte[]        byteBuffer;
    private final WritableImage image;

    private final PixelFormat<ByteBuffer> pixelFormat   = PixelFormat.getByteBgraInstance();
    private final int                     bytesPerPixel = 4;

    public VideoPreview (ImageView view, int width, int height)
    {
        this.width = width;
        this.height = height;
        image = new WritableImage(width, height);
        Platform.runLater(() -> view.setImage(image));
        this.pixelWriter = image.getPixelWriter();
        byteBuffer = new byte[getBufferLength()];
    }

    public int getBufferLength ()
    {
        return width * height * bytesPerPixel;
    }

    @Override
    public WritableImage snapshot ()
    {
        return ImageUtils.clone(image);
    }

    @Override
    public void receive (int bytes, int currentIndex)
    {
        byteBuffer[currentIndex] = (byte) bytes;
    }

    @Override
    public void frameReceived ()
    {
        pixelWriter.setPixels(0, 0, width, height, pixelFormat, byteBuffer, 0, width * bytesPerPixel);
    }

    @Override
    public void captureStopped ()
    {
    }
}
