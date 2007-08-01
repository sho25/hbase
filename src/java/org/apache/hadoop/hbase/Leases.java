begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
comment|/**  * Leases  *  * There are several server classes in HBase that need to track external  * clients that occasionally send heartbeats.  *   *<p>These external clients hold resources in the server class.  * Those resources need to be released if the external client fails to send a  * heartbeat after some interval of time passes.  *  *<p>The Leases class is a general reusable class for this kind of pattern.  * An instance of the Leases class will create a thread to do its dirty work.    * You should close() the instance if you want to clean up the thread properly.  */
end_comment

begin_class
specifier|public
class|class
name|Leases
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Leases
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|long
name|leasePeriod
decl_stmt|;
specifier|protected
specifier|final
name|long
name|leaseCheckFrequency
decl_stmt|;
specifier|private
specifier|final
name|LeaseMonitor
name|leaseMonitor
decl_stmt|;
specifier|private
specifier|final
name|Thread
name|leaseMonitorThread
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|LeaseName
argument_list|,
name|Lease
argument_list|>
name|leases
init|=
operator|new
name|HashMap
argument_list|<
name|LeaseName
argument_list|,
name|Lease
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
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
specifier|protected
name|boolean
name|running
init|=
literal|true
decl_stmt|;
comment|/**    * Creates a lease    *     * @param leasePeriod - length of time (milliseconds) that the lease is valid    * @param leaseCheckFrequency - how often the lease should be checked    * (milliseconds)    */
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
name|leaseCheckFrequency
operator|=
name|leaseCheckFrequency
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
comment|/**    * Shuts down this lease instance when all outstanding leases expire.    * Like {@link #close()} but rather than violently end all leases, waits    * first on extant leases to finish.  Use this method if the lease holders    * could loose data, leak locks, etc.  Presumes client has shutdown    * allocation of new leases.    */
specifier|public
name|void
name|closeAfterLeasesExpire
parameter_list|()
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|leases
init|)
block|{
while|while
condition|(
name|this
operator|.
name|leases
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|leases
operator|.
name|size
argument_list|()
argument_list|)
operator|+
literal|" lease(s) "
operator|+
literal|"outstanding. Waiting for them to expire."
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|leases
operator|.
name|wait
argument_list|(
name|this
operator|.
name|leaseCheckFrequency
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
comment|// Now call close since no leases outstanding.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Shut down this Leases instance.  All pending leases will be destroyed,     * without any cancellation calls.    */
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"closing leases"
argument_list|)
expr_stmt|;
block|}
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
block|{
comment|// Ignore
block|}
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
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"leases closed"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/* A client obtains a lease... */
comment|/**    * Obtain a lease    *     * @param holderId id of lease holder    * @param resourceId id of resource being leased    * @param listener listener that will process lease expirations    */
specifier|public
name|void
name|createLease
parameter_list|(
specifier|final
name|long
name|holderId
parameter_list|,
specifier|final
name|long
name|resourceId
parameter_list|,
specifier|final
name|LeaseListener
name|listener
parameter_list|)
block|{
name|LeaseName
name|name
init|=
literal|null
decl_stmt|;
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
name|name
operator|=
name|lease
operator|.
name|getLeaseName
argument_list|()
expr_stmt|;
if|if
condition|(
name|leases
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Impossible state for createLease(): "
operator|+
literal|"Lease "
operator|+
name|name
operator|+
literal|" is still held."
argument_list|)
throw|;
block|}
name|leases
operator|.
name|put
argument_list|(
name|name
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
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created lease "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
block|}
comment|/* A client renews a lease... */
comment|/**    * Renew a lease    *     * @param holderId id of lease holder    * @param resourceId id of resource being leased    * @throws IOException    */
specifier|public
name|void
name|renewLease
parameter_list|(
specifier|final
name|long
name|holderId
parameter_list|,
specifier|final
name|long
name|resourceId
parameter_list|)
throws|throws
name|IOException
block|{
name|LeaseName
name|name
init|=
literal|null
decl_stmt|;
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
name|name
operator|=
name|createLeaseName
argument_list|(
name|holderId
argument_list|,
name|resourceId
argument_list|)
expr_stmt|;
name|Lease
name|lease
init|=
name|leases
operator|.
name|get
argument_list|(
name|name
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
literal|"Cannot renew lease that is not held: "
operator|+
name|name
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
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Renewed lease "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Client explicitly cancels a lease.    *     * @param holderId id of lease holder    * @param resourceId id of resource being leased    */
specifier|public
name|void
name|cancelLease
parameter_list|(
specifier|final
name|long
name|holderId
parameter_list|,
specifier|final
name|long
name|resourceId
parameter_list|)
block|{
name|LeaseName
name|name
init|=
literal|null
decl_stmt|;
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
name|name
operator|=
name|createLeaseName
argument_list|(
name|holderId
argument_list|,
name|resourceId
argument_list|)
expr_stmt|;
name|Lease
name|lease
init|=
name|leases
operator|.
name|get
argument_list|(
name|name
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
comment|// it just expired a moment ago.  So just skip it.
return|return;
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
name|name
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cancel lease "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** LeaseMonitor is a thread that expires Leases that go on too long. */
class|class
name|LeaseMonitor
implements|implements
name|Runnable
block|{
comment|/** {@inheritDoc} */
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
name|getLeaseName
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
block|{
comment|// continue
block|}
block|}
block|}
block|}
comment|/*    * A Lease name.    * More lightweight than String or Text.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
class|class
name|LeaseName
implements|implements
name|Comparable
block|{
specifier|private
specifier|final
name|long
name|holderId
decl_stmt|;
specifier|private
specifier|final
name|long
name|resourceId
decl_stmt|;
name|LeaseName
parameter_list|(
specifier|final
name|long
name|hid
parameter_list|,
specifier|final
name|long
name|rid
parameter_list|)
block|{
name|this
operator|.
name|holderId
operator|=
name|hid
expr_stmt|;
name|this
operator|.
name|resourceId
operator|=
name|rid
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|LeaseName
name|other
init|=
operator|(
name|LeaseName
operator|)
name|obj
decl_stmt|;
return|return
name|this
operator|.
name|holderId
operator|==
name|other
operator|.
name|holderId
operator|&&
name|this
operator|.
name|resourceId
operator|==
name|other
operator|.
name|resourceId
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
comment|// Copy OR'ing from javadoc for Long#hashCode.
name|int
name|result
init|=
call|(
name|int
call|)
argument_list|(
name|this
operator|.
name|holderId
operator|^
operator|(
name|this
operator|.
name|holderId
operator|>>>
literal|32
operator|)
argument_list|)
decl_stmt|;
name|result
operator|^=
call|(
name|int
call|)
argument_list|(
name|this
operator|.
name|resourceId
operator|^
operator|(
name|this
operator|.
name|resourceId
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Long
operator|.
name|toString
argument_list|(
name|this
operator|.
name|holderId
argument_list|)
operator|+
literal|"/"
operator|+
name|Long
operator|.
name|toString
argument_list|(
name|this
operator|.
name|resourceId
argument_list|)
return|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|LeaseName
name|other
init|=
operator|(
name|LeaseName
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|holderId
operator|<
name|other
operator|.
name|holderId
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|this
operator|.
name|holderId
operator|>
name|other
operator|.
name|holderId
condition|)
block|{
return|return
literal|1
return|;
block|}
comment|// holderIds are equal
if|if
condition|(
name|this
operator|.
name|resourceId
operator|<
name|other
operator|.
name|resourceId
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|this
operator|.
name|resourceId
operator|>
name|other
operator|.
name|resourceId
condition|)
block|{
return|return
literal|1
return|;
block|}
comment|// Objects are equal
return|return
literal|0
return|;
block|}
block|}
comment|/** Create a lease id out of the holder and resource ids. */
specifier|protected
name|LeaseName
name|createLeaseName
parameter_list|(
specifier|final
name|long
name|hid
parameter_list|,
specifier|final
name|long
name|rid
parameter_list|)
block|{
return|return
operator|new
name|LeaseName
argument_list|(
name|hid
argument_list|,
name|rid
argument_list|)
return|;
block|}
comment|/** This class tracks a single Lease. */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
class|class
name|Lease
implements|implements
name|Comparable
block|{
specifier|final
name|long
name|holderId
decl_stmt|;
specifier|final
name|long
name|resourceId
decl_stmt|;
specifier|final
name|LeaseListener
name|listener
decl_stmt|;
name|long
name|lastUpdate
decl_stmt|;
specifier|private
name|LeaseName
name|leaseId
decl_stmt|;
name|Lease
parameter_list|(
specifier|final
name|long
name|holderId
parameter_list|,
specifier|final
name|long
name|resourceId
parameter_list|,
specifier|final
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
specifier|synchronized
name|LeaseName
name|getLeaseName
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|leaseId
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|leaseId
operator|=
name|createLeaseName
argument_list|(
name|holderId
argument_list|,
name|resourceId
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|leaseId
return|;
block|}
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
block|}
name|void
name|expired
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Lease expired "
operator|+
name|getLeaseName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|listener
operator|.
name|leaseExpired
argument_list|()
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|obj
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|this
operator|.
name|getLeaseName
argument_list|()
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|lastUpdate
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Comparable
comment|//////////////////////////////////////////////////////////////////////////////
comment|/** {@inheritDoc} */
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
name|getLeaseName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|getLeaseName
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

