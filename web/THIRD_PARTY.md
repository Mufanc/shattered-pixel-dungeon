# Web source provenance

The browser integration under
`src/main/java/com/shatteredpixel/shatteredpixeldungeon/web` was added for this
fork and uses GPL-3.0-or-later (see the root `LICENSE.txt`). Its former blanket
Oleg Dolya / Evan Debenham headers were copied from the project template and did
not accurately describe authorship of the new integration files.

`WebPlatformSupport.java` adapts the font implementation from Shattered Pixel
Dungeon's `desktop/src/main/java/com/shatteredpixel/shatteredpixeldungeon/desktop/DesktopPlatformSupport.java`.
Its original Oleg Dolya / Evan Debenham GPL notice is retained. Local changes
provide browser platform hooks and Unicode text splitting.

Generic platform-loader, nested-class metadata, and WebGL client-array support
now live in the gdx-teavm fork under `vendor/gdx-teavm`. They are consumed as
source dependencies rather than copied into this module. The optional TeaVM
number-symbol override was removed to keep the minimal upstream behavior.

Upstream projects:
- https://github.com/00-Evan/shattered-pixel-dungeon
- https://github.com/xpenatan/gdx-teavm
- https://github.com/konsoletyper/teavm
