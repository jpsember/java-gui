package js.gui.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class HotKey implements AbstractData {

  public int code() {
    return mCode;
  }

  public String modifiers() {
    return mModifiers;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "code";
  protected static final String _1 = "modifiers";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mCode);
    m.putUnsafe(_1, mModifiers);
    return m;
  }

  @Override
  public HotKey build() {
    return this;
  }

  @Override
  public HotKey parse(Object obj) {
    return new HotKey((JSMap) obj);
  }

  private HotKey(JSMap m) {
    mCode = m.opt(_0, 0);
    mModifiers = m.opt(_1, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof HotKey))
      return false;
    HotKey other = (HotKey) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mCode == other.mCode))
      return false;
    if (!(mModifiers.equals(other.mModifiers)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mCode;
      r = r * 37 + mModifiers.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected int mCode;
  protected String mModifiers;
  protected int m__hashcode;

  public static final class Builder extends HotKey {

    private Builder(HotKey m) {
      mCode = m.mCode;
      mModifiers = m.mModifiers;
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public int hashCode() {
      m__hashcode = 0;
      return super.hashCode();
    }

    @Override
    public HotKey build() {
      HotKey r = new HotKey();
      r.mCode = mCode;
      r.mModifiers = mModifiers;
      return r;
    }

    public Builder code(int x) {
      mCode = x;
      return this;
    }

    public Builder modifiers(String x) {
      mModifiers = (x == null) ? "" : x;
      return this;
    }

  }

  public static final HotKey DEFAULT_INSTANCE = new HotKey();

  private HotKey() {
    mModifiers = "";
  }

}
