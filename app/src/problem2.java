import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class problem2 {

    // Stock map: productId -> remaining stock
    private Map<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    // Waiting list: productId -> queue of userIds (FIFO)
    private Map<String, Queue<Integer>> waitingListMap = new ConcurrentHashMap<>();

    // Constructor (optional: initialize products)
    public problem2() {
        // Example product initialization
        addProduct("IPHONE15_256GB", 100);
    }

    // Add a product with stock count
    public void addProduct(String productId, int stockCount) {
        stockMap.put(productId, new AtomicInteger(stockCount));
        waitingListMap.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Check available stock in O(1)
    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return 0;
        return stock.get();
    }

    // Process purchase request safely
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";

        while (true) {
            int currentStock = stock.get();
            if (currentStock > 0) {
                // Attempt to decrement atomically
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
                // else: retry
            } else {
                // Stock exhausted, add to waiting list
                Queue<Integer> waitQueue = waitingListMap.get(productId);
                waitQueue.add(userId);
                return "Added to waiting list, position #" + waitQueue.size();
            }
        }
    }

    // Get waiting list for a product
    public List<Integer> getWaitingList(String productId) {
        Queue<Integer> waitQueue = waitingListMap.get(productId);
        if (waitQueue == null) return Collections.emptyList();
        return new ArrayList<>(waitQueue);
    }

    // Example usage
    public static void main(String[] args) {
        problem2 manager = new problem2();

        // Check stock
        System.out.println("Stock available: " + manager.checkStock("IPHONE15_256GB"));

        // Simulate purchases
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345)); // Success
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890)); // Success

        // Simulate stock depletion
        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", i);
        }

        // Purchase after stock exhausted
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999)); // Added to waiting list

        // View waiting list
        System.out.println("Waiting list: " + manager.getWaitingList("IPHONE15_256GB"));
    }
}