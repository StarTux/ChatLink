/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012 StarTux
 *
 * This file is part of ChatLink.
 *
 * ChatLink is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * ChatLink is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with ChatLink.  If not, see
 * <http://www.gnu.org/licenses/>.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.winthier.chatlink.channel;

import com.winthier.chatlink.ChatLinkPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncChatChannel extends DefaultChannel {
        public AsyncChatChannel(ChatLinkPlugin plugin, String name) {
                super(plugin, name);
        }

        @Override
        public AsyncListener getListener(ChatLinkPlugin plugin) {
                return AsyncListener.getInstance(plugin);
        }

        protected static class AsyncListener extends DefaultListener {
                private static AsyncListener instance;

                private AsyncListener(ChatLinkPlugin plugin) {
                        super(plugin);
                }

                public static AsyncListener getInstance(ChatLinkPlugin plugin) {
                        if (instance == null || instance.plugin != plugin) instance = new AsyncListener(plugin);
                        return instance;
                }

                @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
                public void onPlayerChat(AsyncPlayerChatEvent event) {
                        DefaultChannel channel = ignoreMap.get(event.getMessage());
                        if (channel != null) {
                                event.setCancelled(true);
                                channel.ignore(event.getPlayer());
                                return;
                        }
                        synchronized (speakMap) {
                                for (String key : speakMap.keySet()) {
                                        if (event.getMessage().startsWith(key)) {
                                                event.setCancelled(true);
                                                speakMap.get(key).chat(event.getPlayer(), event.getMessage().substring(key.length()).trim());
                                                return;
                                        }
                                }
                        }
                }

        }
}
