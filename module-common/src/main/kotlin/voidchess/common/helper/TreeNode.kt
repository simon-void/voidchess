package voidchess.common.helper

import java.util.SortedMap
import java.util.TreeMap


class TreeNode<T, K : Comparable<K>> private constructor(
    private var data: T,
    private val getKey: (T)->K
) : Comparable<TreeNode<T, K>> {
    private var childrenByData: SortedMap<K, TreeNode<T, K>> = TreeMap()

    val depth: Int by lazy {
        childrenByData.values.maxBy { treeNode -> treeNode.depth }?.let { treeNode -> treeNode.depth + 1 } ?: 0
    }

    val childData: List<T>
        get() = childrenByData.values.map { node: TreeNode<T, K> -> node.data }

    fun addChild(data: T): TreeNode<T, K> {
        val key = getKey(data)
        var childNode: TreeNode<T, K>? = childrenByData[key]
        if (childNode == null) {
            childNode = TreeNode(data, getKey)
            childrenByData[key] = childNode
        }
        return childNode
    }

    /**
     * @param data to be found in child nodes
     * @return childNode which contains the specified data, can be null if no such child exist
     */
    fun getChild(key: K): TreeNode<T, K>? {
        return childrenByData[key]
    }

    override fun compareTo(other: TreeNode<T, K>): Int {
        return getKey(data).compareTo(getKey(other.data))
    }

    companion object {
        fun <T, K : Comparable<K>> getRoot(rootValue: T, getKey: (T)->K): TreeNode<T, K> {
            return TreeNode(rootValue, getKey)
        }
    }
}
