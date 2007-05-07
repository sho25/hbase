begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/*******************************************************************************  * Leases  *  * There are several server classes in HBase that need to track external clients  * that occasionally send heartbeats.  *   * These external clients hold resources in the server class.  Those resources   * need to be released if the external client fails to send a heartbeat after   * some interval of time passes.  *  * The Leases class is a general reusable class for this kind of pattern.  *  * An instance of the Leases class will create a thread to do its dirty work.    * You should close() the instance if you want to clean up the thread properly.  ******************************************************************************/
end_comment

begin_class
specifier|public
class|class
name|Leases
block|{
name|long
name|leasePeriod
decl_stmt|;
name|long
name|leaseCheckFrequency
decl_stmt|;
name|LeaseMonitor
name|leaseMonitor
decl_stmt|;
name|Thread
name|leaseMonitorThread
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Lease
argument_list|>
name|leases
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Lease
argument_list|>
argument_list|()
decl_stmt|;
name|TreeSet
argument_list|<
name|Lease
argument_list|>
name|sortedLeases
init|=
operator|new
name|TreeSet
argument_list|<
name|Lease
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|running
init|=
literal|true
decl_stmt|;
comment|/** Indicate the length of the lease, in milliseconds */
specifier|public
name|Leases
parameter_list|(
name|long
name|leasePeriod
parameter_list|,
name|long
name|leaseCheckFrequency
parameter_list|)
block|{
name|this
operator|.
name|leasePeriod
operator|=
name|leasePeriod
expr_stmt|;
name|this
operator|.
name|leaseMonitor
operator|=
operator|new
name|LeaseMonitor
argument_list|()
expr_stmt|;
name|this
operator|.
name|leaseMonitorThread
operator|=
operator|new
name|Thread
argument_list|(
name|leaseMonitor
argument_list|)
expr_stmt|;
name|this
operator|.
name|leaseMonitorThread
operator|.
name|setName
argument_list|(
literal|"Lease.monitor"
argument_list|)
expr_stmt|;
name|leaseMonitorThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * Shut down this Leases outfit.  All pending leases will be destroyed,     * without any cancellation calls.    */
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|running
operator|=
literal|false
expr_stmt|;
try|try
block|{
name|this
operator|.
name|leaseMonitorThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|this
operator|.
name|leaseMonitorThread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|iex
parameter_list|)
block|{     }
synchronized|synchronized
init|(
name|leases
init|)
block|{
synchronized|synchronized
init|(
name|sortedLeases
init|)
block|{
name|leases
operator|.
name|clear
argument_list|()
expr_stmt|;
name|sortedLeases
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/** A client obtains a lease... */
specifier|public
name|void
name|createLease
parameter_list|(
name|Text
name|holderId
parameter_list|,
name|Text
name|resourceId
parameter_list|,
name|LeaseListener
name|listener
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|leases
init|)
block|{
synchronized|synchronized
init|(
name|sortedLeases
init|)
block|{
name|Lease
name|lease
init|=
operator|new
name|Lease
argument_list|(
name|holderId
argument_list|,
name|resourceId
argument_list|,
name|listener
argument_list|)
decl_stmt|;
name|Text
name|leaseId
init|=
name|lease
operator|.
name|getLeaseId
argument_list|()
decl_stmt|;
if|if
condition|(
name|leases
operator|.
name|get
argument_list|(
name|leaseId
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Impossible state for createLease(): Lease for holderId "
operator|+
name|holderId
operator|+
literal|" and resourceId "
operator|+
name|resourceId
operator|+
literal|" is still held."
argument_list|)
throw|;
block|}
name|leases
operator|.
name|put
argument_list|(
name|leaseId
argument_list|,
name|lease
argument_list|)
expr_stmt|;
name|sortedLeases
operator|.
name|add
argument_list|(
name|lease
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/** A client renews a lease... */
specifier|public
name|void
name|renewLease
parameter_list|(
name|Text
name|holderId
parameter_list|,
name|Text
name|resourceId
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|leases
init|)
block|{
synchronized|synchronized
init|(
name|sortedLeases
init|)
block|{
name|Text
name|leaseId
init|=
name|createLeaseId
argument_list|(
name|holderId
argument_list|,
name|resourceId
argument_list|)
decl_stmt|;
name|Lease
name|lease
init|=
name|leases
operator|.
name|get
argument_list|(
name|leaseId
argument_list|)
decl_stmt|;
if|if
condition|(
name|lease
operator|==
literal|null
condition|)
block|{
comment|// It's possible that someone tries to renew the lease, but
comment|// it just expired a moment ago.  So fail.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot renew lease is not held (holderId="
operator|+
name|holderId
operator|+
literal|", resourceId="
operator|+
name|resourceId
operator|+
literal|")"
argument_list|)
throw|;
block|}
name|sortedLeases
operator|.
name|remove
argument_list|(
name|lease
argument_list|)
expr_stmt|;
name|lease
operator|.
name|renew
argument_list|()
expr_stmt|;
name|sortedLeases
operator|.
name|add
argument_list|(
name|lease
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/** A client explicitly cancels a lease.  The lease-cleanup method is not called. */
specifier|public
name|void
name|cancelLease
parameter_list|(
name|Text
name|holderId
parameter_list|,
name|Text
name|resourceId
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|leases
init|)
block|{
synchronized|synchronized
init|(
name|sortedLeases
init|)
block|{
name|Text
name|leaseId
init|=
name|createLeaseId
argument_list|(
name|holderId
argument_list|,
name|resourceId
argument_list|)
decl_stmt|;
name|Lease
name|lease
init|=
name|leases
operator|.
name|get
argument_list|(
name|leaseId
argument_list|)
decl_stmt|;
if|if
condition|(
name|lease
operator|==
literal|null
condition|)
block|{
comment|// It's possible that someone tries to renew the lease, but
comment|// it just expired a moment ago.  So fail.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot cancel lease that is not held (holderId="
operator|+
name|holderId
operator|+
literal|", resourceId="
operator|+
name|resourceId
operator|+
literal|")"
argument_list|)
throw|;
block|}
name|sortedLeases
operator|.
name|remove
argument_list|(
name|lease
argument_list|)
expr_stmt|;
name|leases
operator|.
name|remove
argument_list|(
name|leaseId
argument_list|)
expr_stmt|;
name|lease
operator|.
name|cancelled
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/** LeaseMonitor is a thread that expires Leases that go on too long. */
class|class
name|LeaseMonitor
implements|implements
name|Runnable
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
name|running
condition|)
block|{
synchronized|synchronized
init|(
name|leases
init|)
block|{
synchronized|synchronized
init|(
name|sortedLeases
init|)
block|{
name|Lease
name|top
decl_stmt|;
while|while
condition|(
operator|(
name|sortedLeases
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
operator|&&
operator|(
operator|(
name|top
operator|=
name|sortedLeases
operator|.
name|first
argument_list|()
operator|)
operator|!=
literal|null
operator|)
condition|)
block|{
if|if
condition|(
name|top
operator|.
name|shouldExpire
argument_list|()
condition|)
block|{
name|leases
operator|.
name|remove
argument_list|(
name|top
operator|.
name|getLeaseId
argument_list|()
argument_list|)
expr_stmt|;
name|sortedLeases
operator|.
name|remove
argument_list|(
name|top
argument_list|)
expr_stmt|;
name|top
operator|.
name|expired
argument_list|()
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
block|}
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|leaseCheckFrequency
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{         }
block|}
block|}
block|}
comment|/** Create a lease id out of the holder and resource ids. */
name|Text
name|createLeaseId
parameter_list|(
name|Text
name|holderId
parameter_list|,
name|Text
name|resourceId
parameter_list|)
block|{
return|return
operator|new
name|Text
argument_list|(
literal|"_"
operator|+
name|holderId
operator|+
literal|"/"
operator|+
name|resourceId
operator|+
literal|"_"
argument_list|)
return|;
block|}
comment|/** This class tracks a single Lease. */
class|class
name|Lease
implements|implements
name|Comparable
block|{
name|Text
name|holderId
decl_stmt|;
name|Text
name|resourceId
decl_stmt|;
name|LeaseListener
name|listener
decl_stmt|;
name|long
name|lastUpdate
decl_stmt|;
specifier|public
name|Lease
parameter_list|(
name|Text
name|holderId
parameter_list|,
name|Text
name|resourceId
parameter_list|,
name|LeaseListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|holderId
operator|=
name|holderId
expr_stmt|;
name|this
operator|.
name|resourceId
operator|=
name|resourceId
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|renew
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Text
name|getLeaseId
parameter_list|()
block|{
return|return
name|createLeaseId
argument_list|(
name|holderId
argument_list|,
name|resourceId
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|shouldExpire
parameter_list|()
block|{
return|return
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastUpdate
operator|>
name|leasePeriod
operator|)
return|;
block|}
specifier|public
name|void
name|renew
parameter_list|()
block|{
name|this
operator|.
name|lastUpdate
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|listener
operator|.
name|leaseRenewed
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|cancelled
parameter_list|()
block|{
name|listener
operator|.
name|leaseCancelled
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|expired
parameter_list|()
block|{
name|listener
operator|.
name|leaseExpired
argument_list|()
expr_stmt|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Comparable
comment|//////////////////////////////////////////////////////////////////////////////
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|Lease
name|other
init|=
operator|(
name|Lease
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|lastUpdate
operator|<
name|other
operator|.
name|lastUpdate
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|lastUpdate
operator|>
name|other
operator|.
name|lastUpdate
condition|)
block|{
return|return
literal|1
return|;
block|}
else|else
block|{
return|return
name|this
operator|.
name|getLeaseId
argument_list|()
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|getLeaseId
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

