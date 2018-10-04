package voidchess.helper

import java.util.SortedMap
import java.util.TreeMap
import java.util.stream.Stream


class TreeNode<T : Comparable<T>> private constructor(var data: T) : Comparable<TreeNode<T>> {
    private var childrenByData: SortedMap<T, TreeNode<T>> = TreeMap()

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
