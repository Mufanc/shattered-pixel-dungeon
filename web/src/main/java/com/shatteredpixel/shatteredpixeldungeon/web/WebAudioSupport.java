/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 * LazyMusic is adapted from goddeaterr/web-pixeldungeon's WebAudioSupport.java.
 * Retained upstream notices: Pixel Dungeon, Copyright (C) 2012-2015 Oleg Dolya;
 * Shattered Pixel Dungeon, Copyright (C) 2014-2026 Evan Debenham.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetInstance;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetType;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.WebBlob;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.github.xpenatan.gdx.teavm.backends.web.webaudio.howler.HowlTeaAudio;

public class WebAudioSupport extends HowlTeaAudio {
    @Override
    public Music newMusic(FileHandle file) {
        if (!WebJS.canPlayOgg() && file.path().endsWith(".ogg")) {
            file = Gdx.files.internal(file.path().replace(".ogg", ".mp3"));
        }

        return new LazyMusic(file.path());
    }
    private Music createLoadedMusic(String path) {
        return super.newMusic(Gdx.files.internal(path));
    }

    // Adapted from goddeaterr/web-pixeldungeon, WebAudioSupport.java (GPL-3.0-or-later).
	private class LazyMusic implements Music {

		private Music music;
		private boolean disposed;

		private boolean playing;
		private boolean looping;
		private float volume = 1f;
		private float pan = 0f;
		private float position = 0f;
		private OnCompletionListener listener;

		LazyMusic(final String path){
			final AssetLoader loader = AssetInstance.getLoaderInstance();
			if (loader.isAssetLoaded(Files.FileType.Internal, path)){
				create(path);
			} else {
				loader.loadAsset(path, AssetType.Binary, Files.FileType.Internal, new AssetLoaderListener<WebBlob>() {
					@Override
					public void onSuccess(String url, WebBlob result) {
						//applied on the game thread, like every other audio call
						SPDWebApplication.gameThread().post(new Runnable() {
							@Override
							public void run() {
								create(path);
							}
						});
					}

					@Override
					public void onFailure(String url) {
						Gdx.app.error("AUDIO", "could not load music " + url);
					}
				});
			}
		}

		private void create(String path){
			if (disposed || music != null) return;
			music = createLoadedMusic(path);
			music.setLooping(looping);
			music.setVolume(volume);
			if (pan != 0f) music.setPan(pan, volume);
			if (position > 0f) music.setPosition(position);
			attachCompletion();
			if (playing) music.play();
		}

		@Override
		public void play() {
			playing = true;
			if (music != null) music.play();
		}

		@Override
		public void pause() {
			playing = false;
			if (music != null) music.pause();
		}

		@Override
		public void stop() {
			playing = false;
			position = 0;
			if (music != null) music.stop();
		}

		@Override
		public boolean isPlaying() {
			//while the track is downloading it counts as playing, so the game doesn't consider it finished
			return music != null ? music.isPlaying() : playing;
		}

		@Override
		public void setLooping(boolean isLooping) {
			looping = isLooping;
			if (music != null) music.setLooping(isLooping);
		}

		@Override
		public boolean isLooping() {
			return looping;
		}

		@Override
		public void setVolume(float volume) {
			this.volume = volume;
			if (music != null) music.setVolume(volume);
		}

		@Override
		public float getVolume() {
			return volume;
		}

		@Override
		public void setPan(float pan, float volume) {
			this.pan = pan;
			this.volume = volume;
			if (music != null) music.setPan(pan, volume);
		}

		@Override
		public void setPosition(float position) {
			this.position = position;
			if (music != null) music.setPosition(position);
		}

		@Override
		public float getPosition() {
			return music != null ? music.getPosition() : position;
		}

		@Override
		public void dispose() {
			disposed = true;
			playing = false;
			if (music != null) {
				music.dispose();
				music = null;
			}
		}

		//noosa's Music sets a new listener for every track it plays and clears it with null for single
		// (looping) tracks. gdx-teavm's HowlMusic adds one more Howler "end" handler on every call, null
		// listeners included, and that handler then calls the null listener. So the Howl gets exactly one
		// handler (in create) that looks up the current listener when the track ends.
		@Override
		public void setOnCompletionListener(OnCompletionListener listener) {
			this.listener = listener;
		}

		private void attachCompletion(){
			music.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(Music m) {
					//Howler reports completion from a browser callback; hand it to the game thread
					SPDWebApplication.gameThread().post(new Runnable() {
						@Override
						public void run() {
							//Howler also reports the end of every loop of a looping track, the desktop
							// backends don't: a looping track never completes
							OnCompletionListener current = listener;
							if (!disposed && !looping && current != null) current.onCompletion(LazyMusic.this);
						}
					});
				}
			});
		}
	}
}
