/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.TicketState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * ExpirationPolicy that is based on certain number of uses of a ticket or a
 * certain time period for a ticket to exist.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Component("multiTimeUseOrTimeoutExpirationPolicy")
public final class MultiTimeUseOrTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -5704993954986738308L;

    /** The time to kill in milliseconds. */
    @Value("#{${st.timeToKillInSeconds:10}*1000}")
    private final long timeToKillInMilliSeconds;

    /** The maximum number of uses before expiration. */

    @Value("${st.numberOfUses:1}")
    private final int numberOfUses;


    /** No-arg constructor for serialization support. */
    private MultiTimeUseOrTimeoutExpirationPolicy() {
        this.timeToKillInMilliSeconds = 0;
        this.numberOfUses = 0;
    }


    /**
     * Instantiates a new multi time use or timeout expiration policy.
     *
     * @param numberOfUses the number of uses
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    public MultiTimeUseOrTimeoutExpirationPolicy(final int numberOfUses,
        final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
        this.numberOfUses = numberOfUses;
        Assert.isTrue(this.numberOfUses > 0, "numberOfUsers must be greater than 0.");
        Assert.isTrue(this.timeToKillInMilliSeconds > 0, "timeToKillInMilliseconds must be greater than 0.");

    }

    /**
     * Instantiates a new multi time use or timeout expiration policy.
     *
     * @param numberOfUses the number of uses
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public MultiTimeUseOrTimeoutExpirationPolicy(final int numberOfUses, final long timeToKill,
            final TimeUnit timeUnit) {
        this(numberOfUses, timeUnit.toMillis(timeToKill));
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (ticketState == null) {
            logger.debug("Ticket state is null for {}", this.getClass().getSimpleName());
            return true;
        }
        final long countUses = ticketState.getCountOfUses();
        if (countUses >= this.numberOfUses) {
            logger.debug("Ticket usage count {} is greater than or equal to {}", countUses, this.numberOfUses);
            return true;
        }

        final long systemTime = System.currentTimeMillis();
        final long lastTimeUsed = ticketState.getLastTimeUsed();
        final long difference = systemTime - lastTimeUsed;

        if (difference >= this.timeToKillInMilliSeconds) {
            logger.debug("Ticket has expired because the difference between current time [{}] "
                + "and ticket time [{}] is greater than or equal to [{}]", systemTime, lastTimeUsed,
                this.timeToKillInMilliSeconds);
            return true;
        }
        return false;
    }
}
