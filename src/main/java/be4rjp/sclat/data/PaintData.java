package be4rjp.sclat.data;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

/**
 * @author Be4rJP
 */
public class PaintData {

    private final Block paintedblock;

    private Match match;

    private Material origtype;

    private Team team;

    private BlockState bs;

    private BlockData blockData;


    public PaintData(Block block) {
        this.paintedblock = block;
    }

    public Match getMatch() {
        return this.match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Block getBlock() {
        return this.paintedblock;
    }

    public Material getOriginalType() {
        return this.origtype;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public BlockState getOriginalState() {
        return this.bs;
    }

    public void setOriginalState(BlockState bs) {
        this.bs = bs;
    }

    public BlockData getBlockData() {
        return this.blockData;
    }

    public void setBlockData(BlockData blockData) {
        this.blockData = blockData;
    }

    public void setOrigianlType(Material material) {
        this.origtype = material;
    }
}
