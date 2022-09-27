package js.widget;


import static js.base.Tools.*;


class HiddenWidget extends Widget {

  public HiddenWidget(Object defaultValue) {
    mValue = defaultValue;
    todo("support for Widget read/write values?");
  }

  @Override
  public Object readValue() {
    return mValue;
  }

  @Override
  public void writeValue(Object v) {
    checkArgument(v != null);
    mValue = v;
  }

  @Override
  public void setText(String text) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getText() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHint(String hint) {
    throw new UnsupportedOperationException();
  }

  private Object mValue;
}