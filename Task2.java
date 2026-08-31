

import java.util.Random;
import java.util.Scanner;

public class Task2{

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        char playAgain;

        System.out.println("======================================");
        System.out.println("      WELCOME TO GUESSING GAME");
        System.out.println("======================================");

        do {

            int randomNumber = random.nextInt(100) + 1; 
            int guess;
            int attempts = 0;

            System.out.println("\nI have generated a number between 1 and 100.");
            System.out.println("Can you guess it?");

            while (true) {

                System.out.print("Enter your guess: ");

                while (!sc.hasNextInt()) {
                    System.out.print("Invalid input! Enter a number: ");
                    sc.next();
                }

                guess = sc.nextInt();
                attempts++;

                if (guess < 1 || guess > 100) {
                    System.out.println("Please enter a number between 1 and 100.");
                } else if (guess < randomNumber) {
                    System.out.println("Too Low! Try Again.");
                } else if (guess > randomNumber) {
                    System.out.println("Too High! Try Again.");
                } else {
                    System.out.println("\nCongratulations!");
                    System.out.println("You guessed the correct number.");
                    System.out.println("Number = " + randomNumber);
                    System.out.println("Attempts = " + attempts);
                    break;
                }
            }

            System.out.print("\nDo you want to play again? (Y/N): ");
            playAgain = sc.next().charAt(0);

        } while (playAgain == 'Y' || playAgain == 'y');

        System.out.println("\nThank you for playing!");
    }
}

