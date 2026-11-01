/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetInstance;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;

public class SPDWebApplication extends WebApplication {

	private static GameThread gameThread;

	public SPDWebApplication(GameThread game, ApplicationListener preloader, WebApplicationConfiguration config) {
		super(register(game), preloader, config);
	}

	//the game thread must be known before the superclass constructor starts the app
	private static GameThread register(GameThread game){
		gameThread = game;
		return game;
	}

	public static GameThread gameThread(){
		return gameThread;
	}

	//libGDX runnables must run on the game thread, not in the browser's animation frame callback
	@Override
	public void postRunnable(Runnable runnable) {
		gameThread.post(runnable);
	}

	@Override
	public int getVersion() {
		//Gdx.app.getVersion is the OS major version on mobile (used by DeviceCompat.getPlatformVersion)
		return WebJS.isIOS() ? WebJS.iOSVersion() : 0;
	}

	@Override
	public void exit() {
		WebJS.exit();
	}

	@Override
	protected void initAudio() {
		addInitQueue();
		AssetInstance.getLoaderInstance().loadScript("howler.js", new AssetLoaderListener<String>() {
			@Override
			public void onSuccess(String url, String result) {
				subtractInitQueue();
				Gdx.audio = new WebAudioSupport();
			}
		});
	}

	@Override
	protected void onError(Throwable error) {
		WebCrashHandler.handle(error);
	}
}
