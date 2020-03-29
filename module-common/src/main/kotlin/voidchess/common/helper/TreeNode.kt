package voidchess.common.helper

import java.util.SortedMap
import java.util.TreeMap


class TreeNode<T : Comparable<T>> private constructor(private var data: T) : Comparable<TreeNode<T>> {
    private var childrenByData: SortedMap<T, TreeNode<T>> = TreeMap()

    val depth: Int by lazy {
        childrenByData.values.maxBy { treeNode -> treeNode.depth }?.let { treeNode -> treeNode.depth + 1 } ?: 0
    }

    val childData: List<T>
        get() = childrenByData.values.map { node: TreeNode<T> -> node.data }

    fun addChild(data: T): TreeNode<T> {
        var childNode: TreeNode<T>? = childrenByData[data]
        if (childNode == null) {
            childNode = TreeNode(data)
            childrenByData[data] = childNode
        }
        return childNode
    }

    /**
     * @param data to be found in child nodes
     * @return childNode which contains the specified data, can be null if no such child exist
     */
    fun getChild(data: T): TreeNode<T>? {
        return childrenByData[data]
    }

    override fun compareTo(other: TreeNode<T>): Int {
        return data.compareTo(other.data)
    }

    companion object {

        fun <T : Comparable<T>> getRoot(rootValue: T): TreeNode<T> {
            return TreeNode(rootValue)
        }
    }
}
