/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.context.embedded;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.util.Assert;

/**
 * @author Brian Clozel
 */
public class JettyEmbeddedReactiveHttpServer extends AbstractEmbeddedReactiveHttpServer
		implements EmbeddedReactiveHttpServer {

	private Server jettyServer;

	private boolean running;

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		this.jettyServer = new Server();

		Assert.notNull(getHttpHandler(), "HttpHandler implementation must not be null");
		ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(getHttpHandler());
		ServletHolder servletHolder = new ServletHolder(servlet);

		ServletContextHandler contextHandler = new ServletContextHandler(this.jettyServer,
				"", false, false);
		contextHandler.addServlet(servletHolder, "/");

		ServerConnector connector = new ServerConnector(this.jettyServer);
		if (getAddress() != null) {
			connector.setHost(getAddress().getHostAddress());
		}
		connector.setPort(getPort());
		connector.setIdleTimeout(getRequestTimeout());
		this.jettyServer.addConnector(connector);
	}

	@Override
	public void start() {
		if (!this.running) {
			try {
				this.running = true;
				this.jettyServer.start();
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@Override
	public void stop() {
		if (this.running) {
			try {
				this.running = false;
				jettyServer.stop();
				jettyServer.destroy();
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
	}
}
