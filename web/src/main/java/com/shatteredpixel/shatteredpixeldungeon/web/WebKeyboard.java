/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * On-screen keyboard for touch devices.
 * Browsers only show a virtual keyboard for a focused text element, so the page keeps a hidden
 * <input> (see index.html). Characters typed into it are sent to the game as libGDX keyTyped events,
 * which is what scene2d's TextField (used by Shattered's TextInput) consumes, including '\b' and '\n'.
 */
public class WebKeyboard {

	@JSFunctor
	public interface CharListener extends JSObject {
		void typed( int c );
	}

	private static QueuedInput input;

	public static void install( QueuedInput target ){
		input = target;
		register(new CharListener() {
			@Override
			public void typed(int c) {
				if (input != null) input.keyTyped((char) c);
			}
		});
	}

	public static void setVisible( boolean visible, boolean multiline ){
		setVisibleJS(visible, multiline);
	}

	@JSBody(params = "listener", script = "window.spd = window.spd || {}; window.spd.onKeyboardChar = listener;")
	private static native void register( CharListener listener );

	@JSBody(params = { "visible", "multiline" },
			script = "if (window.spd && window.spd.keyboard) window.spd.keyboard(visible, multiline);")
	private static native void setVisibleJS( boolean visible, boolean multiline );

}
