/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Point;

public class ToxicGasRoom extends SpecialRoom {

	@Override
	public int minWidth() { return 7; }
	public int minHeight() { return 7; }

	@Override
	public void paint(Level level) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		for (Point p : getPoints()){
			int cell = level.pointToCell(p);
			if (level.map[cell] == Terrain.EMPTY) {
				Blob.seed(cell, 3, ToxicGasSeed.class, level);
			}
		}

		int traps = Math.min(width()-2, height()-2);

		for (int i = 0; i < traps; i++){
			int cell;
			do {
				cell = level.pointToCell(random(1));
			} while (level.map[cell] == Terrain.INACTIVE_TRAP);
			level.setTrap(new ToxicVent().reveal(), cell);
			Painter.set(level, cell, Terrain.INACTIVE_TRAP);
		}

		//TODO loot!
		//perhaps 2-3 items around the center?

		level.addItemToSpawn(new PotionOfPurity());

		entrance().set( Door.Type.UNLOCKED );

	}

	@Override
	public boolean canCharacterWander(Point p, Level l) {
		return false;
	}

	public static class ToxicGasSeed extends Blob {

		@Override
		protected void evolve() {
			int cell;
			ToxicGas gas = (ToxicGas) Dungeon.level.blobs.get(ToxicGas.class);
			for (int i=area.top-1; i <= area.bottom; i++) {
				for (int j = area.left-1; j <= area.right; j++) {
					cell = j + i* Dungeon.level.width();
					if (Dungeon.level.insideMap(cell)) {
						off[cell] = cur[cell];
						volume += off[cell];

						if (gas == null || gas.volume == 0){
							GameScene.add(Blob.seed(cell, off[cell], ToxicGas.class));
						} else if (gas.cur[cell] < off[cell]){
							GameScene.add(Blob.seed(cell, off[cell] - gas.cur[cell], ToxicGas.class));
						}
					}
				}
			}
		}

	}

	public static class ToxicVent extends Trap {

		{
			color = BLACK;
			shape = GRILL;

			canBeHidden = false;
			active = false;
		}

		@Override
		public void activate() {
			//does nothing, this trap is just decoration and is always deactivated
		}

	}
}
