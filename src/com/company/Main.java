package com.company;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        boolean isMainMenu = true;
        String[] card;
        String cardNumber;
        String pin;
        HashMap<String, String> cards = getCards();

        createTable();


        while(isMainMenu) {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            String action = scanner.nextLine();
            System.out.println();

            switch (action) {
                case "1":
                    card = createAccount();
                    cardNumber = card[0];
                    pin = card[1];
                    insertCard(cardNumber, pin);
                    cards = getCards();
                    if (cardNumber != null && pin != null) {
                        System.out.println("Your card has been created");
                        System.out.println("Your card number:");
                        System.out.println(cardNumber);
                        System.out.println("Your card PIN:");
                        System.out.println(pin);
                        System.out.println();
                    }
                    break;
                case "2":
                    System.out.println("Enter your card number:");
                    String insertedBankAccount = scanner.next();
                    System.out.println("Enter your PIN:");
                    String insertedPin = scanner.next();
                    System.out.println();
                    isMainMenu = logIn(cards, insertedBankAccount, insertedPin);
                    break;
                case "0":
                    System.out.println("Bye!");
                    isMainMenu = false;
                    break;
                default:
                    System.out.println("Try again!");
                    break;
            }
        }
    }

    public static String[] createAccount() {
        Random random = new Random();
        StringBuilder fullCode = new StringBuilder("400000");
        int bankInterValLen = (999999999 - 100000000) + 1;
        int pinInterValLen = (9999 - 1000) + 1;

        String lastCode = String.valueOf(random.nextInt(bankInterValLen) + 100000000);
        fullCode.append(lastCode);
        String checkSum = getCheckSum(fullCode);
        fullCode.append(checkSum);
        String pin = String.valueOf(random.nextInt(pinInterValLen) + 1000);
        return new String[]{ fullCode.toString(), pin};
    }

    public static String getCheckSum(StringBuilder fullCode) {
        String checkSum = "";
        int sum = 0;
        char[] tempArray = fullCode.toString().toCharArray();
        int[] codeArray = new int[tempArray.length];

        for (int i = 0; i < tempArray.length; i++) {
            codeArray[i] = Integer.parseInt(String.valueOf(tempArray[i]));
            if (i % 2 == 0) {
                codeArray[i] *= 2;
            }
            if (codeArray[i] > 9) {
                codeArray[i] = codeArray[i] - 9;
            }
            sum += codeArray[i];
        }

        for (int j = 0; j < 10; j++) {
            if ((sum + j) % 10 == 0) {
                checkSum = String.valueOf(j);
                break;
            }
        }
        return checkSum;
    }

    public static boolean logIn(HashMap<String, String> cards, String insertedCardNum, String insertedPin) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int balance = 0;
        boolean isMenu = false;
        boolean isMainMenu = true;
        for (String card: cards.keySet()) {
            if (card.equals(insertedCardNum) && cards.get(card).equals(insertedPin)) {
                System.out.println("You have successfully logged in!");
                isMenu = true;
                balance = getBalance(insertedCardNum, insertedPin);
                break;
            }
        }
        if (!isMenu) {
            System.out.println("Wrong card number or PIN! \n");
        }

        while(isMenu) {
            System.out.println();
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
            String action = scanner.nextLine();

            switch (action) {
                case "1":
                    System.out.println();
                    System.out.println("Balance:" + " " + balance);
                    break;
                case "2":
                    System.out.println();
                    System.out.println("Enter income:");
                    int income = scanner.nextInt();
                    addIncome(income, insertedCardNum);
                    System.out.println("Income was added!");
                    balance = getBalance(insertedCardNum, insertedPin);
                    break;
                case "3":
                    System.out.println();
                    System.out.println("Transfer");
                    System.out.println("Enter card number:");
                    String cardNum = scanner.nextLine();

                    boolean isExist = cardExist(cardNum);
                    boolean isValid = isValidCreditCardNumber(cardNum);
                    boolean isDifferent = checkCards(insertedCardNum, cardNum);
                    if (!isValid) {
                        System.out.println("Probably you made a mistake in the card number. Please try again!");
                        break;
                    } else  if (!isExist) {
                        System.out.println("Such a card does not exist.");
                        break;
                    } else if (!isDifferent) {
                        System.out.println("You can't transfer money to the same account!");
                        break;
                    }
                    System.out.println("Enter how much money you want to transfer:");
                    int transferMoney = scanner.nextInt();
                    boolean isEnough = isEnoughMoney(transferMoney, insertedCardNum);
                    if (!isEnough) {
                        System.out.println("Not enough money!");
                        break;
                    } else {
                        updateBalance(transferMoney, insertedCardNum);
                        addIncome(transferMoney, cardNum);
                        balance = getBalance(insertedCardNum, insertedPin);
                        System.out.println("Success!");
                    }
                    break;
                case "4":
                    System.out.println();
                    deleteAccount(insertedCardNum);
                    System.out.println("The account has been closed!");
                    isMenu = false;
                    break;
                case "5":
                    System.out.println();
                    System.out.println("You have successfully logged out! \n");
                    isMenu = false;
                    break;
                case "0":
                    System.out.println("Bye!");
                    isMenu = false;
                    isMainMenu = false;
                    break;
                default:
                    System.out.println("Try again!");
                    break;
            }
        }
        return isMainMenu;
    }

    public static boolean checkCards (String cardNum, String transferCardNum) {
        boolean isDifferent = true;
        if (cardNum.equals(transferCardNum)) {
            isDifferent = false;
        }
        return isDifferent;
    }

    public static Connection connect() {
        String url = "jdbc:sqlite:card.s3db";
//        String url = "jdbc:sqlite:C:/SQLite/bank.db";
        Connection connection = null;
        try {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl(url);
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public static HashMap<String, String> getCards() {
        String sql = "SELECT number, pin FROM card";
        HashMap<String, String> cardsMap= new HashMap<>();
        try (Connection connection = connect()) {
            Statement statement = connection.createStatement();
            ResultSet cards = statement.executeQuery(sql);
            while (cards.next()) {
                cardsMap.put(
                        cards.getString("number"),
                        cards.getString("pin")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cardsMap;
    }

    public static void insertCard(String cardNum, String pin) throws SQLException {
        String sql = "INSERT INTO card (number, pin) VALUES (?, ?);";
        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, cardNum);
            statement.setString(2, pin);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public static int getBalance(String cardNum, String pin) {
        String sql = "SELECT balance FROM card WHERE number = ? AND pin = ?";
        int balance = 0;
        try(Connection connection = connect()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, cardNum);
            preparedStatement.setString(2, pin);
            ResultSet balances = preparedStatement.executeQuery();
            while (balances.next()) {
                balance = balances.getInt("balance");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return balance;
    }

    public static void updateBalance(int transfer, String cardNum) throws SQLException {
        String sql = "UPDATE card SET balance = balance - ? WHERE number = ?";
        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, transfer);
            preparedStatement.setString(2, cardNum);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public static void addIncome(int income, String cardNum) throws SQLException {
        String sql =  "UPDATE card SET balance = balance + ? WHERE number = ?";
        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, income);
            preparedStatement.setString(2, cardNum);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public static void deleteAccount(String cardNum) throws SQLException {
        String sql = "DELETE FROM card WHERE number = ?";
        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, cardNum);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public static boolean isEnoughMoney(int transfer, String cardNum) {
        String sql = "SELECT balance FROM card WHERE number = ?";
        boolean isEnough = true;
        int balance = 0;

        try (Connection connection = connect()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, cardNum);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                balance = resultSet.getInt("balance");
            }
            if (transfer > balance) {
                isEnough = false;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return isEnough;
    }

    public static boolean cardExist(String cardNum) {
        String sql = "SELECT number FROM card";
        boolean isValid = false;
        ArrayList<String> cardsNumber = new ArrayList<>();

        try (Connection connection = connect()){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                cardsNumber.add(resultSet.getString("number"));
            }

            for (String card: cardsNumber) {
                if (cardNum.equals(card)) {
                    isValid = true;
                    break;
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return isValid;
    }

    public static boolean isValidCreditCardNumber(String cardNumber)
    {
        int[] cardIntArray = new int[cardNumber.length()];

        for (int i = 0;i < cardNumber.length(); i++) {
            char c = cardNumber.charAt(i);
            cardIntArray[i] = Integer.parseInt("" + c);
        }

        for ( int i = cardIntArray.length - 2; i >= 0; i = i - 2) {
            int num = cardIntArray[i];
            num = num * 2;
            if (num > 9) {
                num = num % 10 + num / 10;
            }
            cardIntArray[i] = num;
        }

        int sum = sumDigits(cardIntArray);

        if (sum % 10 == 0) {
            return true;
        }
        return false;
    }

    public static int sumDigits(int[] arr) {
        return Arrays.stream(arr).sum();
    }

    public static void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + "     id INTEGER PRIMARY KEY,\n"
                + "     number TEXT,\n"
                + "     pin TEXT,\n"
                + "     balance INTEGER DEFAULT 0"
                + ")";
        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
