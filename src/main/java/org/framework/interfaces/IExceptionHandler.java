package org.framework.interfaces;

public interface IExceptionHandler {
	default void exception(Exception e) {
	}

	default void exception(Exception e, boolean terminate) {
	}

}