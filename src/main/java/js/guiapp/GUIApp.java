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

import javax.swing.SwingUtilities;

import js.app.App;
import js.app.AppOper;
import js.system.SystemUtil;

public abstract class GUIApp extends App {

  /**
   * 
   * @param createAndShowGUIMethod
   *          method to call from Swing thread to construct the GUI and show it
   */
  public void startGUI(Runnable createAndShowGUIMethod) {
    SystemUtil.setConsoleAppFlag(false);

    // Start app within Swing thread
    //
    SwingUtilities.invokeLater(() -> {
      SwingUtils.setEventDispatchThread();
      createAndShowGUIMethod.run();
    });
  }

  /**
   * Construct the (singleton) AppOper for this GUI app
   */
  protected abstract AppOper constructAppOper();

  @Override
  protected final void registerOperations() {
    registerOper(constructAppOper());
  }

}
