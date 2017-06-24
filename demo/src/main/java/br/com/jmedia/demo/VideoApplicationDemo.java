package br.com.jmedia.demo;

import br.com.jmedia.demo.view.VideoDemo;
import br.com.jwheel.javafx.JavaFxApplication;
import br.com.jwheel.xml.model.FromXmlPreferences;
import br.com.jmedia.demo.model.DemoPreferences;
import br.com.jmedia.demo.services.WindowSizeChangeService;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.inject.Inject;

/**
 * @author Lima Filho, A. L. - amsterdam@luvva.com.br
 */
public class VideoApplicationDemo implements JavaFxApplication
{
    private @Inject WindowSizeChangeService changeService;
    private @Inject VideoDemo               videoDemo;

    private @Inject @FromXmlPreferences DemoPreferences demoPreferences;

    @Override
    public void init (Stage primaryStage)
    {
        Stage newPrimaryStage = new Stage();
        Scene scene = new Scene(videoDemo, demoPreferences.getWidth(), demoPreferences.getHeight());
        newPrimaryStage.setScene(scene);

        scene.widthProperty().addListener(
                (observable, oldValue, newValue) -> changeService.widthChanged(newValue.intValue()));
        scene.heightProperty().addListener(
                (observable, oldValue, newValue) -> changeService.heightChanged(newValue.intValue()));

        newPrimaryStage.setTitle("Media Capture for JavaFX");
        primaryStage.close();
        newPrimaryStage.show();
    }

    @Override
    public void stop ()
    {
        videoDemo.exitEvent();
    }
}
