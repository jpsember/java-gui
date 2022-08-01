/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.guiapp;

import static js.base.Tools.*;

import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import js.app.App;
import js.app.AppOper;
import js.data.AbstractData;
import js.graphics.Paint;
import js.json.JSMap;
import js.system.SystemUtil;

public abstract class GUIApp extends App {

  // ------------------------------------------------------------------
  // Development mode
  // ------------------------------------------------------------------

  /**
   * Determine if we're in development mode
   */
  public static boolean devMode() {
    if (sDevModeFlag == null)
      setDevMode(true);
    return sDevModeFlag;
  }

  /**
   * Set development mode. This can only be set once. If it hasn't been
   * explicitly set, it will be set true when devMode() is first called
   */
  public static void setDevMode(boolean flag) {
    checkState(sDevModeFlag == null || sDevModeFlag == flag, "dev mode flag already set");
    sDevModeFlag = flag;
  }

  /**
   * Get a string that can be used to uniquely identify processes representing
   * this app, so that duplicate processes can be killed (when in development
   * mode)
   * 
   * Default implementation returns null, which will generate a warning
   */
  protected String getProcessExpression() {
    return null;
  }

  private static Boolean sDevModeFlag;

  // ------------------------------------------------------------------

  /**
   * Construct the default operation for the event manager. Default returns a
   * 'do nothing' operation
   */
  public UserOperation getDefaultUserOperation() {
    alert("No getDefaultAppOper implemented");
    return new UserOperation() {
    };
  }

  /**
   * A listener for user events; default does nothing
   */
  public void userEventManagerListener(UserEvent event) {
  }

  //------------------------------------------------------------------
  // Frame
  // ------------------------------------------------------------------

  public final OurAppFrame appFrame() {
    return mFrame;
  }

  /**
   * Determine if program should quit after user has attempted to close the
   * window. Default returns true
   */
  public boolean requestWindowClose() {
    return true;
  }

  /**
   * Perform any cleaning up prior to program about to quit
   */
  public void prepareForProgramQuit() {
  }

  private void createFrame() {
    mFrame = new OurAppFrame();

    JFrame jFrame = mFrame.frame();

    // Handle close window requests ourselves
    //
    jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    jFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        if (requestWindowClose()) {
          prepareForProgramQuit();
          //closeProject();
          jFrame.setVisible(false);
          jFrame.dispose();
          mFrame = null;
        }
      }
    });
    jFrame.setVisible(true);
  }

  private OurAppFrame mFrame;

  /**
   * Get the JSMap defining the default keyboard shortcuts. Default
   * implementation returns an empty map
   */
  public JSMap getKeyboardShortcutRegistry() {
    return map();
    //    throw notSupported("No implementation of keyboard shortcut registry");
  }

  // ------------------------------------------------------------------
  // Command line arguments
  // ------------------------------------------------------------------

  /**
   * Get default arguments, if any, for this app; default returns null
   */
  public AbstractData defaultArgs() {
    return null;
  }

  /**
   * Get a description of any optional arguments; default returns null
   */
  public List<Object> getOptionalArgDescriptions() {
    return null;
  }

  /**
   * Process any optional command line arguments
   */
  public void processOptionalArgs() {
  }

  // ------------------------------------------------------------------

  private void auxPerform() {
    processOptionalArgs();
    if (cmdLineArgs().hasNextArg())
      throw badArg("Unexpected argument(s):", cmdLineArgs().peekNextArg());

    SystemUtil.setConsoleAppFlag(false);

    // Start app within Swing thread
    //
    SwingUtilities.invokeLater(() -> {
      SwingUtils.setEventDispatchThread();
      prepareGUI();
    });
  }

  private void prepareGUI() {
    if (devMode()) {
      String processExpr = getProcessExpression();
      if (nonEmpty(processExpr)) {
        SystemUtil.killProcesses(processExpr);
        SystemUtil.killAfterDelay(processExpr);
      } else
        alert("getProcessExpression returned empty string; not killing any existing instances");
    }

    UserEventManager.construct(getDefaultUserOperation());
    UserEventManager.sharedInstance().setListener((x) -> userEventManagerListener(x));
    KeyboardShortcutManager.construct(getKeyboardShortcutRegistry());

    createFrame();
    startGUI();
  }

  public final void updateTitle() {
    String title = name() + " v" + getVersion();
    if (devMode())
      title = title + " !!! DEV MODE !!!";
    String auxTitle = getTitleText();
    if (!nullOrEmpty(auxTitle))
      title = "(" + title + ") " + auxTitle;
    appFrame().frame().setTitle(title);
  }

  public String getTitleText() {
    return null;
  }

  public abstract void startGUI();

  @Override
  protected final void registerOperations() {
    registerOper(new AppOper() {
      @Override
      public String userCommand() {
        return null;
      }

      @Override
      public void perform() {
        auxPerform();
      }

      @Override
      protected List<Object> getAdditionalArgs() {
        return getOptionalArgDescriptions();
      }
    });
  }

  // ------------------------------------------------------------------
  // Menu bar
  // ------------------------------------------------------------------

  public void discardMenuBar() {
    mMenuBar = null;
  }

  private void createMenuBarIfNec() {
    if (mMenuBar != null)
      return;
    KeyboardShortcutManager.sharedInstance().clearAssignedOperationList();
    OurMenuBar m = new OurMenuBar();
    mMenuBar = m;
    populateMenuBar(m);
    mFrame.frame().setJMenuBar(m.jmenuBar());
  }

  public void populateMenuBar(OurMenuBar m) {
  }

  /**
   * Add an item to the current menu
   */
  public JMenuItem addItem(String hotKeyId, String displayedName, UserOperation operation) {
    return mMenuBar.addItem(hotKeyId, displayedName, operation);
  }

  /* private */
  public OurMenuBar mMenuBar;

  public final JComponent contentPane() {
    return (JComponent) mFrame.frame().getContentPane();
  }

  public final void setMouseCursor(int type) {
    if (mFrame != null)
      mFrame.frame().setCursor(Cursor.getPredefinedCursor(type));
  }

  public final void performRepaint(int repaintFlags) {
    // If there is no menu bar, create one
    createMenuBarIfNec();

    String alertText = getAlertText();

    if (alertText != null) {
      // Add placeholder text (it may get immediately replaced if we are in a close+open project cycle)
      JLabel message = new JLabel(alertText, SwingConstants.CENTER);
      message.setFont(Paint.BIG_FONT);
      contentPane().removeAll();
      contentPane().add(message);
      contentPane().revalidate();
    } else {
      repaintPanels(repaintFlags);
    }
  }

  public abstract void repaintPanels(int repaintFlags);

  public String getAlertText() {
    return null;
  }

}
