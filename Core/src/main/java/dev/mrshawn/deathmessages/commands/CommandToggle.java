package dev.mrshawn.deathmessages.commands;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.enums.Permission;
import dev.mrshawn.deathmessages.utils.Assets;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class CommandToggle extends DeathMessagesCommand {

	@Override
	public String command() {
		return "toggle";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player p)) {
			DeathMessages.getInstance().adventure().sender(sender).sendMessage(Assets.convertFromLegacy(Assets.formatMessage("Commands.DeathMessages.Player-Only-Command")));
			return;
		}
        if (!p.hasPermission(Permission.DEATHMESSAGES_COMMAND_TOGGLE.getValue())) {
			DeathMessages.getInstance().adventure().player(p).sendMessage(Assets.convertFromLegacy(Assets.formatMessage("Commands.DeathMessages.No-Permission")));
			return;
		}
		Optional<PlayerManager> getPlayer = PlayerManager.getPlayer(p);
		getPlayer.ifPresent(pm -> {
			boolean msg = pm.getMessagesEnabled();
			if (msg) {
				pm.setMessagesEnabled(false);
				DeathMessages.getInstance().adventure().player(p).sendMessage(Assets.convertFromLegacy(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Toggle.Toggle-Off")));
			} else {
				pm.setMessagesEnabled(true);
				DeathMessages.getInstance().adventure().player(p).sendMessage(Assets.convertFromLegacy(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Toggle.Toggle-On")));
			}
		});
	}
}