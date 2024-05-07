package js.gui.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class GuiAppConfig implements AbstractData {

  public boolean devMode() {
    return mDevMode;
  }

  public boolean singleInstanceMode() {
    return mSingleInstanceMode;
  }

  public String appName() {
    return mAppName;
  }

  public String version() {
    return mVersion;
  }

  public JSMap keyboardShortcutRegistry() {
    return mKeyboardShortcutRegistry;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "dev_mode";
  protected static final String _1 = "single_instance_mode";
  protected static final String _2 = "app_name";
  protected static final String _3 = "version";
  protected static final String _4 = "keyboard_shortcut_registry";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mDevMode);
    m.putUnsafe(_1, mSingleInstanceMode);
    m.putUnsafe(_2, mAppName);
    m.putUnsafe(_3, mVersion);
    m.putUnsafe(_4, mKeyboardShortcutRegistry);
    return m;
  }

  @Override
  public GuiAppConfig build() {
    return this;
  }

  @Override
  public GuiAppConfig parse(Object obj) {
    return new GuiAppConfig((JSMap) obj);
  }

  private GuiAppConfig(JSMap m) {
    mDevMode = m.opt(_0, true);
    mSingleInstanceMode = m.opt(_1, true);
    mAppName = m.opt(_2, "***NO NAME DEFINED***");
    mVersion = m.opt(_3, "1.0");
    {
      mKeyboardShortcutRegistry = JSMap.DEFAULT_INSTANCE;
      JSMap x = m.optJSMap(_4);
      if (x != null) {
        mKeyboardShortcutRegistry = x.lock();
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof GuiAppConfig))
      return false;
    GuiAppConfig other = (GuiAppConfig) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mDevMode == other.mDevMode))
      return false;
    if (!(mSingleInstanceMode == other.mSingleInstanceMode))
      return false;
    if (!(mAppName.equals(other.mAppName)))
      return false;
    if (!(mVersion.equals(other.mVersion)))
      return false;
    if (!(mKeyboardShortcutRegistry.equals(other.mKeyboardShortcutRegistry)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (mDevMode ? 1 : 0);
      r = r * 37 + (mSingleInstanceMode ? 1 : 0);
      r = r * 37 + mAppName.hashCode();
      r = r * 37 + mVersion.hashCode();
      r = r * 37 + mKeyboardShortcutRegistry.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mDevMode;
  protected boolean mSingleInstanceMode;
  protected String mAppName;
  protected String mVersion;
  protected JSMap mKeyboardShortcutRegistry;
  protected int m__hashcode;

  public static final class Builder extends GuiAppConfig {

    private Builder(GuiAppConfig m) {
      mDevMode = m.mDevMode;
      mSingleInstanceMode = m.mSingleInstanceMode;
      mAppName = m.mAppName;
      mVersion = m.mVersion;
      mKeyboardShortcutRegistry = m.mKeyboardShortcutRegistry;
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
    public GuiAppConfig build() {
      GuiAppConfig r = new GuiAppConfig();
      r.mDevMode = mDevMode;
      r.mSingleInstanceMode = mSingleInstanceMode;
      r.mAppName = mAppName;
      r.mVersion = mVersion;
      r.mKeyboardShortcutRegistry = mKeyboardShortcutRegistry;
      return r;
    }

    public Builder devMode(boolean x) {
      mDevMode = x;
      return this;
    }

    public Builder singleInstanceMode(boolean x) {
      mSingleInstanceMode = x;
      return this;
    }

    public Builder appName(String x) {
      mAppName = (x == null) ? "***NO NAME DEFINED***" : x;
      return this;
    }

    public Builder version(String x) {
      mVersion = (x == null) ? "1.0" : x;
      return this;
    }

    public Builder keyboardShortcutRegistry(JSMap x) {
      mKeyboardShortcutRegistry = (x == null) ? JSMap.DEFAULT_INSTANCE : x;
      return this;
    }

  }

  public static final GuiAppConfig DEFAULT_INSTANCE = new GuiAppConfig();

  private GuiAppConfig() {
    mDevMode = true;
    mSingleInstanceMode = true;
    mAppName = "***NO NAME DEFINED***";
    mVersion = "1.0";
    mKeyboardShortcutRegistry = JSMap.DEFAULT_INSTANCE;
  }

}
