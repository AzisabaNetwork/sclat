package be4rjp.sclat.data;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

/**
 * @author Be4rJP
 */
public class Match {
    private final int id;
    private World world;
    private Team team0;
    private Team team1;
    private int playercount = 0;
    private MapData map;
    private boolean canjoin = true;
    private Player leader;
    private int c_nawabari = 0;
    private int c_tdm = 0;
    private int c_ga = 0;
    private boolean finished = false;
    private BlockUpdater bur;
    private Team winteam;
    private boolean hikiwake = false;
    private Scoreboard scoreboard;
    private boolean started = false;
    private int joinedPlayerCount = 0;
    private boolean isStartedCount = false;


    public Match(int id) {
        this.id = id;
    }

    public World getWorld() {
        return world;
    }

    public Team getTeam0() {
        return team0;
    }

    public void setTeam0(Team team) {
        team0 = team;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team) {
        team1 = team;
    }

    public int getPlayerCount() {
        return playercount;
    }

    public MapData getMapData() {
        return this.map;
    }

    public void setMapData(MapData map) {
        this.map = map;
    }

    public boolean canJoin() {
        return this.canjoin;
    }

    public Player getLeaderPlayer() {
        return this.leader;
    }

    public void setLeaderPlayer(Player player) {
        this.leader = player;
    }

    public int getNawabari_T_Count() {
        return this.c_nawabari;
    }

    public int getTDM_T_Count() {
        return this.c_tdm;
    }

    public int getGatiArea_T_Count() {
        return this.c_ga;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public BlockUpdater getBlockUpdater() {
        return this.bur;
    }

    public void setBlockUpdater(BlockUpdater bur) {
        this.bur = bur;
    }

    public Team getWinTeam() {
        return this.winteam;
    }

    public void setWinTeam(Team team) {
        this.winteam = team;
    }

    public boolean getIsHikiwake() {
        return this.hikiwake;
    }

    public void setIsHikiwake(boolean is) {
        this.hikiwake = is;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void setScoreboard(Scoreboard sc) {
        this.scoreboard = sc;
    }

    public boolean getIsStarted() {
        return this.started;
    }

    public void setIsStarted(boolean is) {
        this.started = is;
    }

    public int getJoinedPlayerCount() {
        return this.joinedPlayerCount;
    }

    public boolean isStartedCount() {
        return this.isStartedCount;
    }

    public void addPlayerCount() {
        playercount++;
    }

    public void setCanJoin(boolean is) {
        this.canjoin = is;
    }

    public void setIsFinished(boolean is) {
        this.finished = is;
    }

    public void setIsStartedCount(boolean is) {
        this.isStartedCount = is;
    }


    public void addNawabari_T_Count() {
        this.c_nawabari++;
    }

    public void addTDM_T_Count() {
        this.c_tdm++;
    }

    public void addGatiArea_T_Count() {
        this.c_ga++;
    }

    public void addJoinedPlayerCount() {
        this.joinedPlayerCount++;
    }

    public void subPlayerCount() {
        this.playercount--;
    }

    public void subJoinedPlayerCount() {
        this.joinedPlayerCount--;
    }
}
