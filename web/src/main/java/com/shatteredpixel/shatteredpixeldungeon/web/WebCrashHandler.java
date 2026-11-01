/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.watabou.noosa.Game;

import java.io.PrintWriter;
import java.io.StringWriter;

//web equivalent of the crash dialog that DesktopLauncher shows through tinyfd
public class WebCrashHandler {

	private static boolean crashed = false;

	public static void handle( Throwable t ){
		try {
			Game.reportException(t);
		} catch (Throwable ignored) {
			//reporting must never hide the original error
		}
		if (crashed) return;
		crashed = true;

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		String msg = sw.toString();
		//errors raised by JavaScript itself (TypeError etc.) carry the useful stack in the JS error
		String jsStack = jsStack(t);
		if (jsStack != null) {
			msg += "\nJavaScript stack:\n" + jsStack;
		}
		if (msg.length() > 8000){
			msg = msg.substring(0, 8000) + "...";
		}
		WebJS.crash("Shattered Pixel Dungeon has run into an error it cannot recover from and has crashed, sorry about that!\n\n"
				+ "version: " + Game.version + "\n" + msg);
	}

	@org.teavm.jso.JSBody(params = "t", script = "var e = t && t.$jsException; return e && e.stack ? String(e.stack) : null;")
	private static native String jsStack(Throwable t);

	public static void install(){
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				handle(throwable);
			}
		});
	}

}
