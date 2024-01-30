import java.util.Scanner;

public class MonocarpQueries {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int t = scanner.nextInt();  // Reading number of test cases
        scanner.nextLine(); // Consume the remaining newline character

        for (int i = 0; i < t; i++) {
            String s = scanner.nextLine();
            System.out.println(isConsistent(s) ? "YES" : "NO");
        }

        scanner.close();
    }

    private static boolean isConsistent(String s) {
        int size = 0;  // Size of the array
        boolean lastCheckSorted = true; // Flag to indicate if the array was sorted in the last check

        for (char ch : s.toCharArray()) {
            if (ch == '+') {
                size++;  // Add an element to the array
            } else if (ch == '-') {
                if (size == 0) {
                    return false;  // Cannot remove from an empty array
                }
                size--;  // Remove an element from the array
            } else if (ch == '0') {
                if (size < 2) {
                    return false;  // An array with less than 2 elements should be sorted
                }
                if (size >= 2) {
                    lastCheckSorted = false; // Mark the array as potentially unsorted
                }
            } else if (ch == '1') {
                if (size < 2) {
                    lastCheckSorted = true; // An array with 0 or 1 element is always sorted
                } else if (!lastCheckSorted) {
                    return false;  // If the array was marked unsorted and has 2 or more elements, it's inconsistent
                }
            }
        }

        return true;
    }
}

