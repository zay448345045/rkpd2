/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

package com.zrp200.rkpd2.items.weapon.missiles;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class HeavyBoomerang extends MissileWeapon {
	
	{
		image = ItemSpriteSheet.BOOMERANG;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;
		
		tier = 4;
		sticky = false;
	}
	
	@Override
	public int max(int lvl) {
		return  4 * tier +                  //16 base, down from 20
				(tier) * lvl;               //scaling unchanged
	}

	private boolean circling;
	@Override
	protected void rangedHit(Char enemy, int cell) {
		if(circling) {
			super.rangedHit(enemy, cell);
			return;
		}
		decrementDurability();
		if (durability > 0){
			Buff.append(Dungeon.hero, CircleBack.class).setup(this, cell, Dungeon.hero.pos, Dungeon.depth);
		}
	}

	@Override
	protected void rangedMiss(int cell) {
		if(circling) {
			super.rangedMiss(cell);
			return;
		}
		parent = null;
		Buff.append(Dungeon.hero, CircleBack.class).setup(this, cell, Dungeon.hero.pos, Dungeon.depth);
	}
	
	public static class CircleBack extends Buff {
		
		private HeavyBoomerang boomerang;
		private int thrownPos;
		private int returnPos;
		private int returnDepth;
		
		private int left;
		
		public void setup( HeavyBoomerang boomerang, int thrownPos, int returnPos, int returnDepth){
			this.boomerang = boomerang;
			this.thrownPos = thrownPos;
			this.returnPos = returnPos;
			this.returnDepth = returnDepth;
			left = 3;
		}
		
		public int returnPos(){
			return returnPos;
		}
		
		public MissileWeapon cancel(){
			detach();
			return boomerang;
		}
		
		@Override
		public boolean act() {
			if (returnDepth == Dungeon.depth){
				left--;
				if (left <= 0){
					final Char returnTarget = Actor.findChar(returnPos);
					final Char target = this.target;
					MissileSprite visual = ((MissileSprite) Dungeon.hero.sprite.parent.recycle(MissileSprite.class));
					visual.reset( thrownPos,
									returnPos,
									boomerang,
									new Callback() {
										@Override
										public void call() {
											if (returnTarget == target){
												if (target instanceof Hero && boomerang.doPickUp((Hero) target)) {
													//grabbing the boomerang takes no time
													((Hero) target).spend(-TIME_TO_PICK_UP);
												} else {
													Dungeon.level.drop(boomerang, returnPos).sprite.drop();
												}
												
											} else if (returnTarget != null){
												boomerang.circling = true;
												((Hero)target).shoot( returnTarget, boomerang );
												boomerang.circling = false;
											} else {
												Dungeon.level.drop(boomerang, returnPos).sprite.drop();
											}
											CircleBack.this.next();
										}
									});
					visual.alpha(0f);
					float duration = Dungeon.level.trueDistance(thrownPos, returnPos) / 20f;
					target.sprite.parent.add(new AlphaTweener(visual, 1f, duration));
					detach();
					return false;
				}
			}
			spend( TICK );
			return true;
		}
		
		private static final String BOOMERANG = "boomerang";
		private static final String THROWN_POS = "thrown_pos";
		private static final String RETURN_POS = "return_pos";
		private static final String RETURN_DEPTH = "return_depth";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(BOOMERANG, boomerang);
			bundle.put(THROWN_POS, thrownPos);
			bundle.put(RETURN_POS, returnPos);
			bundle.put(RETURN_DEPTH, returnDepth);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			boomerang = (HeavyBoomerang) bundle.get(BOOMERANG);
			thrownPos = bundle.getInt(THROWN_POS);
			returnPos = bundle.getInt(RETURN_POS);
			returnDepth = bundle.getInt(RETURN_DEPTH);
		}
	}
	
}
