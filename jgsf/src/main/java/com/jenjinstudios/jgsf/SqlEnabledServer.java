package com.jenjinstudios.jgsf;

import com.jenjinstudios.sql.SQLHandler;

import java.io.IOException;

/**
 * A Server with access to a SqlHandler and MySql database.
 * @author Caleb Brinkman
 */
public class SqlEnabledServer<T extends ClientHandler> extends TaskedServer<T>
{
	/** The SQLHandler used by this Server. */
	private SQLHandler sqlHandler;

	/**
	 * Construct a new Server without a SQLHandler.
	 * @param ups The cycles per second at which this server will run.
	 * @param port The port number on which this server will listen.
	 * @param handlerClass The class of ClientHandler used by this Server.
	 * @param sqlHandler The SqlHandler responsible for communicating with a MySql database.
	 * @throws java.io.IOException If there is an IO Error when initializing the server.
	 */
	public SqlEnabledServer(int ups, int port, Class<? extends T> handlerClass, SQLHandler sqlHandler) throws IOException {
		super(ups, port, handlerClass);
		this.sqlHandler = sqlHandler;
	}

	/**
	 * The SQLHandler used by this Server.
	 * @return The SQLHandler used by this Server.
	 */
	public SQLHandler getSqlHandler() {
		return sqlHandler;
	}
}
