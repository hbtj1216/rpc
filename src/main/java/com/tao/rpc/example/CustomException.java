package com.tao.rpc.example;

public class CustomException extends RuntimeException {

	private static final long serialVersionUID = 7292240924613828485L;
	
	public CustomException() {
		super("CustomException!");
	}
}
