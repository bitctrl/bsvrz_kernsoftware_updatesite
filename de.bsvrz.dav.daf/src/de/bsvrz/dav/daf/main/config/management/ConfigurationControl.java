/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.config.management;

import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;

import java.io.File;
import java.util.*;

/**
 * Dieses Interface enthält alle nötigen Zugriffsmethoden, um die Konfigurationsdaten im Sinne des Konfigurationseditors zu manipulieren. Dazu gehört auch der
 * Import und der Export von Versorgungsdateien, sowie die Konsistenzprüfung des Datenmodells.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ConfigurationControl {

	/**
	 * Alle Konfigurationsbereichseinträge in der Verwaltungsdatei werden zurückgegeben. Hierbei ist es unerheblich, ob der Bereich bereits aktiviert wurde oder
	 * noch zu aktivieren ist.
	 *
	 * @return Eine Map, deren Schlüssel die Pid des Bereichs und der Wert das Objekt des Konfigurationsbereichs ist.
	 */
	public Map<String, ConfigurationArea> getAllConfigurationAreas();

	/**
	 * Die Implementierung dieser Methode legt einen neuen Konfigurationsbereich in der lokalen Konfiguration an. Hierfür wird eine entsprechende
	 * Konfigurationsdatei angelegt, die initial das Objekt des Konfigurationsbereichs enthält.
	 * <p>
	 * Zusätzlich müssen die konfigurierenden Datensätze für den Konfigurationsverantwortlichen und für die Versionsnummern (aktivierbare und übernehmbare Version)
	 * angelegt werden.
	 * <p>
	 * Ein Eintrag in der Verwaltungsdatei wird ebenfalls eingetragen. Er enthält die Pid des Konfigurationsbereichs und die Pfadangabe, wo sich die
	 * Konfigurationsdatei nach Erstellung befindet.
	 *
	 * @param name         Name des neuen Konfigurationsbereichs
	 * @param pid          eindeutige Pid des neuen Konfigurationsbereichs
	 * @param authorityPid die Pid des Konfigurationsverantwortlichen des neuen Konfigurationsbereichs
	 *
	 * @return Das Objekt des neuen Konfigurationsbereichs.
	 *
	 * @throws ConfigurationChangeException Falls kein neuer Konfigurationsbereich angelegt werden konnte.
	 */
	public ConfigurationArea createConfigurationArea(String name, String pid, String authorityPid) throws ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration alle Konfigurationsbereiche einer Konsistenzprüfung zu unterziehen (siehe TPuK1-138). Diese
	 * Methode kann unabhängig von der Aktivierung {@link #activateConfigurationAreas} oder Freigabe {@link #releaseConfigurationAreasForTransfer} aufgerufen
	 * werden.
	 *
	 * @param configurationAreas Definiert alle Konfigurationsbereiche, die einer Konsistenzprüfung unterzogen werden sollen. Der Bereich wird über seine Pid
	 *                           identifiziert, zusätzlich wird die Version angegeben in der der Konfigurationsbereich geprüft werden soll. Alle Bereiche der
	 *                           Konfiguration, die nicht angegeben werden, werden in die Prüfung einbezogen und zwar mit ihrer aktuellen Version und müssen somit
	 *                           nicht explizit angegeben werden.
	 *
	 * @return Ergebnis der Konsistenzprüfung
	 */
	public ConsistencyCheckResultInterface checkConsistency(Collection<ConfigAreaAndVersion> configurationAreas);

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angebenen Konfigurationsbereiche zu aktivieren (siehe TPuK1-142). Vor der Aktivierung
	 * wird automatisch eine Konsistenzprüfung durchgeführt. Die Bereiche dürfen nur aktiviert werden, wenn weder lokale noch Interferenzfehler aufgetreten sind.
	 * <p>
	 * Verlief die Konsistenzprüfung positiv (weder lokale noch Interferenzfehler), wird beim nächsten Neustart der Konfiguration jeder angegebene
	 * Konfigurationsbereich mit der angegebenen Version gestartet.
	 * <p>
	 * Verlief die Konsistenzprüfung negativ, wird keiner der angegebenen Konfigurationsbereiche aktiviert.
	 * <p>
	 * Die Implementierung muss dabei berücksichtigen, dass nur Konfigurationsbereiche aktiviert werden dürfen, für die die Konfiguration auch verantwortlich
	 * (Konfiguration ist Konfigurationsverantwortlicher des Bereichs) ist oder aber Konfigurationsbereiche die zur Aktivierung durch andere
	 * Konfigurationsverantwortliche freigegeben sind.
	 * <p>
	 * Die Version, in der ein Konfigurationsbereich aktiviert werden soll, muss größer sein als die derzeit aktuelle Version in der der Konfigurationsbereich
	 * läuft.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version aktiviert werden sollen
	 *
	 * @return Ergebnis der Konsistenzprüfung. Die Bereiche werden nur aktiviert, wenn es weder zu einem lokalen noch zu einem Interferenzfehler gekommen ist.
	 *
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht aktiviert werden konnte. <br>Folgende Gründe können die Ursache sein:
	 *                                      <br> Die Konfiguration wollte einen Konfigurationsbereich in einer Version aktivieren, die noch nicht zur Aktivierung
	 *                                      freigegeben war und für den sie nicht der Konfigurationsverantwortliche ist.<br> Ein Konfigurationsbereich läuft in
	 *                                      einer höheren Version, als die Version in der er aktiviert werden soll.
	 */
	public ConsistencyCheckResultInterface activateConfigurationAreas(Collection<ConfigAreaAndVersion> configurationAreas) throws ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angebenen Konfigurationsbereiche in den Status "Freigabe zur Übernahme" zu überführen
	 * (siehe TPuK1-143). Bevor die angegebenen Bereiche freigegeben werden, wird automatisch eine Konsistenzprüfung durchgeführt.
	 * <p>
	 * Verlief die Konsistenzprüfung positiv(keine lokalen Fehler), werden die angegebenen Konfigurationsbereiche mit der angegebenen Version freigegeben.
	 * <p>
	 * Verlief die Konsistenzprüfung negativ, wird keiner der angegebenen Konfigurationsbereiche freigegeben.
	 * <p>
	 * Die Implementierung muss prüfen ob die Version, in der der Bereich zur Übernahme freigegeben wird, größer als die "aktuelle" Version, die zur Übernahme
	 * freigegeben wurde, ist.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version freigegeben werden sollen
	 *
	 * @return Ergebnis der Konsistenzprüfung. Die Konfigurationsbereiche werden nur freigegeben, wenn kein lokaler Fehler aufgetreten ist.
	 *
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht zur Übernahme freigegeben werden konnte. <br>Folgende Gründe können
	 *                                      die Ursache sein:<br> Die Konfiguration war nicht der Konfigurationsverantwortliche für alle angegebenen Bereiche.<br>
	 *                                      Die aktuelle Version, in der ein Bereich bereits zur Übernahme freigegeben wurde, ist größer als die Version, in der
	 *                                      der Bereich freigegeben werden soll.<br>Der Datensatz, der die Versionsnummer speichert konnte nicht verändert oder
	 *                                      geschrieben werden.
	 */
	public ConsistencyCheckResultInterface releaseConfigurationAreasForTransfer(Collection<ConfigAreaAndVersion> configurationAreas)
			throws ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angegebenen Konfigurationsbereiche in den Status "Freigabe zur Aktivierung" zu
	 * überführen. Dadurch können andere Konfigurationen die Konfigurationsbereiche übernehmen und diese lokal aktivieren. Jede Konfiguration kann nur die Bereiche
	 * zur Aktivierung freigeben, für die sie auch der Konfigurationsverantwortliche ist.
	 * <p>
	 * Es findet keine Konsistenzprüfung statt, da ein Konfigurationsbereich nur dann für andere zur Aktivierung freigegeben werden darf, wenn er bereits lokal
	 * aktiviert {@link #activateConfigurationAreas} wurde.
	 * <p>
	 * Es werden entweder alle angegebenen Konfigurationsbereiche in der jeweils geforderten Version aktiviert oder keiner.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version für andere Konfigurationen freigegeben werden sollen
	 *
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht zur Aktivierung freigegeben werden konnte. <br>Folgende Gründe können
	 *                                      die Ursache sein:<br>Die Konfiguration, die die Konfigurationsbereiche freigeben soll, ist nicht der
	 *                                      Konfigurationsverantwortliche für den/die Bereich/e.<br>Ein Bereich soll in einer Version freigegeben werden, der noch
	 *                                      nicht durch den Konfigurationsverantwortlichen der Konfiguration lokal aktiviert wurde {@link
	 *                                      #activateConfigurationAreas}.<br>Ein Bereich soll in einer Version zur Aktivierung freigegeben werden, der bereits in
	 *                                      einer höheren Version zur Aktivierung freigegeben wurde.<br>Der Datensatz, der die Versionsnummer speichert konnte
	 *                                      nicht verändert oder geschrieben werden.
	 */
	public void releaseConfigurationAreasForActivation(Collection<ConfigAreaAndVersion> configurationAreas) throws ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode importiert die Versorgungsdateien der angegebenen Pids vom angegebenen Verzeichnis in die bestehende Konfiguration.
	 * Dadurch können neue Konfigurationsbereiche angelegt oder bestehende Bereiche verändert werden.
	 * <p>
	 * Versorgungsdateien können auch wieder {@link #exportConfigurationAreas exportiert} werden.
	 *
	 * @param importPath            Verzeichnis der Versorgungsdateien
	 * @param configurationAreaPids Pids der zu importierenden Konfigurationsbereiche
	 *
	 * @throws ConfigurationChangeException Falls während des Imports Fehler auftreten. Nach Korrektur des Fehlers kann der Import wiederholt werden.
	 */
	public void importConfigurationAreas(File importPath, Collection<String> configurationAreaPids) throws ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode exportiert aus der bestehenden Konfiguration die Versorgungsdateien zu den angegebenen Pids in das angegebene
	 * Verzeichnis. Änderungen können an den Versorgungsdateien vorgenommen und diese wieder {@link #importConfigurationAreas importiert} werden.
	 *
	 * @param exportPath            Verzeichnis der Versorgungsdateien
	 * @param configurationAreaPids Pids der zu exportierenden Konfigurationsbereiche
	 *
	 * @throws ConfigurationTaskException Die angegebenen Bereiche konnte nicht exportiert werden. Dies kann mehrere Gründe haben (zu einer Pid wurde kein
	 *                                    Konfigurationsbereich gefunden, eine Versorgungsdatei konnte nicht geschrieben werden, usw.).
	 */
	public void exportConfigurationAreas(File exportPath, Collection<String> configurationAreaPids) throws ConfigurationTaskException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angegebenen Bereiche in den Zustand "Darf durch andere aktiviert werden, obwohl der KV
	 * es selbst nicht aktiviert hat" zu bringen.
	 * <p>
	 * Dadurch können andere Konfigurationen die Bereiche übernehmen und aktivieren.
	 * <p>
	 * Die Konsistenzprüfung darf bei dieser Art der Aktivierung Interferenzfehler zulassen, lokale Fehler sind nicht erlaubt. Alle Bereiche, die nicht im Parameter
	 * configurationAreas angegeben sind, werden in der "zur Aktivierung freigegeben"-Version geprüft. Das betrifft ebenfalls die Bereich, für die der KV
	 * verantwortlich ist.
	 * <p>
	 * Es werden entweder alle angegebenen Bereiche aktiviert oder keiner (falls es zu einem Fehler kommt).
	 * <p>
	 * Eine Beispielanwendung dafür wäre: Bereich A stellt ein Objekt zur Verfügung, kann es aber nicht aktivieren, weil der Typ in Bereich B definiert wird.
	 * Bereich B kann nicht aktivieren, weil das Objekt aus Bereich A referenziert wird.
	 * <p>
	 * Bereich A oder Bereich B können dann über diese Methode das Objekt oder den Typ trotzdem für den anderen Bereich zur Aktivierung freigeben.
	 *
	 * @param configurationAreas Bereiche, die für andere zur Aktivierung freigegeben sind aber ihrerseits nicht durch den KV aktiviert wurden.
	 *
	 * @return Ergebnis der Konsistenzprüfung. Die Konfigurationsbereiche werden nur freigegeben, wenn kein Interferenzfehler aufgetreten ist.
	 *
	 * @throws ConfigurationChangeException Fehler beim Versuch die Bereiche für andere freizugeben. Es wurde kein Bereich freigegeben.
	 */
	public ConsistencyCheckResultInterface releaseConfigurationAreasForActivationWithoutCAActivation(Collection<ConfigAreaAndVersion> configurationAreas)
			throws ConfigurationChangeException;
}
