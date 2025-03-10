package js.widget;

import static js.base.Tools.*;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import js.app.App;
import js.guiapp.GUIApp;
import js.guiapp.UserEvent;

class SliderWidget extends Widget implements ChangeListener {
  public SliderWidget(WidgetListener listener, String key, boolean floatsFlag, Number defaultValue,
      Number minimum, Number maximum, boolean includesDisplay) {
    setId(key);
    mStepper = new NumericStepper(floatsFlag, defaultValue, minimum, maximum, null);
    JComponent component;
    JSlider slider = new JSlider(mStepper.internalMin(), mStepper.internalMax(), mStepper.internalVal());
    slider.addChangeListener(this);
    mSlider = slider;
    if (includesDisplay) {
      int maxValueStringLength = mStepper.maxDigits();
      mDisplay = new JTextField(maxValueStringLength);
      mDisplay.setEditable(false);
      mDisplay.setHorizontalAlignment(SwingConstants.RIGHT);
      JPanel container = new JPanel();
      // Make the container's layout a BorderLayout, with the slider grabbing the available space
      container.setLayout(new BorderLayout());
      container.add(slider, BorderLayout.CENTER);
      container.add(mDisplay, BorderLayout.EAST);
      component = container;
      updateDisplayValue();
    } else {
      component = slider;
    }
    setComponent(component);
    registerListener(listener);
  }

  @Override
  public JComponent componentForTooltip() {
    return getSlider();
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    updateDisplayValue();
    GUIApp app = App.sharedInstance();
    app.userEventManagerListener(UserEvent.widgetEvent(id()));
    notifyListener();
  }

  private void updateDisplayValue() {
    if (mDisplay == null)
      return;
    int numValue = getSlider().getModel().getValue();
    String value = mStepper.formatNumber(mStepper.fromInternalUnits(numValue));
    mDisplay.setText(value);
  }

  public JSlider getSlider() {
    return mSlider;
  }

  @Override
  public void setValue(Number number) {
    var internalValue = mStepper.toInternalUnits(number);
    if (DEB())
      pr("setValue", number, "internal:", internalValue);
    getSlider().getModel().setValue(internalValue);
    updateDisplayValue();
    notifyListener();
  }

  @Override
  public Number readValue() {
    var internalValue = getSlider().getModel().getValue();
    var externalValue = mStepper.fromInternalUnits(internalValue);
    if (DEB())
      pr("readValue; internal:", internalValue, "external:", externalValue);
    return externalValue;
  }

  @Override
  public void writeValue(Object v) {
    if (DEB())
      pr("writeValue:", v);
    Number number = (Number) v;
    setValue(number);
  }

  private NumericStepper mStepper;
  private JTextField mDisplay;
  private JSlider mSlider;
}