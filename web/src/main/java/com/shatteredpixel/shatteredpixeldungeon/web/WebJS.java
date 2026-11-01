/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Small bridge to the browser and to the helpers defined in webapp/index.html (window.spd).
 * Every helper on the page side is optional, so the game still runs if the page is replaced.
 */
public class WebJS {

	@JSFunctor
	public interface Callback extends JSObject {
		void call();
	}

	@JSBody(params = "cb", script = "window.spd = window.spd || {}; window.spd.onFullscreenExit = cb;")
	public static native void onFullscreenExit(Callback cb);

	@JSBody(script = "return /iPad|iPhone|iPod/.test(navigator.userAgent)"
			+ " || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);")
	public static native boolean isIOS();

	@JSBody(script = "return navigator.language || (navigator.languages && navigator.languages[0]) || 'en';")
	public static native String browserLanguage();

	@JSBody(script = "return /Android/i.test(navigator.userAgent);")
	public static native boolean isAndroid();

	@JSBody(script = "return /Mac/i.test(navigator.platform || navigator.userAgent);")
	public static native boolean isMac();

	@JSBody(script = "return /Win/i.test(navigator.platform || navigator.userAgent);")
	public static native boolean isWindows();

	//major iOS version, 0 if unknown / not iOS
	@JSBody(script = "var m = navigator.userAgent.match(/OS (\\d+)_/) || navigator.userAgent.match(/Version\\/(\\d+)/);"
			+ "return m ? parseInt(m[1]) : 0;")
	public static native int iOSVersion();

	@JSBody(script = "try { var a = document.createElement('audio');"
			+ " return !!a.canPlayType && a.canPlayType('audio/ogg; codecs=\"vorbis\"') !== ''; }"
			+ " catch (e) { return false; }")
	public static native boolean canPlayOgg();

	@JSBody(script = "return !!(navigator.vibrate);")
	public static native boolean canVibrate();

	@JSBody(params = "ms", script = "try { navigator.vibrate(ms); } catch (e) {}")
	public static native void vibrate(int ms);

	@JSBody(script = "var d = document.documentElement;"
			+ " return !!(d.requestFullscreen || d.webkitRequestFullscreen);")
	public static native boolean fullscreenSupported();

	@JSBody(params = "on", script = "if (window.spd && window.spd.setFullscreen) window.spd.setFullscreen(on);")
	public static native void setFullscreen(boolean on);

	@JSBody(params = "landscape", script = "if (window.spd && window.spd.setLandscape) window.spd.setLandscape(landscape);")
	public static native void setLandscape(boolean landscape);

	@JSBody(params = "url", script = "var w = window.open(url, '_blank', 'noopener'); return !!w;")
	public static native boolean openURL(String url);

	//safe area insets in CSS pixels, [left, top, right, bottom]
	@JSBody(script = "return (window.spd && window.spd.safeInsets) ? window.spd.safeInsets() : [0,0,0,0];")
	public static native double[] safeInsets();

	@JSBody(script = "return window.devicePixelRatio || 1;")
	public static native double devicePixelRatio();

	@JSBody(params = { "done", "total" }, script = "if (window.spd && window.spd.progress) window.spd.progress(done, total);")
	public static native void loadProgress(int done, int total);

	@JSBody(script = "if (window.spd && window.spd.loaded) window.spd.loaded();")
	public static native void loaded();

	@JSBody(params = "message", script = "console.error(message); if (window.spd && window.spd.crash) window.spd.crash(message);")
	public static native void crash(String message);

	@JSBody(script = "if (window.spd && window.spd.exit) window.spd.exit();")
	public static native void exit();

	@JSBody(params = "key", script = "try { return new URLSearchParams(location.search).get(key); } catch (e) { return null; }")
	public static native String queryParam(String key);

}
