import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class problem8 {

    private static class Vehicle {
        String licensePlate;
        LocalDateTime entryTime;

        Vehicle(String licensePlate) {
            this.licensePlate = licensePlate;
            this.entryTime = LocalDateTime.now();
        }
    }

    private enum SpotStatus { EMPTY, OCCUPIED, DELETED }

    private static class Spot {
        SpotStatus status = SpotStatus.EMPTY;
        Vehicle vehicle = null;
        int probeCount = 0;
    }

    private final int CAPACITY = 500;
    private Spot[] spots = new Spot[CAPACITY];

    // Statistics
    private int totalProbes = 0;
    private int totalParkedVehicles = 0;
    private Map<Integer, Integer> occupancyPerHour = new HashMap<>();

    public problem8() {
        for (int i = 0; i < CAPACITY; i++) {
            spots[i] = new Spot();
        }
    }

    // Hash function based on license plate
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % CAPACITY;
    }

    // Park a vehicle using linear probing
    public String parkVehicle(String licensePlate) {
        int preferred = hash(licensePlate);
        int probe = 0;
        while (probe < CAPACITY) {
            int spotIndex = (preferred + probe) % CAPACITY;
            Spot spot = spots[spotIndex];
            if (spot.status == SpotStatus.EMPTY || spot.status == SpotStatus.DELETED) {
                spot.status = SpotStatus.OCCUPIED;
                spot.vehicle = new Vehicle(licensePlate);
                spot.probeCount = probe;
                totalProbes += probe;
                totalParkedVehicles++;
                updateOccupancy();
                return "Assigned spot #" + spotIndex + " (" + probe + " probes)";
            }
            probe++;
        }
        return "Parking full! Could not find a spot.";
    }

    // Vehicle exit
    public String exitVehicle(String licensePlate) {
        for (int i = 0; i < CAPACITY; i++) {
            Spot spot = spots[i];
            if (spot.status == SpotStatus.OCCUPIED && spot.vehicle.licensePlate.equals(licensePlate)) {
                LocalDateTime entry = spot.vehicle.entryTime;
                LocalDateTime exit = LocalDateTime.now();
                Duration duration = Duration.between(entry, exit);
                double hours = duration.toMinutes() / 60.0;
                double fee = calculateFee(hours);
                spot.status = SpotStatus.DELETED;
                spot.vehicle = null;
                totalParkedVehicles--;
                updateOccupancy();
                return String.format("Spot #%d freed, Duration: %dh %dm, Fee: $%.2f",
                        i, duration.toHours(), duration.toMinutesPart(), fee);
            }
        }
        return "Vehicle not found!";
    }

    private double calculateFee(double hours) {
        double ratePerHour = 5.0; // $5 per hour
        return Math.ceil(hours) * ratePerHour;
    }

    private void updateOccupancy() {
        int hour = LocalDateTime.now().getHour();
        occupancyPerHour.put(hour, totalParkedVehicles);
    }

    // Generate statistics
    public String getStatistics() {
        double occupancy = totalParkedVehicles * 100.0 / CAPACITY;
        double avgProbes = CAPACITY == 0 ? 0 : (totalProbes * 1.0 / CAPACITY);
        int peakHour = occupancyPerHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(-1);
        return String.format("Occupancy: %.1f%%, Avg Probes: %.2f, Peak Hour: %d",
                occupancy, avgProbes, peakHour);
    }

    // Example usage
    public static void main(String[] args) throws InterruptedException {
        problem8 parkingLot = new problem8();

        System.out.println(parkingLot.parkVehicle("ABC-1234"));
        System.out.println(parkingLot.parkVehicle("ABC-1235"));
        System.out.println(parkingLot.parkVehicle("XYZ-9999"));

        Thread.sleep(2000); // simulate time passing

        System.out.println(parkingLot.exitVehicle("ABC-1234"));
        System.out.println(parkingLot.getStatistics());
    }
}