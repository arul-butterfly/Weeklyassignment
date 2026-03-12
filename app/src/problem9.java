import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class problem9 {

    // Transaction class
    static class Transaction {
        int id;
        double amount;
        String merchant;
        String account;
        LocalDateTime time;

        Transaction(int id, double amount, String merchant, String account, String timeStr) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            this.time = LocalDateTime.parse(timeStr, formatter);
        }

        @Override
        public String toString() {
            return "id:" + id + ", amount:" + amount + ", merchant:" + merchant + ", account:" + account;
        }
    }

    private List<Transaction> transactions = new ArrayList<>();

    // Add a transaction
    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // Classic Two-Sum: Find pairs that sum to target
    public List<List<Transaction>> findTwoSum(double target) {
        List<List<Transaction>> result = new ArrayList<>();
        Map<Double, Transaction> complementMap = new HashMap<>();
        for (Transaction t : transactions) {
            if (complementMap.containsKey(t.amount)) {
                result.add(Arrays.asList(complementMap.get(t.amount), t));
            } else {
                complementMap.put(target - t.amount, t);
            }
        }
        return result;
    }

    // Two-Sum with 1-hour time window
    public List<List<Transaction>> findTwoSumWithWindow(double target) {
        List<List<Transaction>> result = new ArrayList<>();
        transactions.sort(Comparator.comparing(t -> t.time));
        Map<Double, List<Transaction>> complementMap = new HashMap<>();

        for (Transaction t : transactions) {
            List<Transaction> matches = complementMap.getOrDefault(t.amount, new ArrayList<>());
            for (Transaction match : matches) {
                if (Math.abs(java.time.Duration.between(t.time, match.time).toMinutes()) <= 60) {
                    result.add(Arrays.asList(match, t));
                }
            }
            complementMap.computeIfAbsent(target - t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // K-Sum: Find K transactions that sum to target (recursive)
    public List<List<Transaction>> findKSum(int k, double target) {
        List<List<Transaction>> result = new ArrayList<>();
        findKSumHelper(transactions, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void findKSumHelper(List<Transaction> txs, int k, double target, int index,
                                List<Transaction> path, List<List<Transaction>> result) {
        if (k == 0 && Math.abs(target) < 0.0001) {
            result.add(new ArrayList<>(path));
            return;
        }
        if (k == 0 || index >= txs.size()) return;

        // Include current transaction
        path.add(txs.get(index));
        findKSumHelper(txs, k - 1, target - txs.get(index).amount, index + 1, path, result);
        path.remove(path.size() - 1);

        // Exclude current transaction
        findKSumHelper(txs, k, target, index + 1, path, result);
    }

    // Detect duplicates: same amount, same merchant, different accounts
    public List<Map<String, Object>> detectDuplicates() {
        Map<String, Map<Double, Set<String>>> merchantAmountMap = new HashMap<>();
        for (Transaction t : transactions) {
            merchantAmountMap.putIfAbsent(t.merchant, new HashMap<>());
            Map<Double, Set<String>> amountMap = merchantAmountMap.get(t.merchant);
            amountMap.putIfAbsent(t.amount, new HashSet<>());
            amountMap.get(t.amount).add(t.account);
        }

        List<Map<String, Object>> duplicates = new ArrayList<>();
        for (String merchant : merchantAmountMap.keySet()) {
            for (Map.Entry<Double, Set<String>> entry : merchantAmountMap.get(merchant).entrySet()) {
                if (entry.getValue().size() > 1) {
                    Map<String, Object> dup = new HashMap<>();
                    dup.put("amount", entry.getKey());
                    dup.put("merchant", merchant);
                    dup.put("accounts", entry.getValue());
                    duplicates.add(dup);
                }
            }
        }
        return duplicates;
    }

    // Example usage
    public static void main(String[] args) {
        problem9 fraudDetector = new problem9();

        fraudDetector.addTransaction(new Transaction(1, 500, "Store A", "acc1", "10:00"));
        fraudDetector.addTransaction(new Transaction(2, 300, "Store B", "acc2", "10:15"));
        fraudDetector.addTransaction(new Transaction(3, 200, "Store C", "acc3", "10:30"));
        fraudDetector.addTransaction(new Transaction(4, 500, "Store A", "acc2", "10:45"));

        System.out.println("Two-Sum for 500:");
        List<List<Transaction>> pairs = fraudDetector.findTwoSum(500);
        for (List<Transaction> pair : pairs) {
            System.out.println(pair);
        }

        System.out.println("\nTwo-Sum within 1 hour for 500:");
        List<List<Transaction>> windowPairs = fraudDetector.findTwoSumWithWindow(500);
        for (List<Transaction> pair : windowPairs) {
            System.out.println(pair);
        }

        System.out.println("\nK-Sum (k=3) for 1000:");
        List<List<Transaction>> ksum = fraudDetector.findKSum(3, 1000);
        for (List<Transaction> group : ksum) {
            System.out.println(group);
        }

        System.out.println("\nDuplicate detection:");
        List<Map<String, Object>> duplicates = fraudDetector.detectDuplicates();
        for (Map<String, Object> dup : duplicates) {
            System.out.println(dup);
        }
    }
}