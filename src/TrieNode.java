import java.util.*;

public class TrieNode {
    ArrayList<Road> roads = new ArrayList<>();
    Character letter;
    private HashMap<Character, TrieNode> children = new HashMap<>();


    public void add(String word,Road road, TrieNode trie) {
        TrieNode root = trie;
        for (Character c : word.toCharArray()) {
            if (!root.children.keySet().contains(c)) {
                TrieNode node = new TrieNode();
                root.children.put(c, node);
            }
            root = root.children.get(c);
        }
        root.roads.add(road);
    }

    public ArrayList<Road> get(String word, TrieNode origin) {
        TrieNode node = origin;
        for (Character c : word.toCharArray()) {
            if (node.children.keySet().contains(c)) {
                node = node.children.get(c);
            } else {
                return null;
            }
        }
        return node.roads;
    }

    public List<Road> getAll(String word, TrieNode origin) {
        List<Road> results = new ArrayList<>();
        TrieNode node = origin;
        for (Character c : word.toCharArray()) {
            if (!node.children.keySet().contains(c)) {
                return null;
            }
            node = node.children.get(c);
        }
        getAllFrom(node, results);
        System.out.println(results.size());
        return results;
    }

    public void getAllFrom(TrieNode node, List<Road> results) {
        if(node.roads!=null){
            results.addAll(node.roads);
        }
        for(Character c: node.children.keySet()){
            getAllFrom(node.children.get(c),results);
        }
    }
}
