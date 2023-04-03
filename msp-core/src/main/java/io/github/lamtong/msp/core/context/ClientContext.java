/*
Copyright 2023 the original author, Lam Tong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.github.lamtong.msp.core.context;

import com.meilisearch.sdk.Client;
import io.github.lamtong.msp.core.exception.MSPException;

/**
 * Context for {@link Client} of {@code MeiliSearch}.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @see Client
 * @since 1.0.0.SNAPSHOT
 */
public final class ClientContext {

    private static volatile Client client;

    private ClientContext() {
    }

    /**
     * Gets {@link Client} instance of {@code MeiliSearch}, or throws {@link MSPException} if and only if {@link Client}
     * instance is <strong>null</strong>.
     *
     * @return {@link Client} instance
     */
    public static Client getClient() {
        if (client == null) {
            throw new MSPException("Client instance should not be null.");
        }
        return ClientContext.client;
    }

    public static void setClient(Client client) {
        ClientContext.client = client;
    }

}
