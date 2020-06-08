/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.aws.messaging;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

/**
 * @author Philip Riecks
 */
@Testcontainers
public abstract class AbstractMessagingIntegrationTest {

	@Container
	public static LocalStackContainer localStack = new LocalStackContainer("0.11.2")
		.withServices(SQS);

	@Autowired
	protected SimpleMessageListenerContainer simpleMessageListenerContainer;

	@BeforeAll
	static void beforeAll() throws IOException, InterruptedException {
		localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name",
			"LoadTestQueue");
	}

	@BeforeEach
	public void setUp() throws Exception {
		if (!this.simpleMessageListenerContainer.isRunning()) {
			this.simpleMessageListenerContainer.start();
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (this.simpleMessageListenerContainer.isRunning()) {
			CountDownLatch countDownLatch = new CountDownLatch(1);
			this.simpleMessageListenerContainer.stop(countDownLatch::countDown);

			if (!countDownLatch.await(15, TimeUnit.SECONDS)) {
				throw new Exception("Couldn't stop container within 15 seconds");
			}
		}
	}

}
