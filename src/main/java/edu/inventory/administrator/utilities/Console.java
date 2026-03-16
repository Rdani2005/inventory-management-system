package edu.inventory.administrator.utilities;

import java.util.Scanner;

public class Console {
    private final Scanner scanner = new Scanner(System.in);

    public void header(String title) {
        System.out.println();
        System.out.println("==============================");
        System.out.println(title);
        System.out.println("==============================");
    }

    public String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine();
    }

    public int promptInt(String label) {
        while (true) {
            try {
                System.out.print(label);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException ex) {
                err("Please enter a valid integer.");
            }
        }
    }

    public void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public void ok(String message) {
        System.out.println("[OK] " + message);
    }

    public void err(String message) {
        System.out.println("[ERROR] " + message);
    }
}
