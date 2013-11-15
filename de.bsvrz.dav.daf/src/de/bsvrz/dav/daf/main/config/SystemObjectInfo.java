/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004, 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.config;

import de.bsvrz.sys.funclib.debug.Debug;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.regex.Pattern;


/**
 * Klasse zum Zugriff auf beschreibende Informationen von Systemobjekten.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @author Achim Wullenkord (aw), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5084 $ / $Date: 2007-09-03 10:42:50 +0200 (Mon, 03 Sep 2007) $ / ($Author: rs $)
 * @see puk.configuration
 */
public class SystemObjectInfo {
	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Kurzinfo im HTML-Format
	 */
	private String _shortInfoAsHTML = null;

	/**
	 * Beschreibung im HTML-Format
	 */
	private String _descriptionAsHTML = null;

	/**
	 * Kurzinfo im XML-Format der Versorgungsdateien
	 */
	private final String _shortInfoAsXML;

	/**
	 * Beschreibung im XML-Format der Versorgungsdateien
	 */
	private final String _descriptionAsXML;

	/**
	 * Objekt f�r die synchronisierte Umwandlung der Kurzinfo/Beschreibung vom XML-Format ins HTML-Format
	 */
	private final Object _lock = new Object();

	/**
	 * der undefinierte Zustand dieses Info-Objekts
	 */
	public final static SystemObjectInfo UNDEFINED = new SystemObjectInfo("", "");


	/**
	 * Initialisiert ein neues Info-Objekt mit beschreibenden Informationen eines Systemobjekts.
	 *
	 * @param shortInfo   Kurze beschreibende Information des jeweiligen Systemobjekts.
	 * @param description Ausf�hrliche Beschreibung des jeweiligen Systemobjekts.
	 */
	public SystemObjectInfo(String shortInfo, String description) {
		// die XML-Version wird nur f�r die Versorgungsdateien im Export ben�tigt (also nur f�r interne Zwecke)
		_shortInfoAsXML = shortInfo;
		_descriptionAsXML = description;
	}

	/**
	 * Lieferte eine kurze beschreibende Information des jeweiligen Systemobjekts zur�ck, die <code>HTML</code> konform ist.
	 *
	 * @return <code>HTML</code> konformer Text, der die Kurzbeschreibung enth�lt. Ist keine Beschreibung vorhanden, wird ein leerer String <code>""</code>
	 *         zur�ckgegeben.
	 */
	public String getShortInfo() {
		if (_shortInfoAsHTML == null) {
			synchronized (_lock) {
				if (_shortInfoAsXML.startsWith("<html>") || "".equals(_shortInfoAsXML)) {
					// Kurzinfo ist bereits in HTML umgewandelt, braucht also nicht mehr
					_shortInfoAsHTML = _shortInfoAsXML;
				} else {
					// Kurzinfo muss umgewandelt werden
					_shortInfoAsHTML = getHTMLText("<kurzinfo>" + _shortInfoAsXML + "</kurzinfo>");
				}
			}
		}
		return _shortInfoAsHTML;
	}

	/**
	 * Lieferte eine ausf�hrliche Beschreibung des jeweiligen Systemobjekts zur�ck, die <code>HTML</code> konform ist.
	 *
	 * @return <code>HTML</code> konformer Text, der die ausf�hrliche Beschreibung enth�lt. Ist keine Beschreibung vorhanden, wird ein leerer String
	 *         <code>""</code> zur�ckgegeben.
	 */
	public String getDescription() {
		if (_descriptionAsHTML == null) {
			synchronized (_lock) {
				if (_descriptionAsXML.startsWith("<html>") || "".equals(_descriptionAsXML)) {
					// Beschreibung ist bereits in HTML umgewandelt, braucht also nicht mehr umgewandelt werden
					_descriptionAsHTML = _descriptionAsXML;
				} else {
					// Beschreibung muss umgewandelt werden
					_descriptionAsHTML = getHTMLText("<beschreibung>" + _descriptionAsXML + "</beschreibung>");
				}
			}
		}
		return _descriptionAsHTML;
	}

	/**
	 * Liefert die Kurzinformation des jeweiligen Systemobjekts zur�ck, wie sie in der Versorgungsdatei steht. Die Formatierung entspricht der K2S.DTD.
	 *
	 * @return Originaltext der Kurzinformation des jeweiligen Systemobjekts aus der Versorgungsdatei
	 */
	public String getShortInfoAsXML() {
		return _shortInfoAsXML;
	}

	/**
	 * Liefert die ausf�hrliche Beschreibung des jeweiligen Systemobjekts zur�ck, wie sie in der Versorgungsdatei steht. Die Formatierung entspricht der K2S.DTD.
	 *
	 * @return Originaltext der Beschreibung des jeweiligen Systemobjekts aus der Versorgungsdatei
	 */
	public String getDescriptionAsXML() {
		return _descriptionAsXML;
	}

	/**
	 * String-Repr�sentation dieses Objekts. Enth�lt die Kurzinformation und die ausf�hrliche Beschreibung.
	 *
	 * @return String-Repr�sentation dieses Objekts
	 *
	 * @see #getShortInfo
	 * @see #getDescription
	 */
	public String toString() {
		return new StringBuilder().append('{').append(getShortInfo()).append(',').append(getDescription()).append('}').toString();
	}

	/**
	 * Wandelt Text im XML-Format in einen Text im HTML-Format um.
	 *
	 * @param text umzuwandelnder Text im XML-Format
	 * @return Text im HTML-Format
	 */
	private String getHTMLText(String text) {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringReader reader = new StringReader(text);
			InputSource source = new InputSource(reader);
			Document xmlDocument = builder.parse(source);
			Element element = xmlDocument.getDocumentElement();
			NodeList childs = element.getChildNodes();
			if (childs.getLength() <= 0) return "";	// Tag zwar da, aber ohne Inhalt
			if (childs.getLength() == 1 && (childs.item(0) instanceof Text)) {
				// enth�lt der Tag nur Text, dann ...
				return ((Text) childs.item(0)).getNodeValue();
			} else {
				// Tags werden durch HTML konforme Tags ersetzt
				StringBuilder resultText = new StringBuilder();
				resultText.append("<html><body>");
				transferXml(childs, resultText);
				resultText.append("</body></html>");
				return resultText.toString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return text;
		}
	}

	/**
	 * Ersetzt einzelne XML-Tags durch passende HTML-Tags
	 *
	 * @param node	   XML-Tag
	 * @param resultText umgewandelter Text in HTML-Format
	 */
	private void transferXml(Node node, StringBuilder resultText) {
		if (node instanceof Element) {
			Element element = (Element) node;
			String tag = element.getTagName();
			if (tag.equals("verweis")) {
				resultText.append(element.getAttribute("txt"));
			} else if (tag.equals("absatz")) {
				resultText.append("<p>");
				transferXml(element.getChildNodes(), resultText);
				resultText.append("</p>");
			} else if (tag.equals("titel")) {
				resultText.append("<p><b>");
				transferXml(element.getChildNodes(), resultText);
				resultText.append("</b></p>");
			} else if (tag.equals("wichtig")) {
				resultText.append("<b>");
				transferXml(element.getChildNodes(), resultText);
				resultText.append("</b>");
			} else if (tag.equals("liste")) {
				resultText.append("<ul>");
				transferXml(element.getChildNodes(), resultText);
				resultText.append("</ul>");
			} else if (tag.equals("numListe")) {
				resultText.append("<ol>");
				transferXml(element.getChildNodes(), resultText);
				resultText.append("</ol>");
			} else if (tag.equals("listenPunkt")) {
				resultText.append("<li>");
				transferXml(element.getChildNodes(), resultText);
				resultText.append("</li>");
			} else if (tag.equals("code") || tag.equals("pre")) {
				resultText.append("<code>");
				transferXml(element.getChildNodes(), resultText);
				resultText.append("</code>");
			} else if (tag.equals("kapitel") || tag.equals("sektion")) {
				transferXml(element.getChildNodes(), resultText);
			} else {
				_debug.finer("XML-Tag " + element.getTagName() + " wird noch nicht in HTML umgesetzt!");
			}
		} else if (node instanceof Text) {
			Text textNode = (Text) node;
			String text = textNode.getNodeValue();
			text = Pattern.compile("\\s+").matcher(text).replaceAll(" ");
			text = Pattern.compile("&").matcher(text).replaceAll("&amp;");
			text = Pattern.compile("<").matcher(text).replaceAll("&lt;");
			text = Pattern.compile(">").matcher(text).replaceAll("&gt;");
			text = Pattern.compile("�").matcher(text).replaceAll("&auml;");
			text = Pattern.compile("�").matcher(text).replaceAll("&ouml;");
			text = Pattern.compile("�").matcher(text).replaceAll("&uuml;");
			text = Pattern.compile("�").matcher(text).replaceAll("&szlig;");
			text = Pattern.compile("�").matcher(text).replaceAll("&Auml;");
			text = Pattern.compile("�").matcher(text).replaceAll("&Ouml;");
			text = Pattern.compile("�").matcher(text).replaceAll("&Uuml;");
			resultText.append(text);
		} else if (node instanceof CDATASection) {
			CDATASection cdataSection = (CDATASection) node;
			resultText.append(cdataSection.getNodeValue());
		}
	}

	/**
	 * Parst den Text nach XML-Tags und gibt sie zur weiteren Verarbeitung an die Methoden {@link #transferXml(org.w3c.dom.Node, StringBuilder)} weiter.
	 *
	 * @param nodes	  alle im Text enthaltenen Tags
	 * @param resultText den im HTML-Format umgewandelten Text
	 */
	private void transferXml(NodeList nodes, StringBuilder resultText) {
		int nodeCount = nodes.getLength();
		for (int i = 0; i < nodeCount; ++i) {
			transferXml(nodes.item(i), resultText);
		}
	}
}
