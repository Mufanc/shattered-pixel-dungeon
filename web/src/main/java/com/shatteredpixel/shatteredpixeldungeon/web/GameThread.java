/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;

import java.util.ArrayDeque;

/**
 * Runs the game on a TeaVM green thread.
 *
 * The browser drives gdx-teavm from requestAnimationFrame / DOM event callbacks, which are not
 * TeaVM threads, so anything that blocks (Object.wait, contended synchronized blocks) would throw there.
 * Shattered relies on exactly that to coordinate its render thread with the actor thread
 * (GameScene/Actor/InterlevelScene), so all lifecycle calls, posted runnables and input events
 * are forwarded to one long-lived "render thread", like on desktop.
 */
public class GameThread implements ApplicationListener {

	private final ApplicationListener game;

	private final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
	private final ArrayDeque<Runnable> posted = new ArrayDeque<>();
	private final QueuedInput inputQueue = new QueuedInput();
	private InputProcessor gameInput;

	private AsyncCallback<Void> parked;
	private boolean renderQueued;
	private boolean crashed;

	private final Runnable renderTask = new Runnable() {
		@Override
		public void run() {
			renderQueued = false;
			runPosted();
			if (gameInput != null) {
				inputQueue.drain(gameInput);
			}
			game.render();
		}
	};

	public GameThread( ApplicationListener game ){
		this.game = game;
	}

	// ---- called from browser callbacks (non-threaded context): only queue work ----

	@Override
	public void create() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				loop();
			}
		}, "SHPD Render Thread");
		thread.start();

		queue(new Runnable() {
			@Override
			public void run() {
				game.create();
				//route DOM input through a queue that is drained on this thread, see class comment
				gameInput = Gdx.input.getInputProcessor();
				Gdx.input.setInputProcessor(inputQueue);
				WebKeyboard.install(inputQueue);
			}
		});
	}

	@Override
	public void resize(final int width, final int height) {
		queue(new Runnable() {
			@Override
			public void run() {
				game.resize(width, height);
			}
		});
	}

	@Override
	public void render() {
		if (!renderQueued) {
			renderQueued = true;
			queue(renderTask);
		}
	}

	@Override
	public void pause() {
		queue(new Runnable() {
			@Override
			public void run() {
				game.pause();
			}
		});
	}

	@Override
	public void resume() {
		queue(new Runnable() {
			@Override
			public void run() {
				game.resume();
			}
		});
	}

	@Override
	public void dispose() {
		queue(new Runnable() {
			@Override
			public void run() {
				game.dispose();
			}
		});
	}

	//replacement for Gdx.app.postRunnable, runs before the next frame on the render thread
	public void post( Runnable r ){
		posted.add(r);
	}

	// ---- render thread ----

	private void queue( Runnable r ){
		if (crashed) return;
		tasks.add(r);
		if (parked != null) {
			AsyncCallback<Void> cb = parked;
			parked = null;
			cb.complete(null);
		}
	}

	private void runPosted(){
		int count = posted.size();
		for (int i = 0; i < count && !posted.isEmpty(); i++) {
			posted.poll().run();
		}
	}

	private void loop(){
		while (true) {
			Runnable task = tasks.poll();
			if (task == null) {
				park(this);
				continue;
			}
			try {
				task.run();
			} catch (Throwable t) {
				crashed = true;
				tasks.clear();
				WebCrashHandler.handle(t);
				return;
			}
		}
	}

	@Async
	private static native void park( GameThread self );

	private static void park( GameThread self, AsyncCallback<Void> callback ){
		self.parked = callback;
	}

}
