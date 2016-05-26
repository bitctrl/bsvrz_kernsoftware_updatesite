/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.application.
 * 
 * de.bsvrz.sys.funclib.application is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.application; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.application;

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;

/**
 * Beispielapplikation, die die Verwendung des StandardApplicationRunner-Mechanismus demonstriert.
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SampleApp implements StandardApplication {
	private static Debug _debug;
	private String _objectPid;
	private String _atgPid;
	private String _aspectPid;

	public static void main(String[] args) {
		StandardApplicationRunner.run(new SampleApp(), args);
	}
	public void parseArguments(ArgumentList argumentList) throws Exception {
		_debug = Debug.getLogger();
		_debug.fine("argumentList = " + argumentList);
		_objectPid = argumentList.fetchArgument("-objekt=").asNonEmptyString();
		_atgPid = argumentList.fetchArgument("-atg=").asNonEmptyString();
		_aspectPid = argumentList.fetchArgument("-asp=").asNonEmptyString();
	}

	public void initialize(ClientDavInterface connection) throws Exception {
		DataModel configuration = connection.getDataModel();

		SystemObject object = configuration.getObject(_objectPid);
		_debug.info("Objekt: " + object);

		AttributeGroup atg = configuration.getAttributeGroup(_atgPid);
		_debug.info("Attributgruppe: " + atg);

		Aspect aspect = configuration.getAspect(_aspectPid);
		_debug.info("Aspekt: " + aspect);

		DataDescription dataDescription= new DataDescription(atg, aspect);

		ClientReceiverInterface receiver= new Receiver();
		connection.subscribeReceiver(receiver, object, dataDescription, ReceiveOptions.normal(), ReceiverRole.receiver());
	}


	private static class Receiver implements ClientReceiverInterface {
		public void update(ResultData results[]) {
			for (ResultData result : results) {
				_debug.info(result.toString());
				System.out.println("result = " + result);
			}
		}

	}


}
