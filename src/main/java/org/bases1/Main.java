package org.bases1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/recurrence";
    private static final String USER = "alodev";
    private static final String PASS = "contra123";

    //    parameters
    private static final int INIT_VALUE = 0;
    private static boolean useLock = false;
    private static int increment = 0;
    private static int decrement = 0;
    private static long incrementTime = 0;
    private static long decrementTime = 0;
    private static long runtime = 0;

    public static void main(String[] args) {
        initParameters();
        System.out.println("---------------- START ---------------");
        try (Connection connIncrementTask = getConnection(); Connection connDecrementTask = getConnection()) {
            Statement stmtIncrementTask = connIncrementTask.createStatement();
            Statement stmtDecrementTask = connDecrementTask.createStatement();

            stmtIncrementTask.executeUpdate("UPDATE movements SET counter = " + INIT_VALUE + " WHERE id = 1");

            Timer incrementTimer = new Timer();
            Timer decrementTimer = new Timer();

            TimerTask incrementTask = new TimerTask() {
                public void run() {
                    if (useLock) {
                        updateValueWithLock(stmtIncrementTask, connIncrementTask, increment, "+");
                    } else {
                        updateValue(stmtIncrementTask, increment, "+");
                    }
                    printMessage("Thread 1");

                }
            };

            TimerTask decrementTask = new TimerTask() {
                public void run() {
                    if (useLock) {
                        updateValueWithLock(stmtDecrementTask, connDecrementTask, decrement, "-");
                    } else {
                        updateValue(stmtDecrementTask, decrement, "-");
                    }
                    printMessage("Thread 2");
                }
            };

            incrementTimer.schedule(incrementTask, 0, incrementTime);
            decrementTimer.schedule(decrementTask, 0, decrementTime);
            Thread.sleep(runtime + 1000);
            incrementTimer.cancel();
            decrementTimer.cancel();
            System.out.println("---------------- END ---------------");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void updateValue(Statement statement, int value, String symbol) {
        try {
            statement.executeUpdate("UPDATE movements SET counter = counter " + symbol + value + " WHERE id = 1");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private static void updateValueWithLock(Statement statement, Connection connection, int value, String symbol) {
        try {
            connection.setAutoCommit(false);
            statement.executeQuery("SELECT counter FROM movements WHERE id = 1 FOR UPDATE");
            statement.executeUpdate("UPDATE movements SET counter = counter" + symbol + value + " WHERE id = 1");
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            try {
                connection.rollback();
            } catch (Exception rollbackException) {
                rollbackException.printStackTrace(System.err);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception finalException) {
                finalException.printStackTrace(System.err);
            }
        }
    }

    public static void initParameters() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Use lock?");
        useLock = scanner.nextBoolean();
        System.out.println("[Thread 1] Enter the value of the increment");
        increment = scanner.nextInt();
        System.out.println("[Thread 1] Enter the time between increment in milliseconds");
        incrementTime = scanner.nextLong();
        System.out.println("[Thread 2] Enter the value of the decrement");
        decrement = scanner.nextInt();
        System.out.println("[Thread 2] Enter the time between decrements in milliseconds");
        decrementTime = scanner.nextLong();
        System.out.println("Enter the test run time");
        runtime = scanner.nextLong();
    }

    private static void printMessage(String thread) {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        System.out.println(now.format(formatter) + " [" + thread + "] " + "Updated");
    }
}
