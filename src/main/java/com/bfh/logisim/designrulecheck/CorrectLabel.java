/*******************************************************************************
 * This file is part of logisim-evolution.
 *
 *   logisim-evolution is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   logisim-evolution is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with logisim-evolution.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Original code by Carl Burch (http://www.cburch.com), 2011.
 *   Subsequent modifications by :
 *     + Haute École Spécialisée Bernoise
 *       http://www.bfh.ch
 *     + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *       http://hepia.hesge.ch/
 *     + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *       http://www.heig-vd.ch/
 *   The project is currently maintained by :
 *     + REDS Institute - HEIG-VD
 *       Yverdon-les-Bains, Switzerland
 *       http://reds.heig-vd.ch
 *******************************************************************************/

package com.bfh.logisim.designrulecheck;

import java.util.Arrays;
import java.util.List;

import com.bfh.logisim.fpgagui.FPGAReport;
import com.bfh.logisim.gui.FPGACliGuiFabric;
import com.bfh.logisim.gui.IFPGAOptionPanel;
import com.bfh.logisim.hdlgenerator.HDLGeneratorFactory;

public class CorrectLabel {
	public static String getCorrectLabel(String Label) {
		if (Label.isEmpty())
			return Label;
		StringBuffer result = new StringBuffer();
		if (Numbers.contains(Label.substring(0, 1)))
			result.append("L_");
		result.append(Label.replace(" ", "_").replace("-", "_"));
		return result.toString();
	}

	public static boolean IsCorrectLabel(String Label, String HDLIdentifier,
			String ErrorIdentifierString, FPGAReport Reporter) {
		if (Label.isEmpty())
			return true;
		for (int i = 0; i < Label.length(); i++) {
			if (!Chars.contains(Label.toLowerCase().substring(i, i + 1))
					&& !Numbers.contains(Label.substring(i, i + 1))) {
				Reporter.AddFatalError(ErrorIdentifierString
						+ " contains the illegal character \""
						+ Label.substring(i, i + 1) + "\", please rename.");
				return false;
			}
		}
		if (HDLIdentifier.equals(HDLGeneratorFactory.VHDL)) {
			if (VHDLKeywords.contains(Label.toLowerCase())) {
				Reporter.AddFatalError(ErrorIdentifierString
						+ " is a reserved VHDL keyword, please rename.");
				return false;
			}
		} else {
			if (HDLIdentifier.equals(HDLGeneratorFactory.VERILOG)) {
				if (VerilogKeywords.contains(Label)) {
					Reporter.AddFatalError(ErrorIdentifierString
							+ " is a reserved Verilog keyword, please rename.");
					return false;
				}
			}
		}
		return true;
	}

	public static String HDLCorrectLabel(String Label) {
		if (Label.isEmpty())
			return null;
		if (VHDLKeywords.contains(Label.toLowerCase()))
			return HDLGeneratorFactory.VHDL;
		if (VerilogKeywords.contains(Label))
			return HDLGeneratorFactory.VERILOG;
		return null;
	}

	public static boolean IsCorrectLabel(String Label) {
		if (Label.isEmpty())
			return true;
		for (int i = 0; i < Label.length(); i++) {
			if (!Chars.contains(Label.toLowerCase().substring(i, i + 1))
					&& !Numbers.contains(Label.substring(i, i + 1))) {
				return false;
			}
		}
		if (VHDLKeywords.contains(Label.toLowerCase()))
			return false;
		if (VerilogKeywords.contains(Label))
			return false;
		return true;
	}

	public static String FirstInvalidCharacter(String Label) {
		if (Label.isEmpty())
			return "";
		for (int i = 0; i < Label.length(); i++) {
			if (!Chars.contains(Label.toLowerCase().substring(i, i + 1))
					&& !Numbers.contains(Label.substring(i, i + 1))) {
				return Label.toLowerCase().substring(i, i + 1);
			}
		}
		return "";
	}

	public static boolean IsCorrectLabel(String Label, String HDLIdentifier) {
		if (Label.isEmpty())
			return true;
		for (int i = 0; i < Label.length(); i++) {
			if (!Chars.contains(Label.toLowerCase().substring(i, i + 1))
					&& !Numbers.contains(Label.substring(i, i + 1))) {
				return false;
			}
		}
		if (HDLIdentifier.equals(HDLGeneratorFactory.VHDL)) {
			if (VHDLKeywords.contains(Label.toLowerCase())) {
				return false;
			}
		} else {
			if (HDLIdentifier.equals(HDLGeneratorFactory.VERILOG)) {
				if (VerilogKeywords.contains(Label)) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean IsKeyword(String Label,Boolean ShowDialog) {
		boolean ret = false;

		IFPGAOptionPanel optionPanel = FPGACliGuiFabric.getFPGAOptionPanel();
		if (VHDLKeywords.contains(Label.toLowerCase())) {
			ret = true;
			if (ShowDialog) optionPanel.doshowMessageDialog(null, Strings.get("VHDLKeywordNameError"));
		} else if (VerilogKeywords.contains(Label.toLowerCase())) {
			if (ShowDialog) optionPanel.doshowMessageDialog(null, Strings.get("VerilogKeywordNameError"));
			ret = true;
		}
		return ret;
	}

	private static final String[] NumbersStr = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9" };
	public static final List<String> Numbers = Arrays.asList(NumbersStr);
	private static final String[] AllowedStrings = { "a", "b", "c", "d", "e",
			"f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
			"s", "t", "u", "v", "w", "x", "y", "z", " ", "-", "_" };
	private static final List<String> Chars = Arrays.asList(AllowedStrings);
	private static final String[] ReservedVHDLWords = { "abs", "access",
			"after", "alias", "all", "and", "architecture", "array", "assert",
			"attribute", "begin", "block", "body", "buffer", "bus", "case",
			"component", "configuration", "constant", "disconnect", "downto",
			"else", "elsif", "end", "entity", "exit", "file", "for",
			"function", "generate", "generic", "group", "guarded", "if",
			"impure", "in", "inertial", "inout", "is", "label", "library",
			"linkage", "literal", "loop", "map", "mod", "nand", "new", "next",
			"nor", "not", "null", "of", "on", "open", "or", "others", "out",
			"package", "port", "postponed", "procedure", "process", "pure",
			"range", "record", "register", "reject", "rem", "report", "return",
			"rol", "ror", "select", "severity", "signal", "shared", "sla",
			"sll", "sra", "srl", "subtype", "then", "to", "transport", "type",
			"unaffected", "units", "until", "use", "variable", "wait", "when",
			"while", "with", "xnor", "xor" };
	private static final List<String> VHDLKeywords = Arrays
			.asList(ReservedVHDLWords);

	private static final String[] ReservedVerilogWords = { "always", "ifnone",
			"rpmos", "and", "initial", "rtran", "assign", "inout", "rtranif0",
			"begin", "input", "rtranif1", "buf", "integer", "scalared",
			"bufif0", "join", "small", "bufif1", "large", "specify", "case",
			"macromodule", "specparam", "casex", "medium", "strong0", "casez",
			"module", "strong1", "cmos", "nand", "supply0", "deassign",
			"negedge", "supply1", "default", "nmos", "table", "defparam",
			"nor", "task", "disable", "not", "time", "edge", "notif0", "tran",
			"else", "notif1", "tranif0", "end", "or", "tranif1", "endcase",
			"output", "tri", "endmodule", "parameter", "tri0", "endfunction",
			"pmos", "tri1", "endprimitive", "posedge", "triand", "endspecify",
			"primitive", "trior", "endtable", "pull0", "trireg", "endtask",
			"pull1", "vectored", "event", "pullup", "wait", "for", "pulldown",
			"wand", "force", "rcmos", "weak0", "forever", "real", "weak1",
			"fork", "realtime", "while", "function", "reg", "wire", "highz0",
			"release", "wor", "highz1", "repeat", "xnor", "if", "rnmos", "xor",
			"automatic", "incdir", "pulsestyle_ondetect", "cell", "include",
			"pulsestyle_onevent", "config", "instance", "signed", "endconfig",
			"liblist", "showcancelled", "endgenerate", "library", "unsigned",
			"generate", "localparam", "use", "genvar", "noshowcancelled" };

	private static final List<String> VerilogKeywords = Arrays
			.asList(ReservedVerilogWords);
}
