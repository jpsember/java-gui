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
import js.widget.WidgetManager;

/**
 * App subclass for applications requiring a graphical user interface
 */
public abstract class GUIApp extends App {

  // ------------------------------------------------------------------
  // Construction
  // ------------------------------------------------------------------

  public GUIApp() {
    setSingleton();
  }

  // ------------------------------------------------------------------
  // App configuration
  // ------------------------------------------------------------------

  public final GuiAppConfig.Builder guiAppConfig() {
    return mConfig;
  }

  @Override
  public final String getVersion() {
    return guiAppConfig().version();
  }

  private GuiAppConfig.Builder mConfig = GuiAppConfig.DEFAULT_INSTANCE.toBuilder();

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
   * Process any optional command line arguments
   */
  public void processOptionalArgs() {
  }

  // ------------------------------------------------------------------
  // App startup
  // ------------------------------------------------------------------

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
        performStartup();
      }

    });
  }

  /**
   * Perform startup of app (before switching to Swing thread)
   */
  private void performStartup() {
    SystemUtil.setConsoleAppFlag(false);
    processOptionalArgs();
    if (cmdLineArgs().hasNextArg())
      throw badArg("Unexpected argument(s):", cmdLineArgs().peekNextArg());

    // Continue starting app within the Swing thread
    //
    SwingUtilities.invokeLater(() -> {
      continueStartupWithinSwingThread();
    });
  }

  /**
   * Continue startup of app from within Swing thread
   */
  private void continueStartupWithinSwingThread() {
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

    mFrame.frame().setVisible(true);
  }

  /**
   * Construct the default UserOperation. Default returns a 'do nothing'
   * operation
   */
  public UserOperation getDefaultUserOperation() {
    alert("No default operation implemented");
    return new UserOperation();
  }

  /**
   * Called after GUI has been constructed; default does nothing
   */
  public void startedGUI() {
  }

  //------------------------------------------------------------------
  // Frame
  // ------------------------------------------------------------------

  public final FrameWrapper appFrame() {
    return mFrame;
  }

  private void createFrame() {
    mFrame = new FrameWrapper();
    mFrame.frame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    rebuildFrameContent();
    startPeriodicBackgroundTask();
    if (false) {
      // We need to make this call to ensure a menu bar exists, and to call revalidate() 
      performRepaint(~0);
    }
  }

  public final void rebuildFrameContent() {

    // Reset the widgets whenever we rebuild the frame
    initWidgets();

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

  /**
   * Get app frame's content pane
   */
  public final JComponent contentPane() {
    return (JComponent) mFrame.frame().getContentPane();
  }

  private FrameWrapper mFrame;

  public static final int REPAINT_EDITOR = (1 << 0);
  public static final int REPAINT_INFO = (1 << 1);
  public static final int REPAINT_ALL = ~0;

  /**
   * Trigger a repaint of various app components
   * 
   * @param repaintFlags
   *          a combination of REPAINT_xxx
   */
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
  // App title
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

  // ------------------------------------------------------------------
  // Mouse (or trackpad) pointer
  // ------------------------------------------------------------------

  public final void setMouseCursor(int type) {
    if (mFrame != null)
      mFrame.frame().setCursor(Cursor.getPredefinedCursor(type));
  }

  // ------------------------------------------------------------------
  // Menu bar
  // ------------------------------------------------------------------

  /**
   * Throw out any existing menu bar. It will get rebuilt by next call to
   * performRepaint()
   */
  public final void discardMenuBar() {
    mMenuBar = null;
  }

  private final void createMenuBarIfNec() {
    if (mMenuBar != null)
      return;
    KeyboardShortcutManager.sharedInstance().clearAssignedOperationList();
    MenuBarWrapper m = new MenuBarWrapper();
    mMenuBar = m;
    populateMenuBar(m);
    mFrame.frame().setJMenuBar(m.jmenuBar());
  }

  /**
   * Add menus to menu bar
   */
  public abstract void populateMenuBar(MenuBarWrapper m);

  /**
   * Add an item to the current menu
   */
  public final JMenuItem addItem(String hotKeyId, String displayedName, UserOperation operation) {
    return mMenuBar.addItem(hotKeyId, displayedName, operation);
  }

  private MenuBarWrapper mMenuBar;

  // ------------------------------------------------------------------
  // User events
  // ------------------------------------------------------------------

  /**
   * A listener for user events; default does nothing
   */
  public void userEventManagerListener(UserEvent event) {
  }

  // ------------------------------------------------------------------
  // Widgets
  // ------------------------------------------------------------------

  public void initWidgets() {
    mWidgetManager = new WidgetManager();
  }

  public final WidgetManager widgetManager() {
    return mWidgetManager;
  }

  private WidgetManager mWidgetManager;

  // ------------------------------------------------------------------
  // Performing periodic tasks on the Swing event thread
  // ------------------------------------------------------------------

  private void startPeriodicBackgroundTask() {
    mSwingTasks = new SwingTaskManager();
    mSwingTasks.addTask(() -> swingBackgroundTask()).start();
  }

  private SwingTaskManager mSwingTasks = new SwingTaskManager();

  protected SwingTaskManager taskManager() {
    return mSwingTasks;
  }

  /**
   * Called every ~3 seconds on the Swing event thread. Default does nothing
   */
  public void swingBackgroundTask() {
  }

}
