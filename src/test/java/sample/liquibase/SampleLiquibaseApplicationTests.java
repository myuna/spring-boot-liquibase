/*
 * Copyright 2012-2018 the original author or authors.
 *
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
 */

package sample.liquibase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.extension.OutputCapture;
import org.springframework.core.NestedCheckedException;

import java.net.ConnectException;
import java.util.Objects;

import static java.util.Objects.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SampleLiquibaseApplicationTests {

    @RegisterExtension
    OutputCapture output = new OutputCapture();

    @Test
    public void testDefaultSettings() {
        try {
            SampleLiquibaseApplication.main(new String[]{"--server.port=0"});
        } catch (IllegalStateException ex) {
            if (serverNotRunning(ex)) {
                return;
            }
        }

        assertThat(this.output).contains("Successfully acquired change log lock")
                .contains("Creating database history table with name: PUBLIC.DATABASECHANGELOG")
                .contains("Table person created")
                .contains("ChangeSet classpath:/db/changelog/db.changelog-1.0.yaml::1::marceloverdijk ran successfully")
                .contains("New row inserted into person")
                .contains("ChangeSet classpath:/db/changelog/db.changelog-1.0.yaml::2::marceloverdijk ran successfully")
                .contains("Columns full_name(varchar(255)) added to person")
                .contains("ChangeSet classpath:/db/changelog/db.changelog-1.0.yaml::3::salem ran successfully")
                .contains("Columns email(varchar(255)) added to person")
                .contains("ChangeSet classpath:/db/changelog/db.changelog-2.0.yaml::4::salem ran successfully")
                .contains("Successfully released change log lock");
    }

    @SuppressWarnings("serial")
    private boolean serverNotRunning(IllegalStateException ex) {
        NestedCheckedException nested = new NestedCheckedException("failed", ex) {
        };
        return nested.contains(ConnectException.class) &&
                requireNonNull(nested.getRootCause()).getMessage().contains("Connection refused");
    }
}
