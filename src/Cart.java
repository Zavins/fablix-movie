import java.util.HashMap;
import java.util.Map;

public class Cart {
    public Map<String, Integer> getItems() {
        return items;
    }

    private final Map<String, Integer> items;

    public Cart() {
        items = new HashMap<>();
    }

    public void addOne(String id) {
        Integer curr = items.putIfAbsent(id, 1);
        if (curr != null) {
            items.put(id, curr + 1);
        }
    }

    public void removeOne(String id) {
        Integer curr = items.get(id);
        if (curr != null) {
            if (curr == 1) {
                items.remove(id);
            } else {
                items.put(id, curr - 1);
            }
        }
    }

    public void delete(String id) {
        items.remove(id);
    }
}
