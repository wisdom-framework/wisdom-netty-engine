/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.engine.server;

import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.router.Router;

/**
 * A structure to access services.
 */
public class ServiceAccessor {

    private final Crypto crypto;
    private final ApplicationConfiguration configuration;
    private final Router router;
    private final ContentEngine contentEngines;
    private final AkkaSystemService system;
    private final Dispatcher dispatcher;

    public ServiceAccessor(Crypto crypto, ApplicationConfiguration configuration, Router router,
                           ContentEngine engine, AkkaSystemService system, Dispatcher dispatcher) {
        this.crypto = crypto;
        this.configuration = configuration;
        this.router = router;
        this.contentEngines = engine;
        this.system = system;
        this.dispatcher = dispatcher;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public ApplicationConfiguration getConfiguration() {
        return configuration;
    }

    public Router getRouter() {
        return router;
    }

    public ContentEngine getContentEngines() {
        return contentEngines;
    }

    public AkkaSystemService getSystem() {
        return system;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

}
