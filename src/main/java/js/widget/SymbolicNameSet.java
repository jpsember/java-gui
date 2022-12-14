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

import java.util.List;

import js.json.JSList;
import js.json.JSMap;

import static js.base.Tools.*;

/**
 * A list of symbolic names, and display counterparts
 */
public final class SymbolicNameSet {

  public void add(String nameExpression) {
    String symbolicName;
    String displayName;
    int sepPos = nameExpression.indexOf(':');
    if (sepPos >= 0) {
      symbolicName = nameExpression.substring(0, sepPos).trim();
      displayName = nameExpression.substring(1 + sepPos).trim();
    } else {
      symbolicName = nameExpression;
      displayName = nameExpression;
    }
    mSymbolicNames.add(symbolicName);
    mDisplayNames.add(displayName);
  }

  public int getSymbolicIndex(String value) {
    int index = mSymbolicNames.indexOf(value);
    if (index < 0) {
      if (!nullOrEmpty(value))
        pr("symbolic name", value, "is not in list:", mSymbolicNames);
      index = 0;
    }
    return index;
  }

  public String displayToSymbolic(String displayName) {
    int i = mDisplayNames.indexOf(displayName);
    if (i < 0) {
      pr("display name", displayName, "doesn't exist:", mDisplayNames);
      i = 0;
    }
    return mSymbolicNames.get(i);
  }

  public int size() {
    return displayNames().size();
  }

  public String getSymbolicName(int position) {
    if (position < 0 || position >= size()) {
      pr("*** out of range symbolic name index:", position);
      position = 0;
    }
    return mSymbolicNames.get(position);
  }

  public String validateSymbolicName(String valueOrNull) {
    if (valueOrNull == null || !mSymbolicNames.contains(valueOrNull))
      valueOrNull = mSymbolicNames.get(0);
    return valueOrNull;
  }

  public JSMap json() {
    JSMap m = mapWithClassName(this);
    m.put("display_names", JSList.withStringRepresentationsOf(displayNames()));
    m.put("symbolic_names", JSList.withStringRepresentationsOf(symbolicNames()));
    return m;
  }

  @Override
  public String toString() {
    return json().prettyPrint();
  }

  public List<String> displayNames() {
    return mDisplayNames;
  }

  public List<String> symbolicNames() {
    return mSymbolicNames;
  }

  private List<String> mSymbolicNames = arrayList();
  private List<String> mDisplayNames = arrayList();

}