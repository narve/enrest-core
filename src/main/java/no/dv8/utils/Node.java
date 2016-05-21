package no.dv8.utils;

import java.util.*;

public class Node<T> implements Iterable<T> {

    final Optional<T> data;
    final Node<T> parent;
    final List<Node<T>> children = new LinkedList<>();

    public Node(Node parent, T data) {
        this.data = Optional.ofNullable(data);
        this.parent = parent;
    }

    public Node<T> addChild(T childData) {
        Node<T> childNode = new Node<T>(parent, childData);
        children.add(childNode);
        return childNode;
    }

    @Override
    public Iterator<T> iterator() {
        return dfs(new ArrayList<>()).iterator();
    }

    public List<T> dfs(List<T> list) {
        children.forEach(child -> child.dfs(list));
        if (data.isPresent()) {
            list.add(data.get());
        }
        return list;
    }

}