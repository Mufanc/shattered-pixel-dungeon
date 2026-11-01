/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.github.xpenatan.gdx.teavm.backends.web.utils.WebBaseUrlProvider;

import org.teavm.jso.JSBody;

//assets and scripts are loaded from the versioned build folder given by index.html (window.SPD_BASE)
public class WebBaseUrl implements WebBaseUrlProvider {

	@Override
	public String getBaseUrl() {
		return baseUrl();
	}

	@JSBody(script = "var base = window.SPD_BASE || '';"
			+ "if (base.indexOf('%') >= 0) base = '';" //unprocessed template, e.g. the raw debug build
			+ "return new URL(base, document.baseURI).href;")
	private static native String baseUrl();
}
