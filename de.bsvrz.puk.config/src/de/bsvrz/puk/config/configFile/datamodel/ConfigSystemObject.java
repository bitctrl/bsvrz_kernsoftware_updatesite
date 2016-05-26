/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationObjectInfo;
import de.bsvrz.puk.config.configFile.fileaccess.DynamicObjectInfo;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformation;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.puk.config.main.consistencycheck.RelaxedModelChanges;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import static de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications.*;


/**
 * Implementierung des Interfaces {@link SystemObject} auf Seiten der Konfiguration. Die Methoden, die allgemein für das SystemObjekt gelten, wurden in einer
 * {@link AbstractConfigSystemObject abstrakten Klasse} implemenentiert. Alle anderen Methoden, die in Abhängigkeit zur Konfiguration stehen, sind hier
 * implementiert.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class ConfigSystemObject extends AbstractConfigSystemObject implements SystemObject {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Das korrespondierende Objekt für die Dateioperationen dieses SystemObjekts. */
	final SystemObjectInformationInterface _systemObjectInfo;

	/**
	 * Konstruktor für ein SystemObjekt.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses SystemObjekts
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen dieses SystemObjekts
	 */
	public ConfigSystemObject(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea);
		_systemObjectInfo = systemObjectInfo;
		((SystemObjectInformation)_systemObjectInfo).setReference(this);
	}

	public long getId() {
		return _systemObjectInfo.getID();
	}

	public String getPid() {
		final String pid = _systemObjectInfo.getPid();
		if(pid == null) {
			return "";
		}
		else {
			return pid;
		}
	}

	public String getName() {
		final String name = _systemObjectInfo.getName();
		if(name == null) {
			return "";
		}
		else {
			return name;
		}
	}

	public void setName(String name) throws ConfigurationChangeException {
		// prüfen, ob die Berechtigung vorliegt, an diesem Objekt etwas zu ändern
		if(checkChangePermit()) {

			// prüfen, ob der Name geändert werden darf
			if(getType().isNameOfObjectsPermanent() && !isNotActivatedYet()) {
				// Bei schon aktivierten Objekten Namensänderung verbieten, wenn der Name permanent ist
				throw new ConfigurationChangeException(
						"Der Name des Objekts (" + getNameOrPidOrId() + ") darf nicht geändert werden. Er wurde permanent gespeichert."
				);
			}
			else {
				// Wenn name == null -> wird name als "" interpretiert, da anzunehmen ist, dass der Name gelöscht werden soll.
				final String newName;
				if(name == null) {
					newName = "";
				}
				else {
					newName = name;
				}

				if(newName.length() > 255) {
					throw new ConfigurationChangeException(
							"Der Name des Objekts ist zu lang, es sind nur 255 Zeichen erlaubt. Name, der gesetzt werden sollte: " + newName
							+ " Länge des Strings: " + newName.length()
					);
				}

				_systemObjectInfo.setName(newName);

				// Wenn es sich um ein dynamisches Objekt handelt, müssen die Listener für Namensänderungen informiert werden
				final SystemObjectType systemObjectType = getType();
				if(systemObjectType instanceof ConfigDynamicObjectType) {
					// Es handlet sich um ein dynamisches Objekt. Die Listener informieren.
					((ConfigDynamicObjectType)systemObjectType).informNameChangedListener((DynamicObject)this);
				}
			}
		}
		else {
			final String errorMessage = "Der Name des Objekts " + getNameOrPidOrId() + " durfte nicht geändert werden, da keine Berechtigung hierfür vorliegt.";
			_debug.warning(errorMessage);
			throw new ConfigurationChangeException(errorMessage);
		}
	}

	public Data getConfigurationData(final AttributeGroup atg, Aspect asp) {
		return getConfigurationData(atg, asp, getObjectLookupForData());
	}

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zurück.
	 *
	 * @param atg          Attributgruppe des gewünschten Datensatzes.
	 * @param asp          Aspekt des gewünschten Datensatzes.
	 * @param objectLookup Objekt das bei der Deserialisierung zur Auflösung von Objektreferenzen benutzt werden soll.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 */
	public Data getConfigurationData(final AttributeGroup atg, Aspect asp, ObjectLookup objectLookup) {
		if(atg == null || asp == null) return null;
		final AttributeGroupUsage atgUsage = atg.getAttributeGroupUsage(asp);
		if(atgUsage == null) {
			_debug.warning(
					"Zur Attributgruppe '" + atg.getNameOrPidOrId() + "' mit Aspekt '" + asp.getNameOrPidOrId() + "' gibt es keine Attributgruppenverwendung."
			);
			return null;
		}
		byte[] bytes = getConfigurationDataBytes(atgUsage);
		if(bytes == null || bytes.length == 0) {
			return null;
		}
		else {
			try {
				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				final Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);
				final Data data = deserializer.readData(atg, objectLookup);
				if(isNotActivatedYet()){
					removeInvalidReferences(data);
				}
				return data;
			}
			catch(Exception ex) {
				final StringBuffer errorMessage = new StringBuffer();
				errorMessage.append("Der konfigurierende Datensatz für das Objekt ").append(getPidOrNameOrId()).append(" mit ID ").append(getId());
				errorMessage.append(" und der " + "\nAttributgruppenverwendung ").append(atgUsage.getPidOrNameOrId());
				errorMessage.append(" mit ID ").append(atgUsage.getId()).append(" konnten nicht deserialisiert werden");
				_debug.warning(errorMessage.toString(), ex);
				throw new RuntimeException(errorMessage.toString(), ex);
			}
		}
	}

	private void removeInvalidReferences(final Data data) {
		if(!data.isPlain()){
			for(Iterator iterator = data.iterator(); iterator.hasNext();) {
				final Data innerData = (Data)iterator.next();
				removeInvalidReferences(innerData);
			}
		}
		else {
			if(data .getAttributeType() != null && data.getAttributeType() instanceof ReferenceAttributeType) {
				final Data.ReferenceValue value = data.asReferenceValue();
				if(getDataModel().getObject(value.getId()) == null) value.setSystemObject(null);
			}
		}
	}

	public Data getConfigurationData(AttributeGroupUsage atgUsage) {
		return getConfigurationData(atgUsage, getObjectLookupForData());
	}

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zurück.
	 *
	 * @param atgUsage     Attributgruppenverwendung des gewünschten Datensatzes
	 * @param objectLookup Objekt das bei der Deserialisierung zur Auflösung von Objektreferenzen benutzt werden soll.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppenverwendung oder <code>null</code>, wenn das Objekt keinen Datensatz zu der angegebenen
	 *         Attributgruppenverwendung hat.
	 */
	public Data getConfigurationData(AttributeGroupUsage atgUsage, ObjectLookup objectLookup) {
		if(atgUsage == null) return null;
		byte[] bytes = getConfigurationDataBytes(atgUsage);
		if(bytes == null || bytes.length == 0) {
			return null;
		}
		else {
			try {
				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				final Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);
				return deserializer.readData(atgUsage.getAttributeGroup(), objectLookup);
			}
			catch(Exception ex) {
				final StringBuffer errorMessage = new StringBuffer();
				errorMessage.append("Der konfigurierende Datensatz für das Objekt ").append(getPidOrNameOrId()).append(" mit ID ").append(getId());
				errorMessage.append(" und der " + "\nAttributgruppenverwendung ").append(atgUsage.getPidOrNameOrId());
				errorMessage.append(" mit ID ").append(atgUsage.getId()).append(" konnten nicht deserialisiert werden");
				_debug.error(errorMessage.toString(), ex);
				throw new RuntimeException(errorMessage.toString(), ex);
			}
		}
	}

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zurück. Als Aspekt des gewünschten Datensatzes wird "asp.eigenschaften" angenommen.
	 *
	 * @param atg          Attributgruppe des gewünschten Datensatzes.
	 * @param objectLookup Objekt das bei der Deserialisierung zur Auflösung von Objektreferenzen benutzt werden soll.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 */
	public Data getConfigurationData(AttributeGroup atg, ObjectLookup objectLookup) {
		return getConfigurationData(atg, getDataModel().getAspect("asp.eigenschaften"), objectLookup);
	}

	/**
	 * Gibt ein ObjectLookup zurück, das für die Auflösung von Referenzen in Konfigurationsdaten benutzt wird,
	 * Dies ist üblicherweise einfach das ConfigDataModel (siehe {@link #getDataModel()}), für dynamische Objekte muss
	 * aber die Simulationsvariante dieses Objekts zur Auflösung der Referenzen verwendet werden.
	 * @return ObjectLookup
	 */
	protected ObjectLookup getObjectLookupForData() {
		return getDataModel();
	}

	/**
	 * Gibt den konfigurierenden Datensatz als Byte-Array zurück, der am Objekt gespeichert ist. Der Datensatz wird über die ID der Attributgruppenverwendung
	 * identifiziert.
	 *
	 * @param attributeGroupUsage die Attributgruppenverwendung
	 *
	 * @return das Byte-Array des konfigurierenden Datensatzes oder <code>null</code>, falls es keinen konfigurierenden Datensatz für die Attributgruppenverwendung
	 *         gibt
	 */
	public byte[] getConfigurationDataBytes(AttributeGroupUsage attributeGroupUsage) {
		return _systemObjectInfo.getConfigurationDataOptional(attributeGroupUsage.getId());
	}

	/**
	 * Gibt die Version des Serializers zurück, die der Konfigurationsbereich dieses Systemobjekts verwendet.
	 *
	 * @return die Version des Serializers, die der Konfigurationsbereich dieses Systemobjekts verwendet
	 */
	int getSerializerVersion() {
		// Serializer-Version aus der Konfigurationsbereichsdatei auslesen
		return getDataModel().getConfigurationFileManager().getAreaFile(getConfigurationArea().getPid()).getSerializerVersion();
	}

	/**
	 * Vergleicht das Objekt mit einem anderen Objekt. Zwei Objekte sind gleich, wenn sie die gleiche Objekt-Id haben.
	 *
	 * @return <code>true</code>, wenn die Objekte gleich sind, sonst <code>false</code>.
	 */
	public final boolean equals(Object other) {
		if(!(other instanceof SystemObject)) {
			return false;
		}

		// Wenn die Referenzen gleich sind, dann ist auch das Objekt identisch. Damit verhält sich die Methode wie früher
		return other == this || ((SystemObject)other).getId() == getId();
	}

	/**
	 * Bestimmt den Hash-Code dieses Objekts.
	 *
	 * @return Hash-Code dieses Objekts.
	 */
	public final int hashCode() {
		return (int)(getId() ^ (getId() >>> 32));
	}

	public final int originalHashCode() {
		return super.hashCode();
	}

	public void setConfigurationData(AttributeGroup atg, Aspect asp, Data data) throws ConfigurationChangeException {
		final AttributeGroupUsage atgUsage = atg.getAttributeGroupUsage(asp);
		if(atgUsage == null) {
			throw new ConfigurationChangeException(
					"Es existiert keine Attributgruppenverwendung zur Attributgruppe " + atg.getNameOrPidOrId() + " mit dem Aspekt " + asp.getNameOrPidOrId()
					+ "."
			);
		}
		else {
			setConfigurationData(atgUsage, data);
		}
	}

	public void setConfigurationData(AttributeGroupUsage atgUsage, Data data) throws ConfigurationChangeException {
		if(atgUsage == null) throw new IllegalArgumentException("Es wurde keine Attributgruppenverwendung angegeben: " + atgUsage);

		if(checkChangePermit()) {

			if(_systemObjectInfo.isDeleted()){
				// der Datensatz darf nicht geändert werden
				// Diese Prüfung erfolgt ersatzweise noch einmal später beim Setzen in SystemObjectInformationInterface,
				// aber hier ist es einfacher eine gute Fehlermeldung zu erzeugen.
				final String errorMessage = "Der konfigurierende Datensatz an der AttributgruppenVerwendung " + atgUsage.getPid()
						+ " darf nicht geändert oder erstellt werden, da das Objekt " + getPidOrNameOrId() + " nicht mehr gültig ist.";
				_debug.error(errorMessage);
				throw new ConfigurationChangeException(errorMessage);
			}

			// Prüfen, ob die Attributgruppe am Typ definiert ist.
			ConfigSystemObjectType type = getType();
			type.validateAttributeGroup(atgUsage.getAttributeGroup());

			// TPuK1-113 (TPuK1-83) Änderung von konfigurierenden Datensätzen
			// der Datensatz darf geändert werden, wenn die Attributgruppenverwendung "Changeable..." ist.
			// der Datensatz darf nicht gelöscht werden, wenn die Attributgruppenverwendung "...Required..." ist
			if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData || (
					atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData && data != null)) {
				// der konfigurierende Datensatz darf geändert werden
			}
			else if(isNotActivatedYet()) {
				// wenn das Objekt noch nicht aktiviert wurde (sich also noch in Bearbeitung befindet), dann darf es geändert werden
				// dynamische Objekte dürfen nicht mehr geändert werden, da sie sofort aktiv sind!!
			}
			else if("atg.werteBereichsEigenschaften".equals(atgUsage.getAttributeGroup().getPid())
					&& RelaxedModelChanges.getInstance(getDataModel()).allowChangeValueRange(this, this.getConfigurationData(atgUsage), data)){
				// Erlaubt durch Spezialbehandlung unversionierte Datenmodelländerung
			}
			else if("atg.attributEigenschaften".equals(atgUsage.getAttributeGroup().getPid())
					&& RelaxedModelChanges.getInstance(getDataModel()).allowChangeArrayMaxCount(
					this, this.getConfigurationData(atgUsage), data
			)){
				// Erlaubt durch Spezialbehandlung unversionierte Datenmodelländerung
			}
			else {
				// der Datensatz darf nicht geändert werden
				final String errorMessage = "Die Verwendungsmöglichkeit " + atgUsage.getUsage() + " der AttributgruppenVerwendung "
				                            + atgUsage.getNameOrPidOrId() + " erlaubt keine Änderung des Datensatzes.";
				_debug.error(errorMessage);
				throw new ConfigurationChangeException(errorMessage);
			}

			// Referenzen auf ungültige Objekte müssen verhindert werden
			if(data != null) {
				getDataModel().verifyDataReferences(this, data);
			}

			// der Datensatz darf also geändert oder neu angelegt werden!
			createConfigurationData(atgUsage, data);
		}
		else {
			final String errorMessage = "Der konfigurierende Datensatz an der AttributgruppenVerwendung " + atgUsage.getPid()
			                            + " darf weder geändert noch erstellt werden, da hierfür keine Berechtigung vorliegt."
			                            + "\nKonfigurationsverantwortlicher der Konfiguration:Bereich(" + getConfigurationArea().getPid() + ") = "
			                            + getDataModel().getConfigurationAuthorityPid() + " : " + getConfigurationArea().getConfigurationAuthority().getPid();
			_debug.warning(errorMessage);
			throw new ConfigurationChangeException(errorMessage);
		}
	}

	/**
	 * Gibt zurück, ob sich das Objekt noch in Bearbeitung befindet und noch nicht aktiviert wurde.
	 *
	 * @return true wenn noch nicht aktiviert
	 */
	private boolean isNotActivatedYet() {
		return this instanceof ConfigurationObject && ((ConfigurationObject)this).getValidSince() == getConfigurationArea().getModifiableVersion();
	}

	public Collection<AttributeGroupUsage> getUsedAttributeGroupUsages() {
		final Collection<AttributeGroupUsage> atgUsages = new LinkedList<AttributeGroupUsage>();
		final long[] atgUsageIds = _systemObjectInfo.getConfigurationsDataAttributeGroupUsageIds();
		for(long id : atgUsageIds) {
			if(id != CONFIGURATION_SETS && id != CONFIGURATION_ELEMENTS_IN_NON_MUTABLE_SET && id != CONFIGURATION_ELEMENTS_IN_MUTABLE_SET) {
				final SystemObject object = getDataModel().getObject(id);
				if(object != null && object instanceof AttributeGroupUsage) {
					AttributeGroupUsage atgUsage = (AttributeGroupUsage)object;
					atgUsages.add(atgUsage);
				}
			}
		}
		return atgUsages;
	}

	/**
	 * Anhand der Attributgruppenverwendung wird an diesem System-Objekt ein konfigurierender Datensatz gespeichert.
	 *
	 * @param atgUsage die Attributgruppenverwendung
	 * @param data     der konfigurierende Datensatz oder <code>null</code>, falls der Datensatz gelöscht werden soll
	 *
	 * @throws ConfigurationChangeException Falls der Datensatz nicht gespeichert werden konnte.
	 */
	public void createConfigurationData(AttributeGroupUsage atgUsage, Data data) throws ConfigurationChangeException {
		try {
			byte[] bytes = new byte[0];
			if(data != null) {
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(getSerializerVersion(), out);
				serializer.writeData(data);
				bytes = out.toByteArray();
			}
			_systemObjectInfo.setConfigurationData(atgUsage.getId(), bytes);
			getConfigurationArea().setTimeOfLastChanges(ConfigConfigurationArea.KindOfLastChange.ConfigurationData);
			invalidateCache();
		}
		catch(Exception ex) {
			final String errorMessage = "Der Datensatz '" + data + "' am Objekt " + getNameOrPidOrId() + " mit der Attributgruppe "
			                            + atgUsage.getAttributeGroup().getNameOrPidOrId() + " und dem Aspekt " + atgUsage.getAspect().getNameOrPidOrId()
			                            + " konnte nicht erstellt werden";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Löscht zu dieser Attributgruppenverwendung an diesem Objekt den zugehörigen Datensatz.
	 *
	 * @param atgUsage die Attributgruppenverwendung, dessen Datensatz gelöscht werden soll.
	 */
	public void removeConfigurationData(final AttributeGroupUsage atgUsage) {
		_systemObjectInfo.removeConfigurationData(atgUsage.getId());
		invalidateCache();
	}

	/**
	 * Löscht eventuell zwischengespeicherte Daten, die erneuert werden müssen, falls {@link #createConfigurationData(de.bsvrz.dav.daf.main.config.AttributeGroupUsage,
	 * de.bsvrz.dav.daf.main.Data)} oder {@link #setConfigurationData(de.bsvrz.dav.daf.main.config.AttributeGroup, de.bsvrz.dav.daf.main.Data)} aufgerufen wird.
	 * Dies wird automatisch beim Aufruf der genannten Methoden gemacht.
	 */
	void invalidateCache() {
		/** Leer, da diese Klasse nichts zwischenspeichert. Wird aber von {@link ConfigConfigurationObject} und weiteren Klassen überschrieben. **/
	}

	public void invalidate() throws ConfigurationChangeException {
		if(!checkChangePermit()) {
			final String errorMessage = "Das Objekt '" + getNameOrPidOrId() + "' darf nicht ungültig gemacht werden, da keine Berechtigung hierfür vorliegt.";
			_debug.warning(errorMessage);
			throw new ConfigurationChangeException(errorMessage);
		}
	}

	/**
	 * Löscht jedes einzelne Objekt, unabhängig davon, ob es sich um ein freies Objekt handelt oder nicht.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht ungültig gemacht werden konnte.
	 */
	public void simpleInvalidation() throws ConfigurationChangeException {
		if(checkChangePermit()) {
			if(_systemObjectInfo instanceof ConfigurationObjectInfo) {
				((ConfigurationObjectInfo)_systemObjectInfo).invalidate();
			}
			else if(_systemObjectInfo instanceof DynamicObjectInfo) {
				try {
					((DynamicObjectInfo)_systemObjectInfo).setInvalid();
					final ConfigDynamicObject dynamicObject = (ConfigDynamicObject) getDataModel().createSystemObject(_systemObjectInfo);
					dynamicObject.informListeners();	// alle InvalidationListener des Objekts werden benachrichtigt
					// Alle Listener des Typs informieren, dass ein Objekt ungültig geworden ist
					((ConfigDynamicObjectType)dynamicObject.getType()).informInvalidationListener(dynamicObject);
				}
				catch(IllegalStateException ex) {
					final String errorMessage = "Die Methode setInvalid wurde bereits aufgerufen";
					_debug.warning(errorMessage, ex.toString());
					throw new ConfigurationChangeException(errorMessage, ex);
				}
			}
			else {
				throw new IllegalStateException("Das SystemObjekt " + getNameOrPidOrId() + " ist weder ein konfigurierendes noch ein dynamisches Objekt!");
			}
		}
		else {
			final String errorMessage = "Das Objekt " + getNameOrPidOrId() + " durfte nicht ungültig gemacht werden, da keine Berechtigung hierfür vorliegt.";
			_debug.warning(errorMessage);
			throw new ConfigurationChangeException(errorMessage);
		}
	}

	public ConfigSystemObjectType getType() {
		SystemObject object = getDataModel().getObject(_systemObjectInfo.getTypeId());
		if(object instanceof ConfigSystemObjectType) return (ConfigSystemObjectType)object;
		_debug.warning("getType(): Objekt hat einen falschen Typ", object);
		return null;
	}

	/**
	 * Prüft, ob die Konfiguration berechtigt ist an diesem SystemObjekt eine Änderung durchzuführen.
	 *
	 * @return <code>true</code>, falls die Konfiguration die Berechtigung hat Änderungen am Objekt durchzuführen, <br/> <code>false</code>, falls die
	 *         Konfiguration diese Berechtigung nicht hat
	 */
	boolean checkChangePermit() {
		return getDataModel().getConfigurationAuthority() == getConfigurationArea().getConfigurationAuthority() || getDataModel().getConfigurationAuthorityPid().equals(getConfigurationArea().getConfigurationAuthority().getPid());
	}
}
