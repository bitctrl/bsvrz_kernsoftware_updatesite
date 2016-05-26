/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.consoleProcessFrame.
 * 
 * de.bsvrz.sys.funclib.consoleProcessFrame is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.consoleProcessFrame is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.consoleProcessFrame; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.consoleProcessFrame;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

/**
 * Implementiert ein JPanel, das mit einem externen Prozess verbunden ist. Der Prozess kann gestartet und beendet werden. Die Textausgaben des Prozesses werden
 * im JPanel dargestellt.
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConsoleProcessPanel extends JPanel {

	private static final List<Process> _runningProcesses;

	static {
		_runningProcesses = new LinkedList<Process>();
		Runtime.getRuntime().addShutdownHook(
				new Thread(
						new Runnable() {
							public void run() {
								synchronized(_runningProcesses) {
									for(Iterator iterator = _runningProcesses.iterator(); iterator.hasNext();) {
										Process process = (Process)iterator.next();
										process.destroy();
									}
								}
							}
						}
				)
		);
	}

	private final Process _process;

	private final JTextPane _textPane;

	private final Thread _processOutputHatch;

	private final Thread _processErrorHatch;

	protected ConsoleProcessPanel(String[] commandArray, String[] environment, File workingDirectory) throws IOException {
		super(new BorderLayout());
		_process = Runtime.getRuntime().exec(commandArray, environment, workingDirectory);
		synchronized(_runningProcesses) {
			_runningProcesses.add(_process);
		}
		_textPane = new JTextPane(new DefaultStyledDocument());
		_processErrorHatch = new Thread(new TextPaneHatch(_textPane, _process.getErrorStream()));
		_processOutputHatch = new Thread(new TextPaneHatch(_textPane, _process.getInputStream()));
		Font font = new Font("Monospaced", Font.PLAIN, 11);
		_textPane.setFont(font);
		_textPane.setMargin(new Insets(5, 5, 5, 5));
		_textPane.setText("");
		JScrollPane scrollpane = new JScrollPane(_textPane);
		scrollpane.setPreferredSize(new Dimension(650, 300));
		add(scrollpane, BorderLayout.CENTER);
	}

	public static ConsoleProcessPanel createProcessPanel(String[] commandArray, String[] environment, File workingDirectory) throws IOException {
		return new ConsoleProcessPanel(commandArray, environment, workingDirectory);
	}


	public static ConsoleProcessPanel createJavaProcessPanel(String className, String[] arguments, String[] environment, File workingDirectory)
			throws IOException {
		String fileSeparator = System.getProperty("file.separator");
		String javaHome = System.getProperty("java.home");
		String classPath = System.getProperty("java.class.path");

		List<String> commandList = new LinkedList<String>();
		commandList.add(javaHome + fileSeparator + "bin" + fileSeparator + "java");
		commandList.add("-Dfile.encoding=ISO-8859-1");
		commandList.add("-Xms32m");
		commandList.add("-cp");
		commandList.add(classPath);
		commandList.add(className);
		if(arguments != null) commandList.addAll(Arrays.asList(arguments));

		final String[] commandArray = (String[])commandList.toArray(new String[0]);
		return createProcessPanel(commandArray, environment, workingDirectory);
	}

	public void killProcess() {
		synchronized(_runningProcesses) {
			_runningProcesses.remove(_process);
		}
		_process.destroy();
	}

	public void start() {
		_processOutputHatch.start();
		_processErrorHatch.start();
	}

	private static class TextPaneHatch implements Runnable {

		private final JTextPane _textPane;

		private final InputStreamReader _inputReader;

		private static final int MAX_TEXT_LENGTH = 1000000;

		public TextPaneHatch(JTextPane textPane, InputStream inputStream) {
			_textPane = textPane;
			try {
				_inputReader = new InputStreamReader(inputStream, "ISO-8859-1");
			}
			catch(UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new RuntimeException(e); 
			}
		}

		public void run() {
			try {
				while(true) {
					char[] buffer = new char[1000];
					int got = _inputReader.read(buffer);
					if(got < 0) {
						//System.err.println("EOF");
						break;
					}
//                    System.out.println("got = " + got);
					append(new String(buffer, 0, got));
//					String slowAppendResult = _textPane.getText() + new String(buffer);
//					_textPane.setText(slowAppendResult);
				}
			}
			catch(IOException e) {
//                e.printStackTrace();
//                System.err.println("I/O Error");
			}
		}

		private void append(final String string) {
			invokeAndWait(
					new Runnable() {
						public void run() {
							int textLength = _textPane.getDocument().getLength();
							int selectionStart = _textPane.getSelectionStart();
							int selectionEnd = _textPane.getSelectionEnd();
							if(textLength > MAX_TEXT_LENGTH) {
								// Zuviel Text => obere Hälfte löschen
								final int deleteLength = textLength / 2;
								_textPane.select(0, deleteLength);
								_textPane.replaceSelection("(Anfang gelöscht)...");
								final int newLength = _textPane.getDocument().getLength();
								final int deletedLength = textLength - newLength;
								selectionStart -= deletedLength;
								selectionEnd -= deletedLength;
								textLength = newLength;
								// wenn Selektion im gelöschten Bereich lag, dann Selektion auf Ende setzen
								if(selectionStart < 0 || selectionEnd < 0) {
									selectionStart = textLength;
									selectionEnd = textLength;
								}
							}
//	                System.out.println("textLength = " + textLength);
//	                System.out.println("selectionStart = " + selectionStart);
//	                System.out.println("selectionEnd = " + selectionEnd);
							_textPane.select(textLength, textLength);
							_textPane.replaceSelection(string);
							if(textLength != selectionStart || textLength != selectionEnd) {
								_textPane.select(selectionStart, selectionEnd);
							}
						}
					}
			);
		}

		private void invokeAndWait(Runnable runnable) {
			if(SwingUtilities.isEventDispatchThread()) {
				runnable.run();
			}
			else {
				try {
					SwingUtilities.invokeAndWait(runnable);
				}
				catch(InvocationTargetException e) {
					e.printStackTrace();
					throw new RuntimeException(e); 
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
