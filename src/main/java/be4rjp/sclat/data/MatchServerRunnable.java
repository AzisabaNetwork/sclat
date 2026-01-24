package be4rjp.sclat.data;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.MessageType;
import be4rjp.sclat.api.SclatUtil;
import be4rjp.sclat.api.SoundType;
import be4rjp.sclat.api.status.ServerStatus;
import be4rjp.sclat.manager.BungeeCordMgr;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchServerRunnable extends BukkitRunnable {

	private final ServerStatus serverStatus;

	private int waitTime = 0;

	public MatchServerRunnable(ServerStatus serverStatus) {
		this.serverStatus = serverStatus;

		SclatUtil.sendMessage(serverStatus.getDisplayName() + "§rの試合待機カウントダウンが開始されました", MessageType.ALL_PLAYER);
		SclatUtil.sendMessage("§a30秒後にマッチングを開始します", MessageType.ALL_PLAYER);
		Sclat.getPlugin().getServer().getOnlinePlayers()
				.forEach(player -> SclatUtil.playGameSound(player, SoundType.SUCCESS));
	}

	@Override
	public void run() {
		if (waitTime == 30) {
			for (Player player : Sclat.getPlugin().getServer().getOnlinePlayers()) {
				BungeeCordMgr.PlayerSendServer(player, serverStatus.getServerName());
				DataMgr.getPlayerData(player).setServerName(serverStatus.getDisplayName());
			}
		}
	}
}
