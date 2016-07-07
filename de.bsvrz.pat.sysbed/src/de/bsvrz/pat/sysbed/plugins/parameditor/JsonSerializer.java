/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.plugins.parameditor;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.kappich.sys.funclib.json.Json;
import de.kappich.sys.funclib.json.JsonObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class JsonSerializer {
	
	private static JsonObject serializeListData(Data data){
		JsonObject jsonObject = Json.getInstance().newObject();
		for(Data subData : data) {
			jsonObject.put(subData.getName(), serializeData(subData));
		}
		return jsonObject;
	}
	
	public static void deserializeData(Object object, Data result) {
		if(object == null) return;
		if(object instanceof JsonObject) {
			JsonObject jsonObject = (JsonObject) object;
			deserializeListData(jsonObject, result);
		}
		else if(object instanceof Collection) {
			Collection collection = (Collection) object;
			Data.Array array = result.asArray();
			array.setLength(collection.size());
			int i = 0;
			for(Object o : collection) {
				deserializeData(o, array.getItem(i++));
			}
		}
		else{
			deserializePlainData(object, result);
		}
	}

	private static void deserializePlainData(final Object object, final Data result) {
		try {
			if(object instanceof BigDecimal) {
				BigDecimal bigDecimal = (BigDecimal) object;
				bigDecimal.divide(new BigDecimal(((IntegerAttributeType) result.getAttributeType()).getRange().getConversionFactor()));
				result.asUnscaledValue().set(bigDecimal.longValueExact());
			}
			else {
				result.asTextValue().setText(object.toString());
			}
		}
		catch(Exception e){
			throw new IllegalArgumentException("Fehler beim Setzen von " + result.getName() + " auf den Wert \"" + String.valueOf(object) + "\": " + e.getMessage(), e);
		}
	}

	private static void deserializeListData(final JsonObject object, final Data result) {
		for(Map.Entry<String, Object> entry : object.entrySet()) {
			deserializeData(entry.getValue(), result.getItem(entry.getKey()));
		}
	}

	public static Object serializeData(final Data data) {
		if(data == null) return null;
		if(data.isList()){
			return serializeListData(data);
		}
		else if(data.isArray()){
			Data.Array array = data.asArray();
			final List<Object> objects = new ArrayList<Object>(array.getLength());
			for(Data d : data) {
				objects.add(serializeData(d));
			}
			return objects;
		}
		else{
			assert data.isPlain();
			return serializePlainData(data);
		}
	}

	private static Object serializePlainData(final Data subData) {
		AttributeType attributeType = subData.getAttributeType();
		if(attributeType instanceof IntegerAttributeType) {
			Data.NumberValue value = subData.asUnscaledValue();
			if(value.isState()){
				return value.getState().getName();
			}
			else {
				BigDecimal bigDecimal = new BigDecimal(value.longValue());
				return bigDecimal.multiply(new BigDecimal(((IntegerAttributeType) attributeType).getRange().getConversionFactor()));
			}
		}
		else if(attributeType instanceof DoubleAttributeType) {
			return subData.asScaledValue().doubleValue();
		}
		else {
			return subData.asTextValue().getText();
		}
	}

}
