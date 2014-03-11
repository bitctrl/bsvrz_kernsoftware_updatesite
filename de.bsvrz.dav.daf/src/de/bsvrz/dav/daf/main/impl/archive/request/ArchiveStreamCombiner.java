/*
 * Copyright 2013 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.archive.request;

import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.archive.ArchiveData;
import de.bsvrz.dav.daf.main.archive.ArchiveDataQueryResult;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveDataStream;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Diese Klasse bündelt bei Anfragen nach Pids eventuell mehrere Ergebnis-Streams (pro historischem Objekt) zu einem einzigen Stream (pro Pid),
 * sodass die Streams den angefragten Daten entsprechen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11925 $
 */
public class ArchiveStreamCombiner implements ArchiveDataQueryResult {
	private ArchiveDataQueryResult _rawResult;
	private List<Integer> _numStreamsToCombine = new ArrayList<Integer>();
	private List<ArchiveDataSpecification> _originalSpecs = new ArrayList<ArchiveDataSpecification>();

	@Override
	public ArchiveDataStream[] getStreams() throws InterruptedException, IllegalStateException {
		ArchiveDataStream[] streams = _rawResult.getStreams(); // wirft Exception, falls nicht erfolgreich
		return combineStreams(streams);
	}

	private ArchiveDataStream[] combineStreams(final ArchiveDataStream[] streams) {
		int streamIndex = 0;
		ArchiveDataStream[] result = new ArchiveDataStream[_numStreamsToCombine.size()];
		for(int i = 0; i < _numStreamsToCombine.size(); i++) {
			final Integer count = _numStreamsToCombine.get(i);
			ArchiveDataSpecification spec = _originalSpecs.get(i);
			assert count > 0;
			if(count == 1){
				result[i] = streams[streamIndex];
			}
			else if(streams[streamIndex].getDataSpecification().getTimeSpec().isStartRelative()) {
				ArchiveDataStream[] streamsToCombine = Arrays.copyOfRange(streams, streamIndex, streamIndex + count);
				result[i] = new RelativeCombinedStream(streamsToCombine, spec);
			}
			else {
				ArchiveDataStream[] streamsToCombine = Arrays.copyOfRange(streams, streamIndex, streamIndex + count);
				result[i] = new CombinedStream(streamsToCombine, spec);
			}
			streamIndex += count;
		}
		return result;
	}

	@Override
	public boolean isRequestSuccessful() throws InterruptedException {
		return _rawResult.isRequestSuccessful();
	}

	@Override
	public String getErrorMessage() throws InterruptedException {
		return _rawResult.getErrorMessage();
	}

	public void setRawResult(final ArchiveDataQueryResult rawResult) {
		_rawResult = rawResult;
	}

	/**
	 * Wird mehrmals aufgerufen. Vermerkt jeweils, wie zusammengehörige Streams zu bündeln sind.
	 * @param size Anzahl zu bündelnder Streams, bei Anfragen ohne Pid 1
	 * @param spec Originale Anfrage
	 */
	public void addQuery(final int size, final ArchiveDataSpecification spec) {
		_numStreamsToCombine.add(size);
		_originalSpecs.add(spec);
	}

	private static class CombinedStream implements ArchiveDataStream {
		private ArchiveDataStream[] _streamsToCombine;
		private ArchiveDataSpecification _spec;
		private int _currentIndex = 0;
		private boolean hasSendData = false;

		public CombinedStream(final ArchiveDataStream[] streamsToCombine, final ArchiveDataSpecification spec) {
			_streamsToCombine = streamsToCombine;
			_spec = spec;
		}

		@Override
		public ArchiveDataSpecification getDataSpecification() {
			return _spec;
		}

		private ArchiveDataStream current() {
			return _streamsToCombine[_currentIndex];
		}

		@Override
		public ArchiveData take() throws InterruptedException, IOException, IllegalStateException {
			ArchiveData take = current().take();
			if(take == null){
				if(_currentIndex + 1 < _streamsToCombine.length){
					// Nächsten Stream öffnen
					_currentIndex++;
					take = current().take();
				}
			}
			if(_currentIndex + 1 < _streamsToCombine.length){
				if(take != null && take.getDataType() == DataState.END_OF_ARCHIVE){
					// Ende-Archiv-Datensätze, die sich nicht am Ende befinden, in potentielle Datenlücken umwandeln
					if(!hasSendData){
						// Wenn Stream komplett leer ist, ignorieren und nächsten Datensatz nehmen
						return take();
					}
					take = new StreamedArchiveData(
							take.getDataTime(), take.getArchiveTime(), take.getDataIndex(),
							DataState.POSSIBLE_GAP,
							take.getDataKind(),
							take.getData(),
							take.getObject(),
							take.getDataDescription()
					);
				}
			}
			if(take != null && take.getDataType() != DataState.POSSIBLE_GAP){
				hasSendData = true;
			}
			return take;
		}

		@Override
		public void abort() {
			// Noch nicht abgeschlossene Streams abbrechen
			for(int i = _currentIndex; i < _streamsToCombine.length; i++){
				_streamsToCombine[i].abort();
			}
		}
	}

	private static class RelativeCombinedStream implements ArchiveDataStream {
		private final ArchiveDataSpecification _spec;
		private Exception _exception = null;
		private final ArrayDeque<ArchiveData> _buffer;

		public RelativeCombinedStream(final ArchiveDataStream[] streamsToCombine, ArchiveDataSpecification spec) {

			// Siehe de.bsvrz.ars.ars.mgmt.tasks.ArchiveQueryTask.Query.maxInterval
			long maxSize = Math.min(spec.getTimeSpec().getIntervalStart(), 16000);

			// Erstmal alle Stream wie gewohnt verketten
			ArchiveDataStream parent = new CombinedStream(streamsToCombine, spec);

			// dann alle Datensätze nacheinander in den (begrenzten) Puffer schieben
			// sodass am Schluss nur die aktuellsten Datensätze übrig bleiben
			_spec = spec;
			_buffer = new ArrayDeque<ArchiveData>();
			try {
				ArchiveData aData;
				int currentDataSets = 0;
				while((aData = parent.take()) != null){
					_buffer.addLast(aData);
					if(shouldCount(aData)){
						currentDataSets++;
					}
					while(currentDataSets > maxSize){
						ArchiveData first = _buffer.removeFirst();
						if(shouldCount(first)){
							currentDataSets--;
						}
						while(_buffer.peekFirst() != null && _buffer.peekFirst().getDataType() == DataState.POSSIBLE_GAP){
							// Potentielle Lücken am Anfang trimmen
							first = _buffer.removeFirst();
							if(shouldCount(first)){
								currentDataSets--;
							}
						}
					}
				}
			}
			catch(Exception e) {
				_exception = e;
			}
		}

		/**
		 * Gibt zurück ob ein Datensatz bei Relativanfragen mitgezählt werden soll oder nicht.
		 * @param aData Datum
		 * @return true wenn mitgezählt werden soll, sonst false
		 */
		private static boolean shouldCount(final ArchiveData aData) {
			return aData.getDataType() != DataState.END_OF_ARCHIVE;
		}

		@Override
		public ArchiveDataSpecification getDataSpecification() {
			return _spec;
		}

		@Override
		public ArchiveData take() throws InterruptedException, IOException, IllegalStateException {
			if(_exception != null){
				if(_exception instanceof RuntimeException) {
					throw (RuntimeException) _exception;
				}
				else if(_exception instanceof IOException) {
					throw (IOException) _exception;
				}
				else if(_exception instanceof IllegalStateException) {
					throw (IllegalStateException) _exception;
				}
				else {
					// Sollte nicht auftreten
					throw new RuntimeException(_exception);
				}
			}
			return _buffer.pollFirst();
		}

		@Override
		public void abort() {
			_exception = new IllegalStateException("Der Stream wurde mit 'abort' abgebrochen und dann erneut mit 'take' aufgerufen");
		}
	}
}
