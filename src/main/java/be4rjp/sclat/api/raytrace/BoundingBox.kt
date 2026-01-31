package be4rjp.sclat.api.raytrace

import net.minecraft.server.v1_14_R1.AxisAlignedBB
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

class BoundingBox {
    var max: Vector
    var min: Vector

    constructor(min: Vector, max: Vector) {
        this.max = max
        this.min = min
    }

    constructor(entity: Entity) {
        val bb = (entity as CraftEntity).handle.boundingBox
        min = Vector(bb.minX - 0.15, bb.minY, bb.minZ - 0.15)
        max = Vector(bb.maxX + 0.15, bb.maxY, bb.maxZ + 0.15)
    }

    constructor(bb: AxisAlignedBB) {
        min = Vector(bb.minX, bb.minY, bb.minZ)
        max = Vector(bb.maxX, bb.maxY, bb.maxZ)
    }

    fun midPoint(): Vector {
        return max.clone().add(min).multiply(0.5)
    }
}
