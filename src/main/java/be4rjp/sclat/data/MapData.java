package be4rjp.sclat.data;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Be4rJP
 */
public class MapData {
    private final String mapname;
    private final List<Path> path = new ArrayList<>();
    private final List<Area> areas = new ArrayList<>();
    private Location teamloc0;
    private Location teamloc1;
    private Location Intro;
    private boolean isUsed;
    private String worldname;
    private Location team0intro;
    private Location team1intro;
    private Location resultloc;
    private double intromovex;
    private double intromovey;
    private double intromovez;
    private boolean canpaintBBlock = false;
    private Location taikibasyo;
    private Location noBlockLoc;
    private boolean canAreaBattle = false;
    private WiremeshListTask wmlTask;

    private int VoidY = 0;


    public MapData(String mapname) {
        this.mapname = mapname;
    }

    public String getMapName() {
        return this.mapname;
    }

    public Location getIntro() {
        return this.Intro;
    }

    public void setIntro(Location l) {
        this.Intro = l;
    }

    public boolean isUsed() {
        return this.isUsed;
    }

    public String getWorldName() {
        return this.worldname;
    }

    public void setWorldName(String name) {
        this.worldname = name;
    }

    public Location getTeam0Loc() {
        return this.teamloc0;
    }

    public void setTeam0Loc(Location l) {
        this.teamloc0 = l;
    }

    public Location getTeam1Loc() {
        return this.teamloc1;
    }

    public void setTeam1Loc(Location l) {
        this.teamloc1 = l;
    }

    public Location getTeam0Intro() {
        return this.team0intro;
    }

    public void setTeam0Intro(Location l) {
        this.team0intro = l;
    }

    public Location getTeam1Intro() {
        return this.team1intro;
    }

    public void setTeam1Intro(Location l) {
        this.team1intro = l;
    }

    public Location getResultLoc() {
        return this.resultloc;
    }

    public void setResultLoc(Location l) {
        this.resultloc = l;
    }

    public double getIntroMoveX() {
        return this.intromovex;
    }

    public void setIntroMoveX(double x) {
        this.intromovex = x;
    }

    public double getIntroMoveY() {
        return this.intromovey;
    }

    public void setIntroMoveY(double y) {
        this.intromovey = y;
    }

    public double getIntroMoveZ() {
        return this.intromovez;
    }

    public void setIntroMoveZ(double z) {
        this.intromovez = z;
    }

    public Boolean canPaintBBlock() {
        return this.canpaintBBlock;
    }

    public Location getTaikibayso() {
        return this.taikibasyo;
    }

    public Location getNoBlockLocation() {
        return this.noBlockLoc;
    }

    public void setNoBlockLocation(Location loc) {
        this.noBlockLoc = loc;
    }

    public boolean getCanAreaBattle() {
        return this.canAreaBattle;
    }

    public void setCanAreaBattle(boolean is) {
        this.canAreaBattle = is;
    }

    public List<Path> getPathList() {
        return this.path;
    }

    public List<Area> getAreaList() {
        return this.areas;
    }

    public WiremeshListTask getWiremeshListTask() {
        return this.wmlTask;
    }

    public void setWiremeshListTask(WiremeshListTask wmlListTask) {
        this.wmlTask = wmlListTask;
    }

    public int getVoidY() {
        return this.VoidY;
    }

    public void setVoidY(int y) {
        this.VoidY = y;
    }

    public void setIsUsed(boolean used) {
        this.isUsed = used;
    }

    public void setCanPaintBBlock(boolean is) {
        this.canpaintBBlock = is;
    }

    public void setTaikibasyo(Location basyo) {
        this.taikibasyo = basyo;
    }

    public void addPath(Path path) {
        this.path.add(path);
    }

    public void addArea(Area area) {
        this.areas.add(area);
    }
}
