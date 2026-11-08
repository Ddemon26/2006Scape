package com.rs2.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all of our cycle based events
 * 
 * @author Stuart <RogueX>
 * @author Null++
 */
public class CycleEventHandler {

	/**
	 * The instance of this class
	 */
	private static CycleEventHandler instance;

	/**
	 * Returns the instance of this class
	 * 
	 * @return
	 */
	public static CycleEventHandler getSingleton() {
		if (instance == null) {
			instance = new CycleEventHandler();
		}
		return instance;
	}

	/**
	 * Holds all of our events currently being ran
	 */
	private final List<CycleEventContainer> events;

	/**
	 * Creates a new instance of this class
	 */
	public CycleEventHandler() {
		events = new ArrayList<CycleEventContainer>();
	}

	/**
	 * Add an event to the list
	 * 
	 * @param id
	 * @param owner
	 * @param event
	 * @param cycles
	 */
	public CycleEventContainer addEvent(int id, Object owner, CycleEvent event, int cycles) {
		CycleEventContainer container = new CycleEventContainer(id, owner, event, cycles);
		events.add(container);
		return container;
	}

	/**
	 * Add an event to the list
	 * 
	 * @param owner
	 * @param event
	 * @param cycles
	 */
	public CycleEventContainer addEvent(Object owner, CycleEvent event, int cycles) {
		CycleEventContainer container = new CycleEventContainer(-1, owner, event, cycles);
		events.add(container);
		return container;
	}

	/**
	 * Executes the events that are due, then removes every stopped event.
	 * <p>
	 * Events added while this runs (an event starting another) are appended to the list and first
	 * run next cycle. Stopped events are removed whoever stopped them: the event itself, or
	 * {@link #stopEvents} / {@link CycleEventContainer#stop()} from outside. Events stopped from
	 * outside used to stay in the list forever (they never ran again, so they were never removed),
	 * so the list, and the time this method takes, grew with server uptime.
	 */
	public void process() {
		int due = events.size();
		for (int i = 0; i < due; i++) {
			CycleEventContainer c = events.get(i);
			if (c != null && c.needsExecution()) {
				c.execute();
			}
		}
		events.removeIf(c -> c == null || !c.isRunning());
	}

	/**
	 * Returns the amount of events currently running
	 * 
	 * @return amount
	 */
	public int getEventsCount() {
		return events.size();
	}

	/**
	 * Stops all events for a specific owner and id
	 * 
	 * @param owner
	 */
	public void stopEvents(Object owner) {
		for (CycleEventContainer c : events) {
			if (c.getOwner() == owner) {
				c.stop();
			}
		}
	}

	/**
	 * Stops all events for a specific owner and id
	 * 
	 * @param owner
	 * @param id
	 */
	public void stopEvents(Object owner, int id) {
		for (CycleEventContainer c : events) {
			if (c.getOwner() == owner && id == c.getID()) {
				c.stop();
			}
		}
	}

	/**
	 * Stops all events for a specific owner and id
	 * 
	 * @param id
	 */
	public void stopEvents(int id) {
		for (CycleEventContainer c : events) {
			if (id == c.getID()) {
				c.stop();
			}
		}
	}

}
