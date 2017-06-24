package br.com.jmedia.core.model;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class InvalidPixelFormatException extends Exception
{
    private final String pixelFormat;

    public InvalidPixelFormatException (String pixelFormat)
    {
        this.pixelFormat = pixelFormat;
    }

    public InvalidPixelFormatException ()
    {
        this("null");
    }

    public String getPixelFormat ()
    {
        return pixelFormat;
    }
}
