begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|quotas
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Simple rate limiter.  *  * Usage Example:  *    // At this point you have a unlimited resource limiter  *   RateLimiter limiter = new AverageIntervalRateLimiter();  *                         or new FixedIntervalRateLimiter();  *   limiter.set(10, TimeUnit.SECONDS);       // set 10 resources/sec  *  *   while (true) {  *     // call canExecute before performing resource consuming operation  *     bool canExecute = limiter.canExecute();  *     // If there are no available resources, wait until one is available  *     if (!canExecute) Thread.sleep(limiter.waitInterval());  *     // ...execute the work and consume the resource...  *     limiter.consume();  *   }  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"IS2_INCONSISTENT_SYNC"
argument_list|,
name|justification
operator|=
literal|"FindBugs seems confused; says limit and tlimit "
operator|+
literal|"are mostly synchronized...but to me it looks like they are totally synchronized"
argument_list|)
specifier|public
specifier|abstract
class|class
name|RateLimiter
block|{
specifier|public
specifier|static
specifier|final
name|String
name|QUOTA_RATE_LIMITER_CONF_KEY
init|=
literal|"hbase.quota.rate.limiter"
decl_stmt|;
specifier|private
name|long
name|tunit
init|=
literal|1000
decl_stmt|;
comment|// Timeunit factor for translating to ms.
specifier|private
name|long
name|limit
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// The max value available resource units can be refilled to.
specifier|private
name|long
name|avail
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// Currently available resource units
comment|/**    * Refill the available units w.r.t the elapsed time.    * @param limit Maximum available resource units that can be refilled to.    * @return how many resource units may be refilled ?    */
specifier|abstract
name|long
name|refill
parameter_list|(
name|long
name|limit
parameter_list|)
function_decl|;
comment|/**    * Time in milliseconds to wait for before requesting to consume 'amount' resource.    * @param limit Maximum available resource units that can be refilled to.    * @param available Currently available resource units    * @param amount Resources for which time interval to calculate for    * @return estimate of the ms required to wait before being able to provide 'amount' resources.    */
specifier|abstract
name|long
name|getWaitInterval
parameter_list|(
name|long
name|limit
parameter_list|,
name|long
name|available
parameter_list|,
name|long
name|amount
parameter_list|)
function_decl|;
comment|/**    * Set the RateLimiter max available resources and refill period.    * @param limit The max value available resource units can be refilled to.    * @param timeUnit Timeunit factor for translating to ms.    */
specifier|public
specifier|synchronized
name|void
name|set
parameter_list|(
specifier|final
name|long
name|limit
parameter_list|,
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
switch|switch
condition|(
name|timeUnit
condition|)
block|{
case|case
name|MILLISECONDS
case|:
name|tunit
operator|=
literal|1
expr_stmt|;
break|break;
case|case
name|SECONDS
case|:
name|tunit
operator|=
literal|1000
expr_stmt|;
break|break;
case|case
name|MINUTES
case|:
name|tunit
operator|=
literal|60
operator|*
literal|1000
expr_stmt|;
break|break;
case|case
name|HOURS
case|:
name|tunit
operator|=
literal|60
operator|*
literal|60
operator|*
literal|1000
expr_stmt|;
break|break;
case|case
name|DAYS
case|:
name|tunit
operator|=
literal|24
operator|*
literal|60
operator|*
literal|60
operator|*
literal|1000
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported "
operator|+
name|timeUnit
operator|.
name|name
argument_list|()
operator|+
literal|" TimeUnit."
argument_list|)
throw|;
block|}
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
name|this
operator|.
name|avail
operator|=
name|limit
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|rateLimiter
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
if|if
condition|(
name|getLimit
argument_list|()
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
name|rateLimiter
operator|+
literal|"(Bypass)"
return|;
block|}
return|return
name|rateLimiter
operator|+
literal|"(avail="
operator|+
name|getAvailable
argument_list|()
operator|+
literal|" limit="
operator|+
name|getLimit
argument_list|()
operator|+
literal|" tunit="
operator|+
name|getTimeUnitInMillis
argument_list|()
operator|+
literal|")"
return|;
block|}
comment|/**    * Sets the current instance of RateLimiter to a new values.    *    * if current limit is smaller than the new limit, bump up the available resources.    * Otherwise allow clients to use up the previously available resources.    */
specifier|public
specifier|synchronized
name|void
name|update
parameter_list|(
specifier|final
name|RateLimiter
name|other
parameter_list|)
block|{
name|this
operator|.
name|tunit
operator|=
name|other
operator|.
name|tunit
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|limit
operator|<
name|other
operator|.
name|limit
condition|)
block|{
comment|// If avail is capped to this.limit, it will never overflow,
comment|// otherwise, avail may overflow, just be careful here.
name|long
name|diff
init|=
name|other
operator|.
name|limit
operator|-
name|this
operator|.
name|limit
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|avail
operator|<=
name|Long
operator|.
name|MAX_VALUE
operator|-
name|diff
condition|)
block|{
name|this
operator|.
name|avail
operator|+=
name|diff
expr_stmt|;
name|this
operator|.
name|avail
operator|=
name|Math
operator|.
name|min
argument_list|(
name|this
operator|.
name|avail
argument_list|,
name|other
operator|.
name|limit
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|avail
operator|=
name|other
operator|.
name|limit
expr_stmt|;
block|}
block|}
name|this
operator|.
name|limit
operator|=
name|other
operator|.
name|limit
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isBypass
parameter_list|()
block|{
return|return
name|getLimit
argument_list|()
operator|==
name|Long
operator|.
name|MAX_VALUE
return|;
block|}
specifier|public
specifier|synchronized
name|long
name|getLimit
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
specifier|public
specifier|synchronized
name|long
name|getAvailable
parameter_list|()
block|{
return|return
name|avail
return|;
block|}
specifier|protected
specifier|synchronized
name|long
name|getTimeUnitInMillis
parameter_list|()
block|{
return|return
name|tunit
return|;
block|}
comment|/**    * Is there at least one resource available to allow execution?    * @return true if there is at least one resource available, otherwise false    */
specifier|public
name|boolean
name|canExecute
parameter_list|()
block|{
return|return
name|canExecute
argument_list|(
literal|1
argument_list|)
return|;
block|}
comment|/**    * Are there enough available resources to allow execution?    * @param amount the number of required resources, a non-negative number    * @return true if there are enough available resources, otherwise false    */
specifier|public
specifier|synchronized
name|boolean
name|canExecute
parameter_list|(
specifier|final
name|long
name|amount
parameter_list|)
block|{
if|if
condition|(
name|isBypass
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|long
name|refillAmount
init|=
name|refill
argument_list|(
name|limit
argument_list|)
decl_stmt|;
if|if
condition|(
name|refillAmount
operator|==
literal|0
operator|&&
name|avail
operator|<
name|amount
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// check for positive overflow
if|if
condition|(
name|avail
operator|<=
name|Long
operator|.
name|MAX_VALUE
operator|-
name|refillAmount
condition|)
block|{
name|avail
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|avail
operator|+
name|refillAmount
argument_list|,
name|limit
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|avail
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|limit
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|avail
operator|>=
name|amount
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * consume one available unit.    */
specifier|public
name|void
name|consume
parameter_list|()
block|{
name|consume
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * consume amount available units, amount could be a negative number    * @param amount the number of units to consume    */
specifier|public
specifier|synchronized
name|void
name|consume
parameter_list|(
specifier|final
name|long
name|amount
parameter_list|)
block|{
if|if
condition|(
name|isBypass
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|amount
operator|>=
literal|0
condition|)
block|{
name|this
operator|.
name|avail
operator|-=
name|amount
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|avail
operator|<
literal|0
condition|)
block|{
name|this
operator|.
name|avail
operator|=
literal|0
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|this
operator|.
name|avail
operator|<=
name|Long
operator|.
name|MAX_VALUE
operator|+
name|amount
condition|)
block|{
name|this
operator|.
name|avail
operator|-=
name|amount
expr_stmt|;
name|this
operator|.
name|avail
operator|=
name|Math
operator|.
name|min
argument_list|(
name|this
operator|.
name|avail
argument_list|,
name|this
operator|.
name|limit
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|avail
operator|=
name|this
operator|.
name|limit
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @return estimate of the ms required to wait before being able to provide 1 resource.    */
specifier|public
name|long
name|waitInterval
parameter_list|()
block|{
return|return
name|waitInterval
argument_list|(
literal|1
argument_list|)
return|;
block|}
comment|/**    * @return estimate of the ms required to wait before being able to provide "amount" resources.    */
specifier|public
specifier|synchronized
name|long
name|waitInterval
parameter_list|(
specifier|final
name|long
name|amount
parameter_list|)
block|{
comment|// TODO Handle over quota?
return|return
operator|(
name|amount
operator|<=
name|avail
operator|)
condition|?
literal|0
else|:
name|getWaitInterval
argument_list|(
name|getLimit
argument_list|()
argument_list|,
name|avail
argument_list|,
name|amount
argument_list|)
return|;
block|}
comment|// These two method are for strictly testing purpose only
annotation|@
name|VisibleForTesting
specifier|public
specifier|abstract
name|void
name|setNextRefillTime
parameter_list|(
name|long
name|nextRefillTime
parameter_list|)
function_decl|;
annotation|@
name|VisibleForTesting
specifier|public
specifier|abstract
name|long
name|getNextRefillTime
parameter_list|()
function_decl|;
block|}
end_class

end_unit

