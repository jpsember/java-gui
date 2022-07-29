package js.gui.example;

import static js.base.Tools.*;

import javax.swing.JFrame;

import js.guiapp.GUIApp;
import js.guiapp.KeyboardShortcutManager;
import js.guiapp.OurAppFrame;
import js.guiapp.UserEventManager;

public class SampleApp extends GUIApp {

  public static void main(String[] args) {
    new SampleApp().startApplication(args);
  }

  private SampleApp() {
    loadTools();
    // To disable development mode, enable this:
    // setDevMode(false);
  }

  @Override
  public String getVersion() {
    return "9.9";
  }

  @Override
  protected String getProcessExpression() {
    return "js.gui.example";
  }


  public void createAndShowGUI() {
    //mUserEventManager.setListener(this::processUserEvent);
    mKeyboardShortcutManager = new KeyboardShortcutManager(this.getClass());
    createFrame();
  }

  // TODO: refactor to make this private
  public UserEventManager mUserEventManager;
  // TODO: refactor to make this private
  public KeyboardShortcutManager mKeyboardShortcutManager;

  private void createFrame() {
    mFrame = new OurAppFrame();
    JFrame jFrame = mFrame.frame();
    jFrame.setVisible(true);
  }

  private OurAppFrame mFrame;

}
