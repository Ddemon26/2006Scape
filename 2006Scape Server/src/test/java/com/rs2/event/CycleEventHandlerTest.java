package com.rs2.event;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CycleEventHandlerTest {

	/** Counts its runs; stops itself after {@code stopAfter} runs (0 = never). */
	private static final class CountingEvent extends CycleEvent {
		final int stopAfter;
		int runs;
		int stops;

		CountingEvent(int stopAfter) {
			this.stopAfter = stopAfter;
		}

		@Override
		public void execute(CycleEventContainer container) {
			runs++;
			if (stopAfter > 0 && runs >= stopAfter) {
				container.stop();
			}
		}

		@Override
		public void stop() {
			stops++;
		}
	}

	@Test
	public void runsEveryTickCycles() {
		CycleEventHandler handler = new CycleEventHandler();
		CountingEvent event = new CountingEvent(0);
		handler.addEvent(this, event, 3);
		for (int i = 0; i < 9; i++) {
			handler.process();
		}
		assertEquals(3, event.runs);
		assertEquals(1, handler.getEventsCount());
	}

	@Test
	public void anEventThatStopsItselfIsRemoved() {
		CycleEventHandler handler = new CycleEventHandler();
		CountingEvent event = new CountingEvent(2);
		handler.addEvent(this, event, 1);
		handler.process();
		assertEquals(1, handler.getEventsCount());
		handler.process();
		assertEquals(0, handler.getEventsCount());
		handler.process();
		assertEquals(2, event.runs);
		assertEquals(1, event.stops);
	}

	@Test
	public void eventsStoppedFromOutsideAreRemovedAndNeverRunAgain() {
		CycleEventHandler handler = new CycleEventHandler();
		Object owner = new Object();
		CountingEvent byOwner = new CountingEvent(0);
		CountingEvent byOwnerAndId = new CountingEvent(0);
		CountingEvent byId = new CountingEvent(0);
		CountingEvent byContainer = new CountingEvent(0);
		CountingEvent untouched = new CountingEvent(0);
		handler.addEvent(owner, byOwner, 1);
		handler.addEvent(7, this, byOwnerAndId, 1);
		handler.addEvent(9, new Object(), byId, 1);
		CycleEventContainer container = handler.addEvent(new Object(), byContainer, 1);
		handler.addEvent(new Object(), untouched, 1);
		handler.process();

		handler.stopEvents(owner);
		handler.stopEvents(this, 7);
		handler.stopEvents(9);
		container.stop();
		handler.process();
		handler.process();

		assertEquals(1, handler.getEventsCount());
		assertEquals(1, byOwner.runs);
		assertEquals(1, byOwnerAndId.runs);
		assertEquals(1, byId.runs);
		assertEquals(1, byContainer.runs);
		assertEquals(3, untouched.runs);
	}

	@Test
	public void startingAndStoppingManyEventsDoesNotGrowTheList() {
		CycleEventHandler handler = new CycleEventHandler();
		Object owner = new Object();
		for (int i = 0; i < 10000; i++) {
			handler.addEvent(owner, new CountingEvent(0), 4);
			handler.process();
			handler.stopEvents(owner);
		}
		handler.process();
		assertEquals(0, handler.getEventsCount());
	}

	@Test
	public void anEventAddedWhileProcessingFirstRunsNextCycle() {
		final CycleEventHandler handler = new CycleEventHandler();
		final CountingEvent child = new CountingEvent(0);
		final List<String> order = new ArrayList<String>();
		handler.addEvent(this, new CycleEvent() {
			@Override
			public void execute(CycleEventContainer container) {
				order.add("parent");
				handler.addEvent(this, child, 1);
				container.stop();
			}

			@Override
			public void stop() {
			}
		}, 1);

		handler.process();
		assertEquals(0, child.runs);
		assertEquals(1, handler.getEventsCount());
		handler.process();
		assertEquals(1, child.runs);
		assertEquals(1, order.size());
	}
}
