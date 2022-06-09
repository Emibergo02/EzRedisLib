/*
 * EzRedisLib - A redis library.
 * Copyright (C) 2022 Emiliano Bergonzani
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
 *  Contact e-mail: emibergo@gmail.com
 */

package ezredislib.channel;



import org.jetbrains.annotations.NotNull;

/**
 * A channel is used to handle reading and actions related to a channel.
 */
@Deprecated
public interface ChannelListener<T> {

    /**
     * Reads packet from the channel.
     * If you want to listen for a packet subclass you need to register another listener.
     * @param message the packet received.
     */
    void read(T message);


    @NotNull String getChannelName();
}
