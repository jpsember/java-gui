package js.widget;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
    GUIApp.sharedInstance().userEventManagerListener(UserEvent.widgetEvent(id()));
    notifyListener();
  }

  private void updateDisplayValue() {
    if (mDisplay == null)
      return;
    int numValue = getSlider().getModel().getValue();
    String value = mStepper.fromInternalUnits(numValue).toString();
    mDisplay.setText(value);
  }

  public JSlider getSlider() {
    return mSlider;
  }

  @Override
  public void setValue(Number number) {
    getSlider().getModel().setValue(number.intValue());
    updateDisplayValue();
    notifyListener();
  }

  @Override
  public Number readValue() {
    return getSlider().getModel().getValue();
  }

  @Override
  public void writeValue(Object v) {
    Number number = (Number) v;
    setValue(number);
  }

  private NumericStepper mStepper;
  private JTextField mDisplay;
  private JSlider mSlider;
}