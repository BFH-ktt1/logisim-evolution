/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io.extra;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.generic.OptionPane;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.util.LocaleManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class PlaRomData implements InstanceData {
  private int inputs;
  private int outputs;
  private int and;
  private boolean[][] inputAnd;
  private boolean[][] andOutput;
  public int rowHovered = -1;
  public int columnHovered = 0;
  private Value[] inputValue;
  private Value[] andValue;
  private Value[] outputValue;
  private final String[] options =
      new String[] {
        new LocaleManager("resources/logisim", "gui").get("saveOption"), S.get("ramClearMenuItem")
      };
  private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  private JScrollPane panel;
  private PlaRomPanel drawing;
  private String myInitialContents;
  
  public PlaRomData(String initValue) {
    inputs = 0;
    outputs = 0;
    and = 0;
    myInitialContents = initValue;
  }

  public PlaRomData(int inputs, int outputs, int and) {
    this.inputs = inputs;
    this.outputs = outputs;
    this.and = and;
    inputAnd = new boolean[getAnd()][getInputs() * 2];
    andOutput = new boolean[getAnd()][getOutputs()];
    inputValue = new Value[getInputs()];
    andValue = new Value[getAnd()];
    outputValue = new Value[getOutputs()];
    initializeInputValue();
    setAndValue();
    setOutputValue();
  }

  public void clearMatrixValues() {
    for (int i = 0; i < getAnd(); i++) {
      for (int j = 0; j < getOutputs(); j++) {
        setAndOutputValue(i, j, false);
      }
      for (int k = 0; k < getInputs() * 2; k++) {
        setInputAndValue(i, k, false);
      }
    }
  }

  @Override
  public PlaRomData clone() {
    try {
      return (PlaRomData) super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public void decodeSavedData(String str) {
    // if empty, all to false so don't do anything
    if (str == null || str.equals("")) return;
    // split the attribute content string in an array of strings with a single
    // information each one
    final var datas = str.split(" ");
    String[] tmp;
    int value;
    var cnt = 0;
    for (final var data : datas) {
      // if contains a '*' it has to fill the array with the first value for x (second
      // number) cycles
      if (data.contains("*")) {
        tmp = data.split("\\*");
        for (var j = 0; j < Integer.parseInt(tmp[1]); j++) {
          value = (int) Integer.parseInt(tmp[0]);
          writeData(value, cnt);
          cnt++;
        }
      } else {
        value = (int) Integer.parseInt(data);
        writeData(value, cnt);
        cnt++;
      }
    }
  }

  public int editWindow() {
    this.drawing = new PlaRomPanel(this);
    panel =
        new JScrollPane(
            this.drawing,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    panel.setBorder(null);
    panel.getVerticalScrollBar().setUnitIncrement(10);
    if (this.drawing.getPreferredSize().getWidth() >= (int) (screenSize.width * 0.75))
      panel.setPreferredSize(
          new Dimension(
              (int) (screenSize.width * 0.75), (int) panel.getPreferredSize().getHeight()));
    if (this.drawing.getPreferredSize().getHeight() >= (int) (screenSize.height * 0.75))
      panel.setPreferredSize(
          new Dimension(
              (int) panel.getPreferredSize().getWidth(), (int) (screenSize.height * 0.75)));
    int ret =
        OptionPane.showOptionDialog(
            null,
            panel,
            S.get("PlaEditWindowTitel", getSizeString()),
            OptionPane.OK_CANCEL_OPTION,
            OptionPane.PLAIN_MESSAGE,
            null,
            this.options,
            null);
    return ret;
  }

  public int getAnd() {
    return and;
  }

  public boolean getAndOutputValue(int row, int column) {
    return this.andOutput[row][column];
  }

  public Value getAndValue(int i) {
    return andValue[i];
  }

  public boolean getInputAndValue(int row, int column) {
    return this.inputAnd[row][column];
  }

  public int getInputs() {
    return inputs;
  }

  public Value getInputValue(int i) {
    return this.inputValue[i];
  }

  public int getOutputs() {
    return outputs;
  }

  public Value getOutputValue(int i) {
    return outputValue[i];
  }

  public Value[] getOutputValues() {
    final var outputValueCopy = new Value[getOutputs()];
    // reverse array
    for (int i = (int) (getOutputs() - 1); i >= 0; i--) {
      outputValueCopy[i] = outputValue[outputValue.length - i - 1];
    }
    return outputValueCopy;
  }

  public String getSizeString() {
    return String.format("%sx%sx%s", getInputs(), getAnd(), getOutputs());
  }

  private void initializeInputValue() {
    for (int i = 0; i < getInputs(); i++) {
      inputValue[i] = Value.UNKNOWN;
    }
  }

  public static String toStandardString(PlaRomData plaData) {
    // string to write inside the .circ to not lose data
    int row, column, size1 = plaData.getInputs() * plaData.getAnd(), size2 = plaData.getOutputs() * plaData.getAnd(), count = 0;
    char val, last = 'x';
    final var contents = new StringBuilder();
    // input-and matrix
    for (var i = 0; i < size1; i++) {
      row = i / plaData.getInputs();
      column = i - row * plaData.getInputs();
      // 1= not line selected, 2 = input line selected, 0 = nothing selected in that
      // input line
      if (plaData.inputAnd[row][column * 2]) {
        val = '1';
      } else if (plaData.inputAnd[row][column * 2 + 1]) {
        val = '2';
      } else val = '0';
      if (val == last) count++;
      else if (last == 'x') {
        last = val;
        count++;
      }
      if (val != last || i == size1 - 1) {
        if (count >= 3) contents.append(last).append("*").append(count).append(' ');
        else for (int j = 0; j < count; j++) contents.append(last).append(" ");
        if (val != last && i == size1 - 1) contents.append(val).append(" ");
        count = 1;
        last = val;
      }
    }
    last = 'x';
    count = 0;
    // and-or matrix
    for (int i = 0; i < size2; i++) {
      row = i / plaData.getOutputs();
      column = i - row * plaData.getOutputs();
      // 0 = nothing selected, 1 = node selected
      if (plaData.andOutput[row][column]) {
        val = '1';
      } else val = '0';
      if (val == last) count++;
      else if (last == 'x') {
        last = val;
        count++;
      }
      if (val != last || i == size2 - 1) {
        if (count >= 3) contents.append(last).append("*").append(count).append(' ');
        else for (int j = 0; j < count; j++) contents.append(last).append(" ");
        if (val != last && i == size2 - 1) contents.append(val).append(" ");
        count = 1;
        last = val;
      }
    }
    return contents.toString();
  }

  public void setAndOutputValue(int row, int column, boolean b) {
    andOutput[row][column] = b;
    // update all values
    setAndValue();
    setOutputValue();
  }

  private void setAndValue() {
    boolean thereisadot = false;
    for (int i = 0; i < getAnd(); i++) {
      andValue[i] = Value.TRUE;
      for (int j = 0; j < getInputs() * 2; j++) {
        if (getInputAndValue(i, j)) {
          thereisadot = true;
          if (j % 2 == 0) { // not
            if (!getInputValue((int) (j / 2)).isFullyDefined()) andValue[i] = Value.ERROR;
            else if (getInputValue((int) (j / 2)) == Value.TRUE) {
              andValue[i] = Value.FALSE;
              break;
            }
          } else if (j % 2 == 1) {
            if (!getInputValue((int) ((j - 1) / 2)).isFullyDefined()) andValue[i] = Value.ERROR;
            else if (getInputValue((int) ((j - 1) / 2)) == Value.FALSE) {
              andValue[i] = Value.FALSE;
              break;
            }
          }
        }
      }
      if (!thereisadot) andValue[i] = Value.ERROR;
      thereisadot = false;
    }
  }

  public void setHovered(int row, int column) {
    rowHovered = row;
    columnHovered = column;
  }

  public void setInputAndValue(int row, int column, boolean b) {
    this.inputAnd[row][column] = b;
    // update all values
    setAndValue();
    setOutputValue();
  }

  public void setInputsValue(Value[] inputs) {
    int mininputs = getInputs() < inputs.length ? getInputs() : inputs.length;
    System.arraycopy(inputs, inputs.length - mininputs, this.inputValue, getInputs() - mininputs, mininputs);
    setAndValue();
    setOutputValue();
  }

  private void setOutputValue() {
    var thereisadot = false;
    for (int i = 0; i < getOutputs(); i++) {
      outputValue[i] = Value.FALSE;
      for (int j = 0; j < getAnd(); j++) {
        if (getAndOutputValue(j, i)) {
          outputValue[i] = outputValue[i].or(getAndValue(j));
          thereisadot = true;
        }
      }
      if (!thereisadot) outputValue[i] = Value.ERROR;
      thereisadot = false;
    }
  }

  public boolean updateSize(int inputs, int outputs, int and) {
    if (this.inputs != inputs || this.outputs != outputs || this.and != and) {
      final var mininputs = getInputs() < inputs ? getInputs() : inputs;
      final var minoutputs = getOutputs() < outputs ? getOutputs() : outputs;
      final var minand = getAnd() < and ? getAnd() : and;
      this.inputs = inputs;
      this.outputs = outputs;
      this.and = and;
      final var oldInputAnd = Arrays.copyOf(inputAnd, inputAnd.length);
      final var oldAndOutput = Arrays.copyOf(andOutput, andOutput.length);
      inputAnd = new boolean[getAnd()][getInputs() * 2];
      andOutput = new boolean[getAnd()][getOutputs()];
      inputValue = new Value[getInputs()];
      andValue = new Value[getAnd()];
      outputValue = new Value[getOutputs()];
      for (int i = 0; i < minand; i++) {
        System.arraycopy(oldInputAnd[i], 0, inputAnd[i], 0, mininputs * 2);
        System.arraycopy(oldAndOutput[i], 0, andOutput[i], 0, minoutputs);
      }
      initializeInputValue();
      setAndValue();
      setOutputValue();
      return true;
    }
    return false;
  }

  private void writeData(int value, int node) {
    int row;
    int column;
    // first matrix
    if (node < getInputs() * getAnd()) {
      row = node / getInputs();
      column = node - row * getInputs();
      switch (value) {
        case 0:
          // none selected
          inputAnd[row][column * 2] = false;
          inputAnd[row][column * 2 + 1] = false;
          break;
        case 1:
          // not selected
          inputAnd[row][column * 2] = true;
          inputAnd[row][column * 2 + 1] = false;
          break;
        case 2:
          // normal input selected
          inputAnd[row][column * 2] = false;
          inputAnd[row][column * 2 + 1] = true;
          break;
        default:
          System.err.println("PlaRom: Error in saved data ");
          return;
      }
    } else if (node < getInputs() * getAnd() + getOutputs() * getAnd()) {
      // second matrix
      node -= getInputs() * getAnd();
      row = node / getOutputs();
      column = node - row * getOutputs();
      switch (value) {
        case 0:
          // not selected
          andOutput[row][column] = false;
          break;
        case 1:
          andOutput[row][column] = true;
          break;
        default:
          System.err.println("PlaRom: Error in saved data 2");
          return;
      }
    }
  }
}
