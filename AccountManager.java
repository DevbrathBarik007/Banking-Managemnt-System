package Banking_Management_Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountManager {
    private Connection con;
    private Scanner sc;

    public AccountManager(Connection con, Scanner sc) {
        this.con = con;
        this.sc = sc;
    }

    public void credit_money(long account_number) throws SQLException {
        System.out.println("Enter Amount :- ");
        double amount = sc.nextDouble();
        System.out.println("Enter Security Pin:- ");
        int security_pin = sc.nextInt();
        try {
            if (amount <= 0) {
                System.out.println("Invalid Amount!");
                return;
            }
            con.setAutoCommit(false);
            PreparedStatement prepareStatement = con.prepareStatement("SELECT * FROM Accounts WHERE account_number = ? AND security_pin = ?");
            prepareStatement.setLong(1, account_number);
            prepareStatement.setInt(2, security_pin);
            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                String credit_query = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
                PreparedStatement prepareStatement1 = con.prepareStatement(credit_query);
                prepareStatement1.setDouble(1, amount);
                prepareStatement1.setLong(2, account_number);
                int rowsAffected = prepareStatement1.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Rs." + amount + " credited Succesfully");
                    con.commit();
                } else {
                    System.out.println("Transaction Failed!");
                    con.rollback();
                }
            } else {
                System.out.println("Invalid Pin!");
            }
        } catch (SQLException e) {
            con.rollback();
            e.printStackTrace();
        } finally {
            con.setAutoCommit(true);
        }
    }

    public void debit_money(long account_number) throws SQLException {
        System.out.println("Enter Amount :- ");
        double amount = sc.nextDouble();
        System.out.println("Enter Security Pin:- ");
        int security_pin = sc.nextInt();
        try {
            if (amount <= 0) {
                System.out.println("Invalid Amount!");
                return;
            }
            con.setAutoCommit(false);
            if (account_number != 0) {
                PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM Accounts WHERE account_number = ? AND security_pin = ?");
                preparedStatement.setLong(1, account_number);
                preparedStatement.setInt(2, security_pin);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    double current_balance = resultSet.getDouble("balance");
                    if (amount <= current_balance) {
                        String debit_query = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
                        PreparedStatement prepareStatement = con.prepareStatement(debit_query);
                        prepareStatement.setDouble(1, amount);
                        prepareStatement.setLong(2, account_number);
                        int rowsAffected = prepareStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Rs." + amount + " debited Succesfully");
                            con.commit();
                        } else {
                            System.out.println("Transaction Failed!");
                            con.rollback();
                        }
                    } else {
                        System.out.println("Insufficient Balance!");
                        con.rollback();
                    }
                } else {
                    System.out.println("Invalid Pin!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.setAutoCommit(true);
        }
    }

    public void getBalance(long account_number) {
        System.out.println("Enter Security Pin:- ");
        int security_pin = sc.nextInt();
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT balance FROM Accounts WHERE account_number = ? AND security_pin = ?");
            preparedStatement.setLong(1, account_number);
            preparedStatement.setInt(2, security_pin);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                System.out.println("Balance :- " + balance);
            } else {
                System.out.println("Invalid Pin!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void transfer_money(long sender_account_number) throws SQLException {
        System.out.println("Enter Receiver Account Number:- ");
        long receiver_account_number = sc.nextLong();
        System.out.println("Enter Amount :- ");
        double amount = sc.nextDouble();
        System.out.println("Enter Security Pin:- ");
        int security_pin = sc.nextInt();
        try {
            if (amount <= 0) {
                System.out.println("Invalid Amount!");
                return;
            }
            con.setAutoCommit(false);
            PreparedStatement sendercheck = con.prepareStatement("SELECT * FROM  Accounts WHERE account_number = ? AND security_pin=?");
            sendercheck.setLong(1, sender_account_number);
            sendercheck.setInt(2, security_pin);
            ResultSet resultSet = sendercheck.executeQuery();

            if (!resultSet.next()) {
                System.out.println("Invalid Sender Account or Pin!");
                con.rollback();
                return;
            }
            double current_balance = resultSet.getDouble("balance");
            if (amount > current_balance) {
                System.out.println("Insufficient Balance!");
                con.rollback();
                return;
            }
            PreparedStatement receiverCheck = con.prepareStatement("SELECT * FROM  Accounts WHERE account_number =? ");
            receiverCheck.setLong(1, receiver_account_number);
            ResultSet receiver_resultSet = receiverCheck.executeQuery();
            if (!receiver_resultSet.next()) {
                System.out.println("Receiver account not found!");
                con.rollback();
                return;
            }

            String debit_query = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
            String credit_query = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement creditPreparedStatement = con.prepareStatement(credit_query);
            PreparedStatement debitPreparedStatement = con.prepareStatement(debit_query);
            creditPreparedStatement.setDouble(1, amount);
            creditPreparedStatement.setLong(2, receiver_account_number);
            debitPreparedStatement.setDouble(1, amount);
            debitPreparedStatement.setLong(2, sender_account_number);
            int rowsAffected1 = debitPreparedStatement.executeUpdate();
            int rowsAffected2 = creditPreparedStatement.executeUpdate();
            if (rowsAffected1 > 0 && rowsAffected2 > 0) {
                con.commit();
                System.out.println("Transaction Successfully");
                System.out.println("Rs." + amount + " Transferred Successfully");
            } else {
                System.out.println("Transcation Failed!");
                con.rollback();
            }
        } catch (SQLException e) {
            con.rollback();
            e.printStackTrace();
        } finally {
            con.setAutoCommit(true);
        }
    }
}

