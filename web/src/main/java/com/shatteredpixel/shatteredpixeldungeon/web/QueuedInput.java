/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.badlogic.gdx.InputProcessor;

import java.util.ArrayList;

//records input events coming from DOM callbacks and replays them, in order, on the game thread
public class QueuedInput implements InputProcessor {

	private interface Event {
		void send( InputProcessor p );
	}

	private ArrayList<Event> events = new ArrayList<>();
	private ArrayList<Event> draining = new ArrayList<>();

	public void drain( InputProcessor target ){
		if (events.isEmpty()) return;
		ArrayList<Event> list = events;
		events = draining;
		draining = list;
		for (Event e : list){
			e.send(target);
		}
		list.clear();
	}

	private void add( Event e ){
		events.add(e);
	}

	@Override
	public boolean keyDown(int keycode) {
		add(p -> p.keyDown(keycode));
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		add(p -> p.keyUp(keycode));
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		add(p -> p.keyTyped(character));
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		add(p -> p.touchDown(screenX, screenY, pointer, button));
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		add(p -> p.touchUp(screenX, screenY, pointer, button));
		return true;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		add(p -> p.touchCancelled(screenX, screenY, pointer, button));
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		add(p -> p.touchDragged(screenX, screenY, pointer));
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		add(p -> p.mouseMoved(screenX, screenY));
		return true;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		add(p -> p.scrolled(amountX, amountY));
		return true;
	}
}
