/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetInstance;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;

//downloads the asset manifest while the page shows its own HTML loading screen (see index.html)
public class WebPreloader extends ApplicationAdapter {

	private AssetLoader loader;
	private int total = -1;
	private boolean done;

	@Override
	public void create() {
		loader = AssetInstance.getLoaderInstance();
		loader.preload(new AssetLoaderListener<Void>() {
			@Override
			public void onSuccess(String url, Void result) {
				total = loader.getQueue();
			}

			@Override
			public void onFailure(String url) {
				WebJS.crash("Failed to download the game files (" + url + "). Please check your connection and reload the page.");
			}
		});
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		if (total < 0 || done) return;

		int remaining = loader.getQueue();
		WebJS.loadProgress(total - remaining, total);
		if (remaining == 0 && !loader.isDownloading()) {
			done = true;
			WebJS.loaded();
			WebApplication.get().setPreloadReady();
		}
	}
}
