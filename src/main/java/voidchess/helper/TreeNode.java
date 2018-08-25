package voidchess.helper;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Created by stephan on 12.07.2015.
 */
public class TreeNode<T extends Comparable<T>> implements Comparable<TreeNode> {

    T data;
    SortedMap<T, TreeNode<T>> childrenByData;

    public static <T extends Comparable<T>> TreeNode<T> getRoot(Class<T> dataClass) {
        return new TreeNode<>(null);
    }

    private TreeNode(T data) {
        this.data = data;
        this.childrenByData = new TreeMap<T, TreeNode<T>>();
    }

    public TreeNode<T> addChild(T data) {
        TreeNode<T> childNode = childrenByData.get(data);
        if (childNode == null) {
            childNode = new TreeNode<>(data);
            childrenByData.put(data, childNode);
        }
        return childNode;
    }

    /**
     * @param data to be found in childnodes
     * @return childNode which contains the specified data, can be null if no such child exist
     */
    public TreeNode<T> getChild(T data) {
        return childrenByData.get(data);
    }

    public Stream<T> getChildData() {
        return childrenByData.values().stream().map(
                (TreeNode<T> node) -> node.data
        );
    }

    @Override
    public int compareTo(TreeNode o) {
        return data.compareTo((T) o.data);
    }

}
