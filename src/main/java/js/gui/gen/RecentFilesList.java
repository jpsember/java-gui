package js.gui.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.file.Files;
import js.json.JSList;
import js.json.JSMap;

public class RecentFilesList implements AbstractData {

  public boolean active() {
    return mActive;
  }

  public List<File> files() {
    return mFiles;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "active";
  protected static final String _1 = "files";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mActive);
    {
      JSList j = new JSList();
      for (File x : mFiles)
        j.add(x.toString());
      m.put(_1, j);
    }
    return m;
  }

  @Override
  public RecentFilesList build() {
    return this;
  }

  @Override
  public RecentFilesList parse(Object obj) {
    return new RecentFilesList((JSMap) obj);
  }

  private RecentFilesList(JSMap m) {
    mActive = m.opt(_0, false);
    {
      List<File> result = new ArrayList<>();
      JSList j = m.optJSList(_1);
      if (j != null) {
        result = new ArrayList<>(j.size());
        for (Object z : j.wrappedList()) {
          File y = Files.DEFAULT;
          if (z != null) {
            String x = (String) z;
            y = new File(x);
          }
          result.add(y);
        }
      }
      mFiles = result;
    }
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof RecentFilesList))
      return false;
    RecentFilesList other = (RecentFilesList) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mActive == other.mActive))
      return false;
    if (!(mFiles.equals(other.mFiles)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (mActive ? 1 : 0);
      for (File x : mFiles)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mActive;
  protected List<File> mFiles;
  protected int m__hashcode;

  public static final class Builder extends RecentFilesList {

    private Builder(RecentFilesList m) {
      mActive = m.mActive;
      mFiles = m.mFiles;
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
    public RecentFilesList build() {
      RecentFilesList r = new RecentFilesList();
      r.mActive = mActive;
      r.mFiles = mFiles;
      return r;
    }

    public Builder active(boolean x) {
      mActive = x;
      return this;
    }

    public Builder files(List<File> x) {
      mFiles = (x == null) ? DataUtil.emptyList() : x;
      return this;
    }

  }

  public static final RecentFilesList DEFAULT_INSTANCE = new RecentFilesList();

  private RecentFilesList() {
    mFiles = DataUtil.emptyList();
  }

}
