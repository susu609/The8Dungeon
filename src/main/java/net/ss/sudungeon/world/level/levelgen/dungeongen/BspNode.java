/*
package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

*/
/**
 * Represents a node in a Binary Space Partitioning (BSP) tree used for level generation in a game.
 * Each node represents a rectangular area in the game world.
 *//*

public class BspNode {
    private final int x, y, z, width, height;
    private BspNode leftChild, rightChild;

    */
/**
     * Constructs a new BspNode with the given start position and dimensions.
     *
     * @param startPos The starting position of the node in the game world.
     * @param width The width of the node's rectangular area.
     * @param height The height of the node's rectangular area.
     *//*

    public BspNode (BlockPos startPos, int width, int height) {
        this.x = startPos.getX();
        this.y = startPos.getY();
        this.z = startPos.getZ();
        this.width = width;
        this.height = height;
        this.leftChild = null;
        this.rightChild = null;
    }

    // Constructor, getters, setters...

    */
/**
     * Splits the current node into two child nodes based on the given parameters.
     *
     * @param random The random source for generating random values.
     * @param minWidth The minimum width for a child node.
     * @param minHeight The minimum height for a child node.
     * @param splitRatioX The ratio at which to split the node along the X-axis.
     * @param splitRatioZ The ratio at which to split the node along the Z-axis.
     *//*

    public void split (RandomSource random, int minWidth, int minHeight, float splitRatioX, float splitRatioZ) {
        if (width > minWidth && height > minHeight) {
            boolean splitX = random.nextBoolean(); // Chọn ngẫu nhiên trục chia

            if (splitX) {
                int splitPointX = (int) (x + width * splitRatioX);
                leftChild = new BspNode(new BlockPos(x, y, z), splitPointX - x, height);
                rightChild = new BspNode(new BlockPos(splitPointX, y, z), x + width - splitPointX, height);
            } else {
                int splitPointZ = (int) (z + height * splitRatioZ);
                leftChild = new BspNode(new BlockPos(x, y, z), width, splitPointZ - z);
                rightChild = new BspNode(new BlockPos(x, y, splitPointZ), width, z + height - splitPointZ);
            }
        }
    }

    */
/**
     * Returns the X-coordinate of the node's starting position.
     *
     * @return The X-coordinate of the node's starting position.
     *//*

    public int getX () {
        return x;
    }

    */
/**
     * Returns the Y-coordinate of the node's starting position.
     *
     * @return The Y-coordinate of the node's starting position.
     *//*

    public int getY () {
        return y;
    }

    */
/**
     * Returns the Z-coordinate of the node's starting position.
     *
     * @return The Z-coordinate of the node's starting position.
     *//*

    public int getZ () {
        return z;
    }

    */
/**
     * Returns the width of the node's rectangular area.
     *
     * @return The width of the node's rectangular area.
     *//*

    public int getWidth () {
        return width;
    }

    */
/**
     * Returns the height of the node's rectangular area.
     *
     * @return The height of the node's rectangular area.
     *//*

    public int getHeight () {
        return height;
    }

    */
/**
     * Returns the left child node of the current node.
     *
     * @return The left child node or null if the current node is a leaf node.
     *//*

    public BspNode getLeft () {
        return leftChild;
    }

    */
/**
     * Returns the right child node of the current node.
     *
     * @return The right child node or null if the current node is a leaf node.
     *//*

    public BspNode getRight () {
        return rightChild;
    }

    */
/**
     * Checks if the current node is a leaf node (i.e., it has no child nodes).
     *
     * @return True if the current node is a leaf node, false otherwise.
     *//*

    public boolean isLeaf () {
        return leftChild == null && rightChild == null;
    }
}
*/
