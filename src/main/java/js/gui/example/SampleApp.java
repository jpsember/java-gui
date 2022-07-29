package js.gui.example;

import static js.base.Tools.*;

import javax.swing.JFrame;

import js.app.AppOper;
import js.guiapp.GUIApp;
import js.guiapp.KeyboardShortcutManager;
import js.guiapp.OurAppFrame;
import js.guiapp.UserEventManager;
import js.guiapp.UserOperation;

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
  protected AppOper constructAppOper() {
    return new OurOper();
  }

  @Override
  public String getVersion() {
    return "9.9";
  }

  @Override
  protected String getProcessExpression() {
    return "js.gui.example";
  }

  // ------------------------------------------------------------------
  // AppOper implementation
  // ------------------------------------------------------------------
  private class OurOper extends AppOper {

    @Override
    public String userCommand() {
      return null;
    }

    @Override
    public void perform() {
      startGUI(() -> createAndShowGUI());
    }

    @Override
    protected String getHelpDescription() {
      return "Sample GUIApp implementation";
    }
  }

  protected void createAndShowGUI() {
    mUserEventManager = new UserEventManager(new UserOperation() {
    });
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
