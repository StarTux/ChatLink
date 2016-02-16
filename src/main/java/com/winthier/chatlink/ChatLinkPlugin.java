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

package com.winthier.chatlink;

import com.winthier.chatlink.channel.AsyncChatChannel;
import com.winthier.chatlink.channel.Channel;
import com.winthier.chatlink.channel.HeroChatChannel;
import com.winthier.chatlink.ignore.HeroChatIgnore;
import com.winthier.chatlink.ignore.IgnoreBackend;
import com.winthier.chatlink.ignore.NullIgnore;
import com.winthier.chatlink.packet.ChatPacket;
import com.winthier.chatlink.packet.Packet;
import com.winthier.chatlink.packet.WhisperAckPacket;
import com.winthier.chatlink.packet.WhisperPacket;
import com.winthier.connect.Connect;
import com.winthier.connect.Message;
import com.winthier.connect.bukkit.event.ConnectMessageEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatLinkPlugin extends JavaPlugin implements Listener {
    Map<String, Channel> channels = Collections.synchronizedMap(new HashMap<>());
    Map<String, String> repliers = Collections.synchronizedMap(new HashMap<>());
    private BukkitRunnable task;
    private WhisperCommand whisperCommand;
    private ReplyCommand replyCommand;
    public IgnoreBackend ignore;
    public net.milkbowl.vault.chat.Chat vaultChat;
    public net.milkbowl.vault.permission.Permission vaultPerm;

    @Override
    public void onEnable() {
        setupChat();
        setupPermissions();
        if (getServer().getPluginManager().getPlugin("Herochat") != null) ignore = new HeroChatIgnore();
        else ignore = new NullIgnore();
        whisperCommand = new WhisperCommand(this);
        replyCommand = new ReplyCommand(this);
        saveDefaultConfig();
        loadConfiguration();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<net.milkbowl.vault.chat.Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            vaultChat = chatProvider.getProvider();
        }
        return (vaultChat != null);
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            vaultPerm = permissionProvider.getProvider();
        }
        return (vaultPerm != null);
    }

    private net.milkbowl.vault.chat.Chat getChat() {
        return vaultChat;
    }

    public String getPrefix(String player) {
        try {
            if (getChat() == null) return "";
            String worldName = getServer().getWorlds().get(0).getName();
            return getChat().getPlayerPrefix(worldName, player);
        } catch (NoClassDefFoundError cnfe) {
            return "";
        }
    }

    public String getSuffix(String player) {
        try {
            if (getChat() == null) return "";
            String worldName = getServer().getWorlds().get(0).getName();
            return getChat().getPlayerSuffix(worldName, player);
        } catch (NoClassDefFoundError cnfe) {
            return "";
        }
    }

    @Override
    public void onDisable() {
        clearChannels();
        ignore = null;
    }

    private void clearChannels() {
        synchronized(channels) {
            for (Channel channel : channels.values()) {
                channel.disable();
            }
        }
        channels.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String token, String args[]) {
        if (args.length == 1 && args[0].equals("reload")) {
            try {
                clearChannels();
                reloadConfig();
                loadConfiguration();
                sender.sendMessage("Configuration reloaded.");
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("An error occured. See console.");
            }
        } else {
            sender.sendMessage("Usage: /chatlink [subcommand] ...");
            sender.sendMessage("Subcommands: reload");
        }
        return true;
    }

    @EventHandler
    public void onConnectMessage(ConnectMessageEvent event) {
        final Message message = event.getMessage();
        if (!message.getChannel().equals("ChatLink")) return;
        Map<String, String> map = (Map<String, String>)message.getPayload();
        String messageType = map.get("type");
        if (messageType.equals("Chat")) {
            ChatPacket packet = ChatPacket.deserialize(map);
            Channel channel = channels.get(packet.channel);
            if (channel == null) {
                //getLogger().warning("Channel not found: `" + packet.channel + "'");
                return;
            }
            String msg = packet.message;
            if (vaultPerm != null && vaultPerm.has(getServer().getWorlds().get(0), packet.sender, "chatlink.colors")) {
                msg = ChatColor.translateAlternateColorCodes('&', msg);
            }
            channel.sendChat(packet.sender, message.getFrom(), msg);
        } else if (messageType.equals("Whisper")) {
            final WhisperPacket packet = WhisperPacket.deserialize(map);
            new BukkitRunnable() {
                public void run() {
                    Player player = getServer().getPlayer(packet.recipient);
                    if (player == null) {
                        return;
                    }
                    repliers.put(player.getName(), packet.sender);
                    if (whisperCommand.msgRecipient(player, packet.sender, message.getFrom(), packet.message)) {
                        Connect.getInstance().send(message.getFrom(), "ChatLink", new WhisperAckPacket(player.getName(), packet.sender, packet.message).serialize());
                    }
                }
            }.runTask(this);
        } else if (messageType.equals("WhisperAck")) {
            final WhisperAckPacket packet = WhisperAckPacket.deserialize(map);
            new BukkitRunnable() {
                public void run() {
                    try {
                        repliers.put(packet.recipient, packet.sender);
                        Player player = getServer().getPlayer(packet.sender);
                        if (player == null) return;
                        whisperCommand.msgSender(player, packet.recipient, message.getFrom(), packet.message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTask(this);
        }
    }

    public void loadConfiguration() {
        synchronized (channels) { for (Channel channel : channels.values()) channel.disable(); }
        whisperCommand.loadConfiguration();
        channels.clear();
        ConfigurationSection channelsSection = getConfig().getConfigurationSection("channels");
        if (channelsSection == null) {
            channelsSection = getConfig().createSection("channels");
        }
        for (String name : channelsSection.getKeys(false)) {
            ConfigurationSection channelSection = channelsSection.getConfigurationSection(name);
            String channelType = channelSection.getString("Type", "default");
            Channel channel;
            if (channelType.equalsIgnoreCase("default")) {
                channel = new AsyncChatChannel(this, name);
            } else if (channelType.equalsIgnoreCase("async")) {
                channel = new AsyncChatChannel(this, name);
            } else if (channelType.equalsIgnoreCase("HeroChat")) {
                channel = new HeroChatChannel(this, name);
            } else {
                getLogger().warning("config.yml: channel `" + name + "': invalid type: `" + channelType + "'");
                continue;
            }
            channel.enable();
            channel.loadConfiguration(channelSection);
            channels.put(name, channel);
        }
    }

    public void broadcastMessage(Packet packet) {
        Connect.getInstance().broadcast("ChatLink", packet.serialize());
    }
}
