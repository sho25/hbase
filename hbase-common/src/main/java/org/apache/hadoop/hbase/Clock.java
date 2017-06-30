begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|exceptions
operator|.
name|HBaseException
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
name|util
operator|.
name|EnvironmentEdgeManager
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|AtomicUtils
operator|.
name|updateMax
import|;
end_import

begin_comment
comment|/**  * A clock is an implementation of an algorithm to get timestamps corresponding to one of the  * {@link TimestampType}s for the current time. Different clock implementations can have  * different semantics associated with them. Every such clock should be able to map its  * representation of time to one of the {link TimestampType}s.  * HBase has traditionally been using the {@link java.lang.System#currentTimeMillis()} to  * timestamp events in HBase. {@link java.lang.System#currentTimeMillis()} does not give any  * guarantees about monotonicity of time. We will keep this implementation of clock in place for  * backward compatibility and call it SYSTEM clock.  * It is easy to provide monotonically non decreasing time semantics by keeping track of the last  * timestamp given by the clock and updating it on receipt of external message. This  * implementation of clock is called SYSTEM_MONOTONIC.  * SYSTEM Clock and SYSTEM_MONOTONIC clock as described above, both being physical clocks, they  * cannot track causality. Hybrid Logical Clocks(HLC), as described in  *<a href="http://www.cse.buffalo.edu/tech-reports/2014-04.pdf">HLC Paper</a>, helps tracking  * causality using a  *<a href="http://research.microsoft.com/en-us/um/people/lamport/pubs/time-clocks.pdf">Logical  * Clock</a> but always keeps the logical time close to the wall time or physical time. It kind  * of has the advantages of both the worlds. One such advantage being getting consistent  * snapshots in physical time as described in the paper. Hybrid Logical Clock has an additional  * advantage that it is always monotonically increasing.  * Note: It is assumed that any physical clock implementation has millisecond resolution else the  * {@link TimestampType} implementation has to changed to accommodate it. It is decided after  * careful discussion to go with millisecond resolution in the HLC design document attached in the  * issue<a href="https://issues.apache.org/jira/browse/HBASE-14070">HBASE-14070</a>.  */
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
specifier|public
specifier|abstract
class|class
name|Clock
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Clock
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|PhysicalClock
name|physicalClock
decl_stmt|;
specifier|protected
name|TimestampType
name|timestampType
decl_stmt|;
specifier|public
name|ClockType
name|clockType
decl_stmt|;
name|Clock
parameter_list|(
name|PhysicalClock
name|physicalClock
parameter_list|)
block|{
name|this
operator|.
name|physicalClock
operator|=
name|physicalClock
expr_stmt|;
block|}
comment|// Only for testing.
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|Clock
name|getDummyClockOfGivenClockType
parameter_list|(
name|ClockType
name|clockType
parameter_list|)
block|{
if|if
condition|(
name|clockType
operator|==
name|ClockType
operator|.
name|HLC
condition|)
block|{
return|return
operator|new
name|Clock
operator|.
name|HLC
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|clockType
operator|==
name|ClockType
operator|.
name|SYSTEM_MONOTONIC
condition|)
block|{
return|return
operator|new
name|Clock
operator|.
name|SystemMonotonic
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|Clock
operator|.
name|System
argument_list|()
return|;
block|}
block|}
comment|/**    * Indicates that Physical Time or Logical Time component has overflowed. This extends    * RuntimeException.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
specifier|static
class|class
name|ClockException
extends|extends
name|RuntimeException
block|{
specifier|public
name|ClockException
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This is a method to get the current time.    *    * @return Timestamp of current time in 64 bit representation corresponding to the particular    * clock    */
specifier|public
specifier|abstract
name|long
name|now
parameter_list|()
throws|throws
name|RuntimeException
function_decl|;
comment|/**    * This is a method to update the current time with the passed timestamp.    * @param timestamp    * @return Timestamp of current time in 64 bit representation corresponding to the particular    * clock    */
specifier|public
specifier|abstract
name|long
name|update
parameter_list|(
name|long
name|timestamp
parameter_list|)
throws|throws
name|RuntimeException
function_decl|;
comment|/**    * @return true if the clock implementation gives monotonically non decreasing timestamps else    * false.    */
specifier|public
specifier|abstract
name|boolean
name|isMonotonic
parameter_list|()
function_decl|;
comment|/**    * @return true if the clock implementation gives monotonically increasing timestamps else false.    */
specifier|public
specifier|abstract
name|boolean
name|isMonotonicallyIncreasing
parameter_list|()
function_decl|;
comment|/**    * @return {@link org.apache.hadoop.hbase.TimestampType}    */
specifier|public
name|TimestampType
name|getTimestampType
parameter_list|()
block|{
return|return
name|timestampType
return|;
block|}
interface|interface
name|Monotonic
block|{
comment|// This is currently equal to the HBase default.
name|long
name|DEFAULT_MAX_CLOCK_SKEW
init|=
literal|30000
decl_stmt|;
comment|/**      * This is a method to update the local clock on receipt of a timestamped message from      * the external world.      *      * @param timestamp The timestamp present in the message received by the node from outside.      */
name|long
name|update
parameter_list|(
name|long
name|timestamp
parameter_list|)
throws|throws
name|RuntimeException
throws|,
name|HBaseException
function_decl|;
block|}
specifier|public
interface|interface
name|PhysicalClock
block|{
comment|/**      * This is a method to get the current time.      *      * @return Timestamp of current time in 64 bit representation corresponding to the particular      * clock      */
name|long
name|now
parameter_list|()
throws|throws
name|RuntimeException
function_decl|;
comment|/**      * This is a method to get the unit of the physical time used by the clock      *      * @return A {@link TimeUnit}      */
name|TimeUnit
name|getTimeUnit
parameter_list|()
function_decl|;
block|}
specifier|public
specifier|static
class|class
name|JavaMillisPhysicalClock
implements|implements
name|PhysicalClock
block|{
annotation|@
name|Override
specifier|public
name|long
name|now
parameter_list|()
block|{
return|return
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TimeUnit
name|getTimeUnit
parameter_list|()
block|{
return|return
name|TimeUnit
operator|.
name|MILLISECONDS
return|;
block|}
block|}
comment|/**    * Returns the default physical clock used in HBase. It is currently based on    * {@link java.lang.System#currentTimeMillis()}    *    * @return the default PhysicalClock    */
specifier|public
specifier|static
name|PhysicalClock
name|getDefaultPhysicalClock
parameter_list|()
block|{
return|return
operator|new
name|JavaMillisPhysicalClock
argument_list|()
return|;
block|}
comment|/**    * System clock is an implementation of clock which doesn't give any monotonic guarantees.    */
specifier|public
specifier|static
class|class
name|System
extends|extends
name|Clock
implements|implements
name|PhysicalClock
block|{
specifier|public
name|System
parameter_list|()
block|{
name|super
argument_list|(
name|getDefaultPhysicalClock
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestampType
operator|=
name|TimestampType
operator|.
name|PHYSICAL
expr_stmt|;
name|this
operator|.
name|clockType
operator|=
name|ClockType
operator|.
name|SYSTEM
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|now
parameter_list|()
block|{
return|return
name|physicalClock
operator|.
name|now
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|update
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
return|return
name|physicalClock
operator|.
name|now
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMonotonic
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMonotonicallyIncreasing
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|TimeUnit
name|getTimeUnit
parameter_list|()
block|{
return|return
name|physicalClock
operator|.
name|getTimeUnit
argument_list|()
return|;
block|}
block|}
comment|/**    * System clock is an implementation of clock which guarantees monotonically non-decreasing    * timestamps.    */
specifier|public
specifier|static
class|class
name|SystemMonotonic
extends|extends
name|Clock
implements|implements
name|Monotonic
implements|,
name|PhysicalClock
block|{
specifier|private
name|long
name|maxClockSkew
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|OFFSET
init|=
literal|5000
decl_stmt|;
name|AtomicLong
name|physicalTime
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|public
name|SystemMonotonic
parameter_list|(
name|PhysicalClock
name|physicalClock
parameter_list|,
name|long
name|maxClockSkew
parameter_list|)
block|{
name|super
argument_list|(
name|physicalClock
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxClockSkew
operator|=
name|maxClockSkew
operator|>
literal|0
condition|?
name|maxClockSkew
else|:
name|DEFAULT_MAX_CLOCK_SKEW
expr_stmt|;
name|this
operator|.
name|timestampType
operator|=
name|TimestampType
operator|.
name|PHYSICAL
expr_stmt|;
name|this
operator|.
name|clockType
operator|=
name|ClockType
operator|.
name|SYSTEM_MONOTONIC
expr_stmt|;
block|}
specifier|public
name|SystemMonotonic
parameter_list|()
block|{
name|super
argument_list|(
name|getDefaultPhysicalClock
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxClockSkew
operator|=
name|DEFAULT_MAX_CLOCK_SKEW
expr_stmt|;
name|this
operator|.
name|timestampType
operator|=
name|TimestampType
operator|.
name|PHYSICAL
expr_stmt|;
name|this
operator|.
name|clockType
operator|=
name|ClockType
operator|.
name|SYSTEM_MONOTONIC
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|now
parameter_list|()
block|{
name|long
name|systemTime
init|=
name|physicalClock
operator|.
name|now
argument_list|()
decl_stmt|;
name|updateMax
argument_list|(
name|physicalTime
argument_list|,
name|systemTime
argument_list|)
expr_stmt|;
return|return
name|physicalTime
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|update
parameter_list|(
name|long
name|messageTimestamp
parameter_list|)
throws|throws
name|ClockException
block|{
name|long
name|systemTime
init|=
name|physicalClock
operator|.
name|now
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxClockSkew
operator|>
literal|0
operator|&&
operator|(
name|messageTimestamp
operator|-
name|systemTime
operator|)
operator|>
name|maxClockSkew
condition|)
block|{
throw|throw
operator|new
name|ClockException
argument_list|(
literal|"Received event with timestamp:"
operator|+
name|timestampType
operator|.
name|toString
argument_list|(
name|messageTimestamp
argument_list|)
operator|+
literal|" which is greater than allowed clock skew "
argument_list|)
throw|;
block|}
name|long
name|physicalTime_
init|=
name|systemTime
operator|>
name|messageTimestamp
condition|?
name|systemTime
else|:
name|messageTimestamp
decl_stmt|;
name|updateMax
argument_list|(
name|physicalTime
argument_list|,
name|physicalTime_
argument_list|)
expr_stmt|;
return|return
name|physicalTime
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMonotonic
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMonotonicallyIncreasing
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|TimeUnit
name|getTimeUnit
parameter_list|()
block|{
return|return
name|physicalClock
operator|.
name|getTimeUnit
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|setPhysicalTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|physicalTime
operator|.
name|set
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HLC
extends|extends
name|Clock
implements|implements
name|Monotonic
implements|,
name|PhysicalClock
block|{
specifier|private
name|long
name|maxClockSkew
decl_stmt|;
specifier|private
name|long
name|physicalTime
decl_stmt|;
specifier|private
name|long
name|logicalTime
decl_stmt|;
specifier|private
name|long
name|maxPhysicalTime
decl_stmt|;
specifier|private
name|long
name|maxLogicalTime
decl_stmt|;
specifier|public
name|HLC
parameter_list|(
name|PhysicalClock
name|physicalClock
parameter_list|,
name|long
name|maxClockSkew
parameter_list|)
block|{
name|super
argument_list|(
name|physicalClock
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxClockSkew
operator|=
name|maxClockSkew
operator|>
literal|0
condition|?
name|maxClockSkew
else|:
name|DEFAULT_MAX_CLOCK_SKEW
expr_stmt|;
name|this
operator|.
name|timestampType
operator|=
name|TimestampType
operator|.
name|HYBRID
expr_stmt|;
name|this
operator|.
name|maxPhysicalTime
operator|=
name|timestampType
operator|.
name|getMaxPhysicalTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxLogicalTime
operator|=
name|timestampType
operator|.
name|getMaxLogicalTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|physicalTime
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|logicalTime
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|clockType
operator|=
name|ClockType
operator|.
name|HLC
expr_stmt|;
block|}
specifier|public
name|HLC
parameter_list|()
block|{
name|super
argument_list|(
name|getDefaultPhysicalClock
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxClockSkew
operator|=
name|DEFAULT_MAX_CLOCK_SKEW
expr_stmt|;
name|this
operator|.
name|timestampType
operator|=
name|TimestampType
operator|.
name|HYBRID
expr_stmt|;
name|this
operator|.
name|maxPhysicalTime
operator|=
name|timestampType
operator|.
name|getMaxPhysicalTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxLogicalTime
operator|=
name|timestampType
operator|.
name|getMaxLogicalTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|physicalTime
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|logicalTime
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|clockType
operator|=
name|ClockType
operator|.
name|HLC
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|long
name|now
parameter_list|()
throws|throws
name|ClockException
block|{
name|long
name|systemTime
init|=
name|physicalClock
operator|.
name|now
argument_list|()
decl_stmt|;
name|long
name|physicalTime_
init|=
name|physicalTime
decl_stmt|;
if|if
condition|(
name|systemTime
operator|>=
name|maxPhysicalTime
condition|)
block|{
comment|// Extremely unlikely to happen, if this happens upper layers may have to kill the server.
throw|throw
operator|new
name|ClockException
argument_list|(
literal|"PT overflowed: "
operator|+
name|systemTime
operator|+
literal|" and max physical time:"
operator|+
name|maxPhysicalTime
argument_list|)
throw|;
block|}
if|if
condition|(
name|logicalTime
operator|>=
name|maxLogicalTime
condition|)
block|{
comment|// highly unlikely to happen, when it happens, we throw exception for the above layer to
comment|// handle.
throw|throw
operator|new
name|ClockException
argument_list|(
literal|"Logical Time Overflowed: "
operator|+
name|logicalTime
operator|+
literal|"max "
operator|+
literal|"logical "
operator|+
literal|"time:"
operator|+
name|maxLogicalTime
argument_list|)
throw|;
block|}
if|if
condition|(
name|systemTime
operator|>
name|physicalTime_
condition|)
name|physicalTime
operator|=
name|systemTime
expr_stmt|;
if|if
condition|(
name|physicalTime
operator|==
name|physicalTime_
condition|)
block|{
name|logicalTime
operator|++
expr_stmt|;
block|}
else|else
block|{
name|logicalTime
operator|=
literal|0
expr_stmt|;
block|}
return|return
name|toTimestamp
argument_list|()
return|;
block|}
comment|/**      * Updates {@link HLC} with the given timestamp received from elsewhere (possibly      * some other node). Returned timestamp is strict greater than msgTimestamp and local      * timestamp.      *      * @param messageTimestamp timestamp from the external message.      * @return a hybrid timestamp of HLC that is strictly greater than local timestamp and      * msgTimestamp      * @throws ClockException      */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|long
name|update
parameter_list|(
name|long
name|messageTimestamp
parameter_list|)
throws|throws
name|ClockException
block|{
name|long
name|messagePhysicalTime
init|=
name|timestampType
operator|.
name|getPhysicalTime
argument_list|(
name|messageTimestamp
argument_list|)
decl_stmt|;
name|long
name|messageLogicalTime
init|=
name|timestampType
operator|.
name|getLogicalTime
argument_list|(
name|messageTimestamp
argument_list|)
decl_stmt|;
comment|// variable to keep old physical time when we update it.
name|long
name|physicalTime_
init|=
name|physicalTime
decl_stmt|;
name|long
name|systemTime
init|=
name|physicalClock
operator|.
name|now
argument_list|()
decl_stmt|;
name|physicalTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|physicalTime_
argument_list|,
name|messagePhysicalTime
argument_list|)
argument_list|,
name|systemTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|systemTime
operator|>=
name|maxPhysicalTime
condition|)
block|{
comment|// Extremely unlikely to happen, if this happens upper layers may have to kill the server.
throw|throw
operator|new
name|ClockException
argument_list|(
literal|"Physical Time overflowed: "
operator|+
name|systemTime
operator|+
literal|" and max physical time:"
operator|+
name|maxPhysicalTime
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|messagePhysicalTime
operator|-
name|systemTime
operator|>
name|maxClockSkew
condition|)
block|{
throw|throw
operator|new
name|ClockException
argument_list|(
literal|"Received event with timestamp:"
operator|+
name|timestampType
operator|.
name|toString
argument_list|(
name|messageTimestamp
argument_list|)
operator|+
literal|" which is greater than allowed clock skew "
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|physicalTime
operator|==
name|physicalTime_
operator|&&
name|physicalTime_
operator|==
name|messagePhysicalTime
condition|)
block|{
name|logicalTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|logicalTime
argument_list|,
name|messageLogicalTime
argument_list|)
operator|+
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|physicalTime
operator|==
name|messagePhysicalTime
condition|)
block|{
name|logicalTime
operator|=
name|messageLogicalTime
operator|+
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|physicalTime
operator|==
name|physicalTime_
condition|)
block|{
name|logicalTime
operator|++
expr_stmt|;
block|}
else|else
block|{
name|logicalTime
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|logicalTime
operator|>=
name|maxLogicalTime
condition|)
block|{
comment|// highly unlikely to happen, when it happens, we throw exception for the above layer to
comment|// handle it the way they wish to.
throw|throw
operator|new
name|ClockException
argument_list|(
literal|"Logical Time Overflowed: "
operator|+
name|logicalTime
operator|+
literal|"max "
operator|+
literal|"logical time: "
operator|+
name|maxLogicalTime
argument_list|)
throw|;
block|}
return|return
name|toTimestamp
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMonotonic
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMonotonicallyIncreasing
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|TimeUnit
name|getTimeUnit
parameter_list|()
block|{
return|return
name|physicalClock
operator|.
name|getTimeUnit
argument_list|()
return|;
block|}
specifier|private
name|long
name|toTimestamp
parameter_list|()
block|{
return|return
name|timestampType
operator|.
name|toTimestamp
argument_list|(
name|getTimeUnit
argument_list|()
argument_list|,
name|physicalTime
argument_list|,
name|logicalTime
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|synchronized
name|void
name|setLogicalTime
parameter_list|(
name|long
name|logicalTime
parameter_list|)
block|{
name|this
operator|.
name|logicalTime
operator|=
name|logicalTime
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|synchronized
name|void
name|setPhysicalTime
parameter_list|(
name|long
name|physicalTime
parameter_list|)
block|{
name|this
operator|.
name|physicalTime
operator|=
name|physicalTime
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

