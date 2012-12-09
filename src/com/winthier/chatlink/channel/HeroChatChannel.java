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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class HeroChatChannel implements Channel, Listener {
        private ChatLinkPlugin plugin;
        private String name;
        private String channelName;
        private String format;

        public HeroChatChannel(ChatLinkPlugin plugin, String name) {
                this.plugin = plugin;
                this.name = name;
        }

        public void enable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public void disable() {
        }

        public void loadConfiguration(ConfigurationSection section) {
                channelName = section.getString("Channel", "Global");
                format = Util.replaceColorCodes(section.getString("Format", "[{server}]{sender}: {message}"));
        }

        public void sendChat(String sender, String server, String message) {
                String msg = format.replaceAll("\\{server\\}", server).replaceAll("\\{sender\\}", sender).replaceAll("\\{message\\}", message);
                new HeroChatMessageTask(plugin, sender, msg, channelName).runTask(plugin);
        }

        public String getName() {
                return name;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onChannelChat(com.dthielke.herochat.ChannelChatEvent event) {
                if (event.getChannel().getName().equals(channelName)) {
                        WinLinkPlugin.getWinLink().broadcastPacket(new ChatPacket(event.getSender().getName(), name, event.getMessage()));
                }
        }
}

class HeroChatMessageTask extends BukkitRunnable {
        private ChatLinkPlugin plugin;
        private String sender;
        private String message;
        private String channelName;

        public HeroChatMessageTask(ChatLinkPlugin plugin, String sender, String message, String channelName) {
                this.plugin = plugin;
                this.sender = sender;
                this.message = message;
                this.channelName = channelName;
        }

        public void run() {
                com.dthielke.herochat.Channel channel = com.dthielke.herochat.Herochat.getChannelManager().getChannel(channelName);
                if (channel == null) {
                        plugin.getLogger().warning("HeroChat channel not found: `" + channelName + "'");
                        return;
                }
                for (com.dthielke.herochat.Chatter chatter : channel.getMembers()) {
                        Player player = chatter.getPlayer();
                        if (player == null) continue;
                        if (com.dthielke.herochat.Herochat.getChatterManager().getChatter(player).getIgnores().contains(sender.toLowerCase())) return;
                        player.sendMessage(message);
                }
        }
}
