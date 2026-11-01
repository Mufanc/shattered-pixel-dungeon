/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class RingOfGuarding extends Ring {

	{
		unique = true;
		bones = false;
		levelKnown = cursedKnown = true;
		icon = ItemSpriteSheet.Icons.RING_EVASION;
		buffClass = Guarding.class;
	}

	private int regenerationTurns;
	private int healingRemainder;
	private int lifeStealRemainder;

	@Override
	public void reset() {
		super.reset();
		image = ItemSpriteSheet.RING_GUARDING;
	}

	@Override
	public boolean isKnown() {
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public Item upgrade() {
		return this;
	}

	public static boolean isActive(Char target) {
		if (!(target instanceof Hero) || target.buff(MagicImmune.class) != null) {
			return false;
		}

		Hero hero = (Hero) target;

		return hero.belongings.ring() instanceof RingOfGuarding
				|| hero.belongings.misc() instanceof RingOfGuarding;
	}

	public static float hungerMultiplier(Char target) {
		return isActive(target) ? 0.65f : 1f;
	}

	@Override
	protected RingBuff buff() {
		return new Guarding();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("regeneration_turns", regenerationTurns);
		bundle.put("healing_remainder", healingRemainder);
		bundle.put("life_steal_remainder", lifeStealRemainder);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		regenerationTurns = bundle.getInt("regeneration_turns");
		healingRemainder = bundle.getInt("healing_remainder");
		lifeStealRemainder = bundle.getInt("life_steal_remainder");
	}

	public class Guarding extends RingBuff {

		{
			spend(TICK);
		}

		public void onAttack(int damage) {
			if (damage <= 0 || !target.isAlive() || !isActive(target) || !isEquipped((Hero) target)) {
				return;
			}

			lifeStealRemainder += damage;

			int healing = Math.min(target.HT - target.HP, lifeStealRemainder / 5);

			lifeStealRemainder %= 5;
			target.HP += healing;

			if (target.HP == target.HT) {
				lifeStealRemainder = 0;
			}

			if (healing > 0 && target.sprite != null) {
				target.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healing), FloatingText.HEALING);
			}
		}

		@Override
		public boolean act() {
			if (target.isAlive() && isActive(target) && isEquipped((Hero) target)) {
				regenerationTurns++;

				if (regenerationTurns >= 10) {
					regenerationTurns = 0;

					if (target.HP < target.HT) {
						// Keep hundredths so 3% healing is exact even at low maximum health.
						healingRemainder += target.HT * 3;
						target.HP = Math.min(target.HT, target.HP + healingRemainder / 100);
						healingRemainder %= 100;
					}

					if (target.HP == target.HT) {
						healingRemainder = 0;
					}
				}
			}

			return super.act();
		}
	}
}
