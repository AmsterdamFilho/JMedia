package br.com.jmedia.demo.view;

import br.com.jmedia.core.service.MediaMessagesCourier;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.inject.Singleton;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
@Singleton
public class FxMediaMessagesCourier implements MediaMessagesCourier
{
    @Override
    public void showErrorMessage (String message)
    {
        Platform.runLater(() ->
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void showWarningMessage (String message)
    {
        Platform.runLater(() ->
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void showInfoMessage (String message)
    {
        Platform.runLater(() ->
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
