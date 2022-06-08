/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2022 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Contact e-mail: andrew.dascalu@gmail.com
 */

package messaging;

/**
 * A list of default channel IDs.
 */
public enum DefaultChannels {
    CHANNEL_A("ezlib-a"),
    CHANNEL_B("ezlib-b"),
    CHANNEL_C("ezlib-c"),
    CHANNEL_D("ezlib-d"),
    ;

    private final String name;

    // remember. char limit is 8.
    DefaultChannels(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
