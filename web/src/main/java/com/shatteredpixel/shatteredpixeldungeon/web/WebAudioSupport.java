/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Web integration added for this fork. See LICENSE.txt at the repository root.
 */

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.github.xpenatan.gdx.teavm.backends.web.webaudio.howler.HowlTeaAudio;

public class WebAudioSupport extends HowlTeaAudio {
    @Override
    public Music newMusic(FileHandle file) {
        if (!WebJS.canPlayOgg() && file.path().endsWith(".ogg")) {
            file = Gdx.files.internal(file.path().replace(".ogg", ".mp3"));
        }

        return super.newMusic(file);
    }
}
