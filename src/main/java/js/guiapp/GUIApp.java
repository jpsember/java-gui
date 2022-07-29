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

import java.util.List;

import javax.swing.SwingUtilities;

import js.app.App;
import js.app.AppOper;
import js.data.AbstractData;
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
   * Construct the default operation for the event manager. Default returns
   * a 'do nothing' operation
   */
  public UserOperation getDefaultUserOperation() {
    alert("No getDefaultAppOper implemented");
    return new UserOperation() {};
  }

  /**
   * A listener for user events; default does nothing
   */
  public void userEventManagerListener(UserEvent event) {
  }

  private void startGUI() {
    SystemUtil.setConsoleAppFlag(false);

    // Start app within Swing thread
    //
    SwingUtilities.invokeLater(() -> {
      SwingUtils.setEventDispatchThread();

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
      createAndShowGUI();
    });
  }

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

  public abstract void createAndShowGUI();

  @Override
  protected final void registerOperations() {
    registerOper(new SingletonAppOper());
  }

  private class SingletonAppOper extends AppOper {

    @Override
    public String userCommand() {
      return null;
    }

    @Override
    public void perform() {

      processOptionalArgs();
      if (cmdLineArgs().hasNextArg())
        throw badArg("Unexpected argument(s):", cmdLineArgs().peekNextArg());

      startGUI();
    }

    @Override
    protected List<Object> getAdditionalArgs() {
      return getOptionalArgDescriptions();
    }

  }

}
