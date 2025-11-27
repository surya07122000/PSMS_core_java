package com.hcltech.psms.exception;

import java.lang.RuntimeException;

public class InvalidDateRangeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidDateRangeException(String msg) {
		super(msg);
	}
}
