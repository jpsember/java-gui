package js.gui.example;

import static js.base.Tools.*;

import js.guiapp.GUIApp;

public class SampleApp extends GUIApp {

  public static void main(String[] args) {
    new SampleApp().startApplication(args);
  }

  private SampleApp() {
    loadTools();
    guiAppConfig() //
    .appName("example") //
    .processExpression("js.gui.example");
  }

 
  public void startGUI() {
    todo("add some panels");
  }

  @Override
  public void repaintPanels(int repaintFlags) {
    todo("paint some panels");
  }

}
