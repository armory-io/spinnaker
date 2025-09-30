/*
 * Copyright 2025 Harness, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.google.security;

import com.netflix.spinnaker.clouddriver.google.config.GoogleConfigurationProperties;
import com.netflix.spinnaker.clouddriver.google.model.GoogleLabeledResource;
import com.netflix.spinnaker.clouddriver.names.NamerRegistry;
import com.netflix.spinnaker.credentials.definition.CredentialsParser;
import com.netflix.spinnaker.moniker.Namer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Parser for Google account credentials. */
@Component
public class GoogleCredentialsParser
    implements CredentialsParser<
        GoogleConfigurationProperties.ManagedAccount, GoogleNamedAccountCredentials> {

  private static final Logger log = LoggerFactory.getLogger(GoogleCredentialsParser.class);

  private final NamerRegistry namerRegistry;

  public GoogleCredentialsParser(NamerRegistry namerRegistry) {
    this.namerRegistry = namerRegistry;
  }

  @Override
  public GoogleNamedAccountCredentials parse(
      GoogleConfigurationProperties.ManagedAccount managedAccount) {
    // Configure naming strategy
    String namingStrategy = managedAccount.getNamingStrategy();
    if (namingStrategy == null) {
      namingStrategy = "default";
    }

    try {
      // Register naming strategy in the registry
      if (namerRegistry != null) {
        Namer namer = namerRegistry.getNamingStrategy(namingStrategy);
        if (namer != null) {
          NamerRegistry.lookup()
              .withProvider("gce")
              .withAccount(managedAccount.getName())
              .setNamer(GoogleLabeledResource.class, namer);
        }
      }
    } catch (Exception e) {
      log.warn(
          "Error registering naming strategy for account {}: {}",
          managedAccount.getName(),
          e.getMessage());
    }

    // Return credentials using the Builder pattern that is actually defined in the class
    String environment =
        managedAccount.getEnvironment() != null
            ? managedAccount.getEnvironment()
            : managedAccount.getName();
    String accountType =
        managedAccount.getAccountType() != null
            ? managedAccount.getAccountType()
            : managedAccount.getName();

    // Use the Builder defined in GoogleNamedAccountCredentials
    GoogleNamedAccountCredentials.Builder builder =
        new GoogleNamedAccountCredentials.Builder()
            .name(managedAccount.getName())
            .environment(environment)
            .accountType(accountType)
            .project(managedAccount.getProject())
            .jsonKey(managedAccount.getJsonPath());

    // Return built credentials
    return builder.build();
  }
}
