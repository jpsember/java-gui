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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import js.app.App;
import js.app.AppOper;
import js.data.AbstractData;
import js.graphics.Paint;
import js.gui.gen.GuiAppConfig;
import js.system.SystemUtil;

public abstract class GUIApp extends App {

  // ------------------------------------------------------------------
  // App configuration
  // ------------------------------------------------------------------

  public static GuiAppConfig.Builder guiAppConfig() {
    return sConfig;
  }

  @Override
  public final String getVersion() {
    return guiAppConfig().version();
  }

  private static GuiAppConfig.Builder sConfig = GuiAppConfig.DEFAULT_INSTANCE.toBuilder();

  /**
   * Register the single operation for this application. GUI apps don't support
   * multiple operations; this singleton operation will forward calls to app
   * methods for simplicity
   */
  @Override
  protected final void registerOperations() {
    registerOper(new AppOper() {
      @Override
      public String userCommand() {
        return null;
      }

      @Override
      public void perform() {
        startApplication2();
      }

      @Override
      protected List<Object> getAdditionalArgs() {
        return getOptionalArgDescriptions();
      }
    });
  }

  /**
   * Perform this app's singleton operation; a continuation of the 'start
   * application' process
   */
  private void startApplication2() {
    SystemUtil.setConsoleAppFlag(false);
    processOptionalArgs();
    if (cmdLineArgs().hasNextArg())
      throw badArg("Unexpected argument(s):", cmdLineArgs().peekNextArg());

    // Continue starting app within the Swing thread
    //
    SwingUtilities.invokeLater(() -> {
      SwingUtils.setEventDispatchThread();
      startApplication3();
    });
  }

  private void startApplication3() {
    if (guiAppConfig().devMode() && guiAppConfig().singleInstanceMode()) {
      String processExpr = getClass().getName();
      SystemUtil.killProcesses(processExpr);
      SystemUtil.killAfterDelay(processExpr);
    }

    UserEventManager.construct(getDefaultUserOperation());
    UserEventManager.sharedInstance().setListener((x) -> userEventManagerListener(x));
    KeyboardShortcutManager.construct(guiAppConfig().keyboardShortcutRegistry());

    createFrame();
    startedGUI();
    // TODO: when switching projects, the frame does a quick 'bounce'; it would be better to hide the frame when a project
    // is closing, and only make it visible again once a new one is loaded (or if no new project is replacing it)
    mFrame.frame().setVisible(true);
  }
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

  private void createFrame() {
    mFrame = new OurAppFrame();
    mFrame.frame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    rebuildFrameContent();
    // We need to make this call to ensure a menu bar exists, and to call revalidate() 
    performRepaint(~0);
    startPeriodicBackgroundTask();
  }

  public final void rebuildFrameContent() {
    checkState(mFrame != null, "frame doesn't exist yet");

    // Remove any placeholder message (in case no project was open)
    contentPane().removeAll();

    // We embed a JPanel that serves as a container for other components, 
    // the main one being the editor window, but others that may include
    // control panels or informational windows

    JPanel parentPanel = new JPanel(new BorderLayout());
    populateFrame(parentPanel);
    contentPane().add(parentPanel);
    // WTF, apparently this is necessary to get repainting to occur; see
    // https://groups.google.com/g/comp.lang.java.gui/c/vCbwLOX9Vow?pli=1
    contentPane().revalidate();
  }

  /**
   * Add appropriate components to the app frame's parent panel. Default does
   * nothing
   */
  public void populateFrame(JPanel parentPanel) {
  }

  private OurAppFrame mFrame;

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

  public final void updateTitle() {
    String title = name() + " v" + getVersion();
    if (guiAppConfig().devMode())
      title = title + " !!! DEV MODE !!!";
    String auxTitle = getTitleText();
    if (!nullOrEmpty(auxTitle))
      title = "(" + title + ") " + auxTitle;
    appFrame().frame().setTitle(title);
  }

  public String getTitleText() {
    return null;
  }

  /**
   * Called after GUI has been constructed; default does nothing
   */
  public void startedGUI() {
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

  // ------------------------------------------------------------------
  // Performing periodic tasks on the Swing event thread
  // ------------------------------------------------------------------

  private void startPeriodicBackgroundTask() {
    mSwingTasks = new SwingTaskManager();
    mSwingTasks.addTask(() -> swingBackgroundTask()).start();
  }

  /**
   * Called every ~3 seconds on the Swing event thread. Default does nothing
   */
  public void swingBackgroundTask() {
  }

  private SwingTaskManager mSwingTasks = new SwingTaskManager();

}
