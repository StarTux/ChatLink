/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012-2014 StarTux
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

import com.winthier.chatlink.packet.WhisperPacket;
import com.winthier.winlink.WinLink;
import com.winthier.winlink.WinLinkPlugin;
import java.util.Collections;
import java.util.regex.Matcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class WhisperCommand implements CommandExecutor {
    private ChatLinkPlugin plugin;
    public String senderFormat;
    public String recipientFormat;

    public WhisperCommand(ChatLinkPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("xmsg").setExecutor(this);
        plugin.getCommand("xtell").setExecutor(this);
        plugin.getCommand("xwhisper").setExecutor(this);
    }
        
    public void loadConfiguration() {
        senderFormat = Util.replaceColorCodes(plugin.getConfig().getString("whisper.SenderFormat"));
        recipientFormat = Util.replaceColorCodes(plugin.getConfig().getString("whisper.RecipientFormat"));
    }

    public void msgSender(Player sender, String recipient, String server, String message) {
        server = WinLinkPlugin.getInstance().getDisplayName(server);
        plugin.getLogger().info(String.format("%s -> %s(%s): %s", sender.getName(), recipient, server, message));
        sender.sendMessage(senderFormat.replaceAll("\\{sender\\}", Matcher.quoteReplacement(sender.getName())).replaceAll("\\{recipient\\}", Matcher.quoteReplacement(recipient)).replaceAll("\\{server\\}", Matcher.quoteReplacement(server)).replaceAll("\\{message\\}", Matcher.quoteReplacement(message)));
    }

    public boolean msgRecipient(Player recipient, String sender, String server, String message) {
        server = WinLinkPlugin.getInstance().getDisplayName(server);
        if (plugin.ignore.doesIgnore(recipient.getName(), sender)) return false;
        plugin.getLogger().info(String.format("%s(%s) -> %s: %s", sender, server, recipient.getName(), message));
        recipient.sendMessage(recipientFormat.replaceAll("\\{sender\\}", Matcher.quoteReplacement(sender)).replaceAll("\\{recipient\\}", Matcher.quoteReplacement(recipient.getName())).replaceAll("\\{server\\}", Matcher.quoteReplacement(server)).replaceAll("\\{message\\}", Matcher.quoteReplacement(message)));
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (args.length < 2) return false;
        String recipient = args[0];
        StringBuilder sb = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
        String message = sb.toString();
        // if (sender instanceof Player) {
        //     AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, (Player)sender, "", Collections.<Player>singleton((Player)sender));
        //     plugin.getServer().getPluginManager().callEvent(event);
        //     if (event.isCancelled()) return true;
        // }
        Player receiver = plugin.getServer().getPlayer(recipient);
        if (receiver == null) {
            WinLinkPlugin.getWinLink().broadcastPacket(new WhisperPacket(sender.getName(), recipient, message));
        } else {
            plugin.repliers.put(receiver.getName(), sender.getName());
            String serverName = WinLinkPlugin.getInstance().getWinLink().getServerName();
            if (player != null) msgSender(player, receiver.getName(), serverName, message);
            msgRecipient(receiver, sender.getName(), serverName, message);
        }
        return true;
    }
}
