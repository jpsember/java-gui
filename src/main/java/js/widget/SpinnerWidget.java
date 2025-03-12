package js.widget;

import static js.base.Tools.*;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class SpinnerWidget extends Widget implements ChangeListener {
  public SpinnerWidget(WidgetListener listener, String key, boolean floatsFlag, Number defaultValue,
      Number minimum, Number maximum, Number stepSize) {
    mStepper = new NumericStepper(floatsFlag, defaultValue, minimum, maximum, stepSize);
    checkState(mStepper.isInt(), "non-integer not supported");
    SpinnerModel model = new SpinnerNumberModel(mStepper.def().intValue(), mStepper.min().intValue(),
        mStepper.max().intValue(), mStepper.step().intValue());
    JSpinner component = new JSpinner(model);
    model.addChangeListener(this);
    setComponent(component);
    registerListener(listener);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    notifyListener();
    notifyApp();
  }

  @Override
  public Number readValue() {
    return (Number) spinner().getModel().getValue();
  }

  @Override
  public void writeValue(Object v) {
    Number number = (Number) v;
    setValue(number);
  }

  private JSpinner spinner() {
    return swingComponent();
  }

  private NumericStepper mStepper;
}