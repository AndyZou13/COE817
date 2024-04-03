package main;

public class clientFile {

	private String login;
	private double balance = 100.00;
	
	public clientFile(String log) {
		login = log;
	}
	
	public String getLogin() {
		return login;
	}
	public double getBalance() {
		return balance;
	}
	
	public void depositMoney(double mon) {
		balance += mon;
	}
	
	public void withdrawMoney (double mon) {
		balance -= mon;
	}
}
