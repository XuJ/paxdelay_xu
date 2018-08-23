package edu.mit.nsfnats.paxdelay;

public class InvalidFormatException extends Exception {
	public static final long serialVersionUID = 123456789;

	public InvalidFormatException() {
		super();
	}

	public InvalidFormatException(String s) {
		super(s);
	}

	public InvalidFormatException(Throwable e) {
		super(e);
	}

	public InvalidFormatException(String s, Throwable e) {
		super(s, e);
	}
}
