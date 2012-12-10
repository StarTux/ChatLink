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
import com.winthier.chatlink.Util;
import com.winthier.chatlink.packet.ChatPacket;
import com.winthier.winlink.BukkitRunnable;
import com.winthier.winlink.WinLink;
import com.winthier.winlink.WinLinkPlugin;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

public abstract class DefaultChannel implements Channel, Listener {
        protected ChatLinkPlugin plugin;
        protected String name;
        protected String prefix;
        protected String ignore;
        protected String format;
        protected boolean enabled = false;
        protected Set<String> ignoreList = Collections.synchronizedSet(new LinkedHashSet<String>());

        public DefaultChannel(ChatLinkPlugin plugin, String name) {
                this.plugin = plugin;
                this.name = name;
        }

        @Override
        public void enable() {
                enabled = true;
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }


        @Override
        public void disable() {
                enabled = false;
        }

        @Override
        public void loadConfiguration(ConfigurationSection section) {
                prefix = section.getString("Prefix", "#");
                ignore = section.getString("Ignore", "##");
                format = Util.replaceColorCodes(section.getString("Format", "&f[{server}]{sender}: {message}"));
        }

        @Override
        public void sendChat(final String sender, final String server, final String message) {
                if (!enabled) return;
                final String output = format.replaceAll("\\{server\\}", Matcher.quoteReplacement(server)).replaceAll("\\{sender\\}", Matcher.quoteReplacement(sender)).replaceAll("\\{message\\}", Matcher.quoteReplacement(message));
                plugin.getLogger().info(String.format("[%s][%s]%s: %s", server, name, sender, message));
                new BukkitRunnable() {
                        public void run() {
                                for (Player player : plugin.getServer().getOnlinePlayers()) {
                                        if (ignoreList.contains(player.getName())) continue;
                                        if (plugin.ignore.doesIgnore(player.getName(), sender)) continue;
                                        player.sendMessage(output);
                                }
                        }
                }.runTask(plugin);
        }

        @Override
        public String getName() {
                return name;
        }

        protected void onEvent(final Cancellable event, final Player player, final String message) {
                if (!enabled) return;
                if (ignore.equals(message)) {
                        if (ignoreList.remove(player.getName())) {
                                new BukkitRunnable() {
                                        public void run() {
                                                player.sendMessage("No longer ignoring channel");
                                        }
                                }.runTask(plugin);
                        } else {
                                ignoreList.add(player.getName());
                                new BukkitRunnable() {
                                        public void run() {
                                                player.sendMessage("Now ignoring channel");
                                        }
                                }.runTask(plugin);
                        }
                        event.setCancelled(true);
                } else if (message.startsWith(prefix)) {
                        String msg = message.substring(prefix.length());
                        WinLinkPlugin.getWinLink().broadcastPacket(new ChatPacket(player.getName(), name, msg));
                        sendChat(player.getName(), WinLinkPlugin.getWinLink().getServerName(), msg);
                        event.setCancelled(true);
                }
        }
}
