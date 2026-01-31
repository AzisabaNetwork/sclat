package be4rjp.sclat.api.raytrace

import org.bukkit.util.Vector

class RayTrace(var origin: Vector, var direction: Vector) {
    fun getPostion(blocksAway: Double): Vector {
        return origin.clone().add(direction.clone().multiply(blocksAway))
    }

    fun isOnLine(position: Vector): Boolean {
        val t = (position.getX() - origin.getX()) / direction.getX()
        return position.blockY.toDouble() == origin.getY() + (t * direction.getY()) &&
            position.blockZ.toDouble() == origin.getZ() + (t * direction.getZ())
    }

    fun traverse(blocksAway: Double, accuracy: Double): ArrayList<Vector> {
        val positions = ArrayList<Vector>()
        var d = 0.0
        while (d <= blocksAway) {
            positions.add(getPostion(d))
            d += accuracy
        }
        return positions
    }

    fun positionOfIntersection(min: Vector, max: Vector, blocksAway: Double, accuracy: Double): Vector? {
        val positions = traverse(blocksAway, accuracy)
        for (position in positions) {
            if (intersects(position, min, max)) {
                return position
            }
        }
        return null
    }

    fun intersects(min: Vector, max: Vector, blocksAway: Double, accuracy: Double): Boolean {
        val positions = traverse(blocksAway, accuracy)
        for (position in positions) {
            if (intersects(position, min, max)) {
                return true
            }
        }
        return false
    }

    fun positionOfIntersection(boundingBox: BoundingBox, blocksAway: Double, accuracy: Double): Vector? {
        val positions = traverse(blocksAway, accuracy)
        for (position in positions) {
            if (intersects(position, boundingBox.min, boundingBox.max)) {
                return position
            }
        }
        return null
    }

    fun intersects(boundingBox: BoundingBox, blocksAway: Double, accuracy: Double): Boolean {
        val positions = traverse(blocksAway, accuracy)
        for (position in positions) {
            if (intersects(position, boundingBox.min, boundingBox.max)) {
                return true
            }
        }
        return false
    }

    companion object {
        fun intersects(position: Vector, min: Vector, max: Vector): Boolean {
            if (position.getX() < min.getX() || position.getX() > max.getX()) {
                return false
            } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
                return false
            } else {
                return !(position.getZ() < min.getZ()) && !(position.getZ() > max.getZ())
            }
        }
    }
}
