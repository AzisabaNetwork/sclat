
package be4rjp.sclat.api.rank;

import be4rjp.sclat.manager.PlayerStatusMgr;
import org.bukkit.entity.Player;

/**
 *
 * @author Be4rJP
 */
public class Ratings {

	private static final String[] ranks = {"E", "D-", "D", "D+", "C-", "C", "C+", "B-", "B", "B+", "A-", "A", "A+",
			"S-", "S", "S+", "MASTER"};
	private static final int MAX_RATE = (ranks.length - 1) * 500;

	// レートを500単位で区切ってランク付けする
	public static String toABCRank(int ir) {
		return ir >= 0 ? ranks[ir <= MAX_RATE ? ir / 500 : ranks.length - 1] : "UnRanked";
	}
	public static int IndicateRankPointmove(Player p, int rankPoint) {
		if (rankPoint == 0)
			return 0;

		int rank = PlayerStatusMgr.getRank(p);

		double rank_Rate = 1.0;

		if (rank < 500) {
			rank_Rate = 3.0;
		} else if (rank < 2000) {
			rank_Rate = 2.0;
		} else if (rank < 3500) {
			rank_Rate = 1.5;
		} else if (rank < 6500) {
			rank_Rate = 1.0;
		} else if (rank < 8000) {
			rank_Rate = 0.75;
		} else if (rank < 20000) {
			rank_Rate = 0.5;
		} else {
			rank_Rate = 0.2;
		}
		int plus = (int) ((double) rankPoint * rank_Rate);
		return plus;

	}
	public static void addPlayerRankPoint(String uuid, int rankPoint) {
		if (rankPoint == 0)
			return;

		int rank = PlayerStatusMgr.getRank(uuid);

		// int MAX_RATE = ranks.length * 500;

		double rank_Rate = 1.0;

		if (rank < 500) {
			rank_Rate = 3.0;
		} else if (rank < 2000) {
			rank_Rate = 2.0;
		} else if (rank < 3500) {
			rank_Rate = 1.5;
		} else if (rank < 6500) {
			rank_Rate = 1.0;
		} else if (rank < 8000) {
			rank_Rate = 0.75;
		} else if (rank < 20000) {
			rank_Rate = 0.5;
		} else {
			rank_Rate = 0.2;
		}

		int plus = (int) ((double) rankPoint * rank_Rate);
		if (plus > 0) {
			PlayerStatusMgr.addRank(uuid, plus);
		}
	}
}
