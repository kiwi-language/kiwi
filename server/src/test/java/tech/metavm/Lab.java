package tech.metavm;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Scanner;

import java.util.Scanner;
import java.util.Scanner;

public class Lab {

    static @NotNull String s = "Hello";

    public static void main(String[] args) {
    }

    private static boolean isConsistent(String s) {
        int size = 0;  // Size of the array
        int toRemove = 0;  // Elements to remove to restore sorted order

        for (char ch : s.toCharArray()) {
            if (ch == '+') {
                size++;  // Add an element to the array
            } else if (ch == '-') {
                if (size == 0) {
                    return false;  // Cannot remove from an empty array
                }
                size--;  // Remove an element from the array
                if (toRemove > 0) {
                    toRemove--;
                }
            } else if (ch == '0') {
                if (size < 2) {
                    return false;  // An array with less than 2 elements should be sorted
                }
                toRemove = size;  // Set the number of elements to be removed to restore sorted order
            } else if (ch == '1') {
                if (toRemove > 0) {
                    return false;  // The array is unsorted and cannot be sorted without removing more elements
                }
            }
        }

        return true;
    }
}
