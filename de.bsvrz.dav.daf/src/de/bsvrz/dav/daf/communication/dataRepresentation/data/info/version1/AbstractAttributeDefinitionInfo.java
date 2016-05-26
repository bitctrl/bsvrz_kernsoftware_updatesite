/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeDefinitionInfo;
import de.bsvrz.dav.daf.main.config.AttributeSet;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;

import java.util.*;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class AbstractAttributeDefinitionInfo implements AttributeDefinitionInfo {
	private static final Map<AttributeSet, AttributeDefinitionInfo> _AttributSet2InfoMap = new IdentityHashMap<AttributeSet, AttributeDefinitionInfo>();
	private static final Map<AttributeType, AttributeDefinitionInfo> _AttributType2DefinitionInfoMap = new IdentityHashMap<AttributeType, AttributeDefinitionInfo>();

	public static void forgetDataModel(DataModel dataModel) {
		synchronized(_AttributSet2InfoMap) {
			List<AttributeSet> attributeSets = new ArrayList<AttributeSet>(_AttributSet2InfoMap.keySet());
			for(AttributeSet attributeSet : attributeSets) {
				if(attributeSet.getDataModel() == dataModel) {
					_AttributSet2InfoMap.remove(attributeSet);
				}
			}
		}
		synchronized(_AttributType2DefinitionInfoMap) {
			List<AttributeType> attributeTypes = new ArrayList<AttributeType>(_AttributType2DefinitionInfoMap.keySet());
			for(AttributeType attributeType : attributeTypes) {
				if(attributeType.getDataModel() == dataModel) {
					_AttributType2DefinitionInfoMap.remove(attributeType);
				}
			}
		}
	}

	public static AttributeDefinitionInfo forAttributSet(final AttributeSet attributeSet) {
		synchronized(_AttributSet2InfoMap) {
			AttributeDefinitionInfo definitionInfo = _AttributSet2InfoMap.get(attributeSet);
			if(definitionInfo == null) {
				definitionInfo = new AttributeSetDefinitionInfo(attributeSet);
				_AttributSet2InfoMap.put(attributeSet, definitionInfo);
			}
			return definitionInfo;
		}
	}

	public static AttributeDefinitionInfo forAttributeType(AttributeType attributeType) {
		synchronized(_AttributType2DefinitionInfoMap) {
			AttributeDefinitionInfo definitionInfo;
			definitionInfo = _AttributType2DefinitionInfoMap.get(attributeType);
			if(definitionInfo == null) {
				definitionInfo = createDefinition(attributeType);
				_AttributType2DefinitionInfoMap.put(attributeType, definitionInfo);
			}
			return definitionInfo;
		}
	}

	private static AttributeDefinitionInfo createDefinition(AttributeType attributeType) {
		try {
			if(attributeType instanceof IntegerAttributeType) {
				IntegerAttributeType att = (IntegerAttributeType)attributeType;
				switch(att.getByteCount()) {
				case 1:
					return new ByteDefinitionInfo(att);
				case 2:
					return new ShortDefinitionInfo(att);
				case 4:
					return new IntDefinitionInfo(att);
				case 8:
					return new LongDefinitionInfo(att);
				}
			}
			else if(attributeType instanceof TimeAttributeType) {
				TimeAttributeType att = (TimeAttributeType)attributeType;
				if(att.getAccuracy() == TimeAttributeType.MILLISECONDS) {
					if(att.isRelative()) {
						return new RelativeMillisecondsDefinitionInfo(att);
			        }
					else {
						return new AbsoluteMillisecondsDefinitionInfo(att);
					}
				}
				else if(att.getAccuracy() == TimeAttributeType.SECONDS) {
					if(att.isRelative()) {
						return new RelativeSecondsDefinitionInfo(att);
			        }
					else {
						return new AbsoluteSecondsDefinitionInfo(att);
					}
				}
			}
			else if(attributeType instanceof StringAttributeType) {
				return new StringDefinitionInfo((StringAttributeType)attributeType);
			}
			else if(attributeType instanceof ReferenceAttributeType) {
				return new ReferenceDefinitionInfo((ReferenceAttributeType)attributeType);
			}
			else if(attributeType instanceof DoubleAttributeType) {
				DoubleAttributeType att = (DoubleAttributeType)attributeType;
				if(att.getAccuracy() == DoubleAttributeType.FLOAT) {
					return new FloatDefinitionInfo(att);
				}
				else if(att.getAccuracy() == DoubleAttributeType.DOUBLE) {
					return new DoubleDefinitionInfo(att);
				}
			}
			throw new IllegalArgumentException("Attributtyp wird nicht unterstützt: " + attributeType);
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public boolean isTimeAttribute() {
		return false;
	}

	public long getSeconds(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zeit interpretiert werden.");
	}

	public long getMillis(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zeit interpretiert werden.");
	}

	public boolean isReferenceAttribute() {
		return false;
	}

	public long getId(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Referenz interpretiert werden.");
	}

	public SystemObject getSystemObject(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Referenz interpretiert werden.");
	}

	public boolean isNumberAttribute() {
		return false;
	}

	public boolean isScalableNumberAttribute() {
		return false;
	}

	public boolean isNumber(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public boolean isState(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public byte unscaledByteValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public short unscaledShortValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public int unscaledIntValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public long unscaledLongValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public float unscaledFloatValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public double unscaledDoubleValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public String getUnscaledValueText(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public String getUnscaledSuffixText(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public IntegerValueState getState(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public byte byteValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public short shortValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public int intValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public long longValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public float floatValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

	public double doubleValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Ein Attribut vom Typ " + getAttributeType().getPid() + " kann nicht als Zahl interpretiert werden.");
	}

}
