package br.com.jmedia.core.service;

/**
 * A view class capable of showing dialog messages to the user.
 *
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public interface MediaMessagesCourier
{
    void showErrorMessage (String message);

    void showWarningMessage (String message);

    void showInfoMessage (String message);
}
