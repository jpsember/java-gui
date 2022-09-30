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
package js.widget;

import static js.base.Tools.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

import js.guiapp.GUIApp;

/**
 * Abstract class representing a user interface element
 */
public abstract class Widget implements ActionListener {

  public final Widget setId(String id) {
    checkState(!hasId(), "already has an id");
    // If id is not null, it cannot be empty
    checkArgument(!"".equals(id));
    mId = id;
    return this;
  }

  public final boolean hasId() {
    return mId != null;
  }

  public final String id() {
    if (mId == null)
      throw badState("widget has no id");
    return mId;
  }

  public final String optionalId() {
    return ifNullOrEmpty(mId, "<no id>");
  }

  private String mId;

  public abstract Object readValue();

  public abstract void writeValue(Object v);

  public static void setTextIfExists(Widget widget, String text) {
    if (widget != null)
      widget.setText(text);
  }

  public void appendText(String text) {
    throw new UnsupportedOperationException();
  }

  protected final void registerListener(WidgetListener listener) {
    mListener = listener;
  }

  private WidgetManager widgets() {
    return GUIApp.sharedInstance().widgetManager();
  }

  /**
   * Notify WidgetListener, if there is one, of an event involving this widget
   */
  protected final void notifyListener() {
    if (mListener != null)
      widgets().notifyWidgetListener(this, mListener);
  }

  @Override
  public String toString() {
    return id() + ":" + getClass().getSimpleName();
  }

  public void displayKeyboard() {
    throw new UnsupportedOperationException();
  }

  public void hideKeyboard() {
    throw new UnsupportedOperationException();
  }

  public void setInputType(int inputType) {
    throw new UnsupportedOperationException();
  }

  public void setEnabled(boolean enabled) {
    throw notSupported(className());
  }

  public boolean enabled() {
    throw notSupported(className());
  }

  public void setVisible(boolean visible) {
    todo("setVisible not implemented for:", className());
  }

  public void doClick() {
    throw new UnsupportedOperationException();
  }

  public void setChecked(boolean state) {
    throw new UnsupportedOperationException();
  }

  public boolean isChecked() {
    throw new UnsupportedOperationException();
  }

  public void setValue(Number number) {
    throw new UnsupportedOperationException();
  }

  /**
   * Cause the wrapped component to be repainted
   */
  public void repaint() {
    throw new UnsupportedOperationException();
  }

  /**
   * Replace this widget in its view hierarchy with another
   */
  public void replaceWith(Widget other) {
    throw new UnsupportedOperationException();
  }

  //public SwingWidget(WidgetManager manager, String key) {
  //  super(manager, key);
  //}

  public void actionPerformed(ActionEvent e) {
    notifyListener();
  }

  public void setComponent(JComponent component) {
    mWrappedComponent = component;
  }

  public final JComponent component() {
    return mWrappedComponent;
  }

  public final <T extends JComponent> T swingComponent() {
    return (T) component();
  }

  /**
   * Get component to attach tooltip to (if there is one). Default
   * implementation returns swingComponent()
   */
  public JComponent componentForTooltip() {
    return swingComponent();
  }

  public void setText(String text) {
    throw new UnsupportedOperationException();
  }

  public String getText() {
    throw new UnsupportedOperationException();
  }

  public void setHint(String hint) {
    throw new UnsupportedOperationException();
  }

  private String className() {
    return getClass().getSimpleName();
  }

  private WidgetListener mListener;
  private JComponent mWrappedComponent;

}
