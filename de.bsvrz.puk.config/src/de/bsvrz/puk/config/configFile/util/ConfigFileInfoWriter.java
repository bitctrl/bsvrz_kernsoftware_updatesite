/*
 * Copyright 2013 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Klasse, die Informationen zu Konfigurationsbereichen auf Konsole ausgibt und formatiert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11592 $
 */
public class ConfigFileInfoWriter {
	private final boolean _displayShortInfo;
	private final boolean _displayDescription;
	private final boolean _displayTransferVersion;
	private final boolean _displayActivatableVersion;
	private final boolean _displayDependencies;
	private final boolean _displayChanges;
	private final int _displayWidth;
	private boolean _verboseMode = false;

	public ConfigFileInfoWriter(final boolean displayShortInfo, final boolean displayDescription, final boolean displayTransferVersion, final boolean displayActivatableVersion, final boolean displayDependencies, final boolean displayChanges, final int terminalWidth) {
		_displayShortInfo = displayShortInfo;
		_displayDescription = displayDescription;
		_displayTransferVersion = displayTransferVersion;
		_displayActivatableVersion = displayActivatableVersion;
		_displayDependencies = displayDependencies;
		_displayChanges = displayChanges;
		_displayWidth = terminalWidth - 28;
	}

	public void writeInfo(final ConfigFileInfo configFileInfo) throws IOException {
		printHeader("Konfigurationsbereich: ", configFileInfo);
		if(_displayShortInfo) printData("Kurzinfo", configFileInfo.getShortInfo());
		if(_displayDescription) printData("Beschreibung", configFileInfo.getDescription());
		if(_displayTransferVersion) printData("Übernahme-Version", configFileInfo.getTransferableVersion());
		if(_displayActivatableVersion) printData("Aktivierbare Version", configFileInfo.getActivatableVersion());
		if(_displayDependencies) printData("Abhängigkeiten", configFileInfo.getDependencies());
		if(_displayChanges) printData("Änderungsvermerke", configFileInfo.getChanges());
		System.out.println();
		System.out.println();
	}


	public void writeDependencySummary(final List<ConfigAreaDependency> required, final List<ConfigAreaDependency> optional) {
		printHeader("Zusammenfassung", "");
		if(required.size() == 0){
			printData("Notwendige Abhängigkeiten", "Keine unerfüllten notwendigen Abhängigkeiten.");
		}
		else{
			printData("Notwendige Abhängigkeiten", required);
		}
		if(optional.size() == 0){
			printData("Optionale Abhängigkeiten", "Keine unerfüllten optionalen Abhängigkeiten.");
		}
		else{
			printData("Optionale Abhängigkeiten", optional);
		}
	}

	private void printData(final String info, final Object data) {
		System.out.printf("%-26s| ", info + ":");
		print(data);
	}

	private void print(final Object data) {
		if(data instanceof Collection) {
			Collection<?> collection = (Collection<?>) data;
			StringBuilder stringBuilder = new StringBuilder();
			for(Object o : collection) {
				stringBuilder.append(toString(o));
				stringBuilder.append("\n");
			}
			print(stringBuilder.toString().trim());
			return;
		}
		if(data == null){
			printText("[Nicht verfügbar]");
		}
		else {
			printText(data.toString());
		}
	}

	protected String toString(final Object o) {
		if(o instanceof ChangeHistoryItem) {
			ChangeHistoryItem changeHistoryItem = (ChangeHistoryItem) o;

			if(isInVerboseMode()){
				return String.format(
						"%d: %-40s %3$td.%3$tm.%3$tY\n%4$s\n%5$s", changeHistoryItem.getVersion(), changeHistoryItem.getAuthor(),
						changeHistoryItem.getTimeStamp(), changeHistoryItem.getReason(), changeHistoryItem.getText()
				).trim() + "\n";
			}

			return String.format("%d: %s\n%s", changeHistoryItem.getVersion(),
					changeHistoryItem.getReason(), changeHistoryItem.getText()).trim() + "\n";
		}
		if(o instanceof ConfigAreaDependency) {
			ConfigAreaDependency configAreaDependency = (ConfigAreaDependency) o;

			if(isInVerboseMode()){
				return String.format(
						"%-3d %-25s (%s, seit Version %d)", configAreaDependency.getNeededVersion(), configAreaDependency.getDependentPid(),
						configAreaDependency.getType(), configAreaDependency.getDependentVersion()
				);
			}

			if(configAreaDependency.getType().equals("optional")){
				return String.format("%-3d %-25s (optional)", configAreaDependency.getNeededVersion(), configAreaDependency.getDependentPid());
			}

			return String.format("%-3d %-25s", configAreaDependency.getNeededVersion(), configAreaDependency.getDependentPid());
		}
		return o.toString();
	}

	private void printText(final String text) {
		String[] split = breakLines(text.split("\n"));
		for(int i = 0; i < split.length; i++) {
			final String s = split[i];
			if(i != 0){
				System.out.printf("%-26s| %s\n", "", s);
			}
			else {
				System.out.println(s);
			}
		}
	}

	private String[] breakLines(final String[] split) {
		final List<String> strings = new ArrayList<String>(split.length);
		for(String s : split) {
			strings.addAll(breakLines(s));
		}
		return strings.toArray(new String[strings.size()]);
	}

	private List<String> breakLines(final String s) {
		final List<String> result = new ArrayList<String>();
		StringBuilder current = new StringBuilder(_displayWidth);
		for(String tmp : s.split(" ")) {
			if(current.length() == 0){
				current.append(tmp);
			}
			else if(current.length() + tmp.length() + 1 < _displayWidth){
				current.append(" ").append(tmp);
			}
			else {
				result.add(current.toString());
				current.setLength(0);
				current.append(tmp);
			}
		}
		result.add(current.toString());
		return result;
	}

	private void printHeader(final String label, final Object header) {
		System.out.printf("%-26s| %s\n", label, header);
		for(int i =0; i < 28 + _displayWidth; i++){
			System.out.print("=");
		}
		System.out.println();
	}

	public void setVerboseMode(final boolean verboseMode) {
		_verboseMode = verboseMode;
	}

	public boolean isInVerboseMode() {
		return _verboseMode;
	}
}
