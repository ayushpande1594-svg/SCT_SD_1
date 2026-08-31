import java.util.Scanner;

public class Task1 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int choice;
        double temp, result;

        do {
            System.out.println("\n========== Temperature Converter ==========");
            System.out.println("1. Celsius to Fahrenheit");
            System.out.println("2. Celsius to Kelvin");
            System.out.println("3. Fahrenheit to Celsius");
            System.out.println("4. Fahrenheit to Kelvin");
            System.out.println("5. Kelvin to Celsius");
            System.out.println("6. Kelvin to Fahrenheit");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    System.out.print("Enter temperature in Celsius: ");
                    temp = sc.nextDouble();
                    result = (temp * 9 / 5) + 32;
                    System.out.printf("Temperature in Fahrenheit: %.2f °F%n", result);
                    break;

                case 2:
                    System.out.print("Enter temperature in Celsius: ");
                    temp = sc.nextDouble();
                    result = temp + 273.15;
                    System.out.printf("Temperature in Kelvin: %.2f K%n", result);
                    break;

                case 3:
                    System.out.print("Enter temperature in Fahrenheit: ");
                    temp = sc.nextDouble();
                    result = (temp - 32) * 5 / 9;
                    System.out.printf("Temperature in Celsius: %.2f °C%n", result);
                    break;

                case 4:
                    System.out.print("Enter temperature in Fahrenheit: ");
                    temp = sc.nextDouble();
                    result = (temp - 32) * 5 / 9 + 273.15;
                    System.out.printf("Temperature in Kelvin: %.2f K%n", result);
                    break;

                case 5:
                    System.out.print("Enter temperature in Kelvin: ");
                    temp = sc.nextDouble();

                    if (temp < 0) {
                        System.out.println("Invalid! Kelvin temperature cannot be negative.");
                    } else {
                        result = temp - 273.15;
                        System.out.printf("Temperature in Celsius: %.2f °C%n", result);
                    }
                    break;

                case 6:
                    System.out.print("Enter temperature in Kelvin: ");
                    temp = sc.nextDouble();

                    if (temp < 0) {
                        System.out.println("Invalid! Kelvin temperature cannot be negative.");
                    } else {
                        result = (temp - 273.15) * 9 / 5 + 32;
                        System.out.printf("Temperature in Fahrenheit: %.2f °F%n", result);
                    }
                    break;

                case 7:
                    System.out.println("Thank you for using Temperature Converter!");
                    break;

                default:
                    System.out.println("Invalid choice! Please select a valid option.");
            }

        } while (choice != 7);
    }
}
