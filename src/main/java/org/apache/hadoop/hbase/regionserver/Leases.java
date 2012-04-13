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
operator|.
name|regionserver
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
name|org
operator|.
name|apache
operator|.
name|hadoop
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
name|util
operator|.
name|HasThread
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ConcurrentModificationException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|Delayed
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
name|DelayQueue
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
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Leases  *  * There are several server classes in HBase that need to track external  * clients that occasionally send heartbeats.  *  *<p>These external clients hold resources in the server class.  * Those resources need to be released if the external client fails to send a  * heartbeat after some interval of time passes.  *  *<p>The Leases class is a general reusable class for this kind of pattern.  * An instance of the Leases class will create a thread to do its dirty work.  * You should close() the instance if you want to clean up the thread properly.  *  *<p>  * NOTE: This class extends Thread rather than Chore because the sleep time  * can be interrupted when there is something to do, rather than the Chore  * sleep time which is invariant.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Leases
extends|extends
name|HasThread
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
name|int
name|leasePeriod
decl_stmt|;
specifier|private
specifier|final
name|int
name|leaseCheckFrequency
decl_stmt|;
specifier|private
specifier|volatile
name|DelayQueue
argument_list|<
name|Lease
argument_list|>
name|leaseQueue
init|=
operator|new
name|DelayQueue
argument_list|<
name|Lease
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Lease
argument_list|>
name|leases
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Lease
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|stopRequested
init|=
literal|false
decl_stmt|;
comment|/**    * Creates a lease monitor    *    * @param leasePeriod - length of time (milliseconds) that the lease is valid    * @param leaseCheckFrequency - how often the lease should be checked    * (milliseconds)    */
specifier|public
name|Leases
parameter_list|(
specifier|final
name|int
name|leasePeriod
parameter_list|,
specifier|final
name|int
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
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see java.lang.Thread#run()    */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|stopRequested
operator|||
operator|(
name|stopRequested
operator|&&
name|leaseQueue
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
name|Lease
name|lease
init|=
literal|null
decl_stmt|;
try|try
block|{
name|lease
operator|=
name|leaseQueue
operator|.
name|poll
argument_list|(
name|leaseCheckFrequency
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
continue|continue;
block|}
catch|catch
parameter_list|(
name|ConcurrentModificationException
name|e
parameter_list|)
block|{
continue|continue;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Unexpected exception killed leases thread"
argument_list|,
name|e
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|lease
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// A lease expired.  Run the expired code before removing from queue
comment|// since its presence in queue is used to see if lease exists still.
if|if
condition|(
name|lease
operator|.
name|getListener
argument_list|()
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"lease listener is null for lease "
operator|+
name|lease
operator|.
name|getLeaseName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|lease
operator|.
name|getListener
argument_list|()
operator|.
name|leaseExpired
argument_list|()
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|leaseQueue
init|)
block|{
name|leases
operator|.
name|remove
argument_list|(
name|lease
operator|.
name|getLeaseName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Shuts down this lease instance when all outstanding leases expire.    * Like {@link #close()} but rather than violently end all leases, waits    * first on extant leases to finish.  Use this method if the lease holders    * could loose data, leak locks, etc.  Presumes client has shutdown    * allocation of new leases.    */
specifier|public
name|void
name|closeAfterLeasesExpire
parameter_list|()
block|{
name|this
operator|.
name|stopRequested
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Shut down this Leases instance.  All pending leases will be destroyed,    * without any cancellation calls.    */
specifier|public
name|void
name|close
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" closing leases"
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopRequested
operator|=
literal|true
expr_stmt|;
synchronized|synchronized
init|(
name|leaseQueue
init|)
block|{
name|leaseQueue
operator|.
name|clear
argument_list|()
expr_stmt|;
name|leases
operator|.
name|clear
argument_list|()
expr_stmt|;
name|leaseQueue
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" closed leases"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Obtain a lease    *    * @param leaseName name of the lease    * @param listener listener that will process lease expirations    * @throws LeaseStillHeldException    */
specifier|public
name|void
name|createLease
parameter_list|(
name|String
name|leaseName
parameter_list|,
specifier|final
name|LeaseListener
name|listener
parameter_list|)
throws|throws
name|LeaseStillHeldException
block|{
name|addLease
argument_list|(
operator|new
name|Lease
argument_list|(
name|leaseName
argument_list|,
name|listener
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Inserts lease.  Resets expiration before insertion.    * @param lease    * @throws LeaseStillHeldException    */
specifier|public
name|void
name|addLease
parameter_list|(
specifier|final
name|Lease
name|lease
parameter_list|)
throws|throws
name|LeaseStillHeldException
block|{
if|if
condition|(
name|this
operator|.
name|stopRequested
condition|)
block|{
return|return;
block|}
name|lease
operator|.
name|setExpirationTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|this
operator|.
name|leasePeriod
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|leaseQueue
init|)
block|{
if|if
condition|(
name|leases
operator|.
name|containsKey
argument_list|(
name|lease
operator|.
name|getLeaseName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|LeaseStillHeldException
argument_list|(
name|lease
operator|.
name|getLeaseName
argument_list|()
argument_list|)
throw|;
block|}
name|leases
operator|.
name|put
argument_list|(
name|lease
operator|.
name|getLeaseName
argument_list|()
argument_list|,
name|lease
argument_list|)
expr_stmt|;
name|leaseQueue
operator|.
name|add
argument_list|(
name|lease
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Thrown if we are asked create a lease but lease on passed name already    * exists.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
specifier|static
class|class
name|LeaseStillHeldException
extends|extends
name|IOException
block|{
specifier|private
specifier|final
name|String
name|leaseName
decl_stmt|;
comment|/**      * @param name      */
specifier|public
name|LeaseStillHeldException
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|leaseName
operator|=
name|name
expr_stmt|;
block|}
comment|/** @return name of lease */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|leaseName
return|;
block|}
block|}
comment|/**    * Renew a lease    *    * @param leaseName name of lease    * @throws LeaseException    */
specifier|public
name|void
name|renewLease
parameter_list|(
specifier|final
name|String
name|leaseName
parameter_list|)
throws|throws
name|LeaseException
block|{
synchronized|synchronized
init|(
name|leaseQueue
init|)
block|{
name|Lease
name|lease
init|=
name|leases
operator|.
name|get
argument_list|(
name|leaseName
argument_list|)
decl_stmt|;
comment|// We need to check to see if the remove is successful as the poll in the run()
comment|// method could have completed between the get and the remove which will result
comment|// in a corrupt leaseQueue.
if|if
condition|(
name|lease
operator|==
literal|null
operator|||
operator|!
name|leaseQueue
operator|.
name|remove
argument_list|(
name|lease
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|LeaseException
argument_list|(
literal|"lease '"
operator|+
name|leaseName
operator|+
literal|"' does not exist or has already expired"
argument_list|)
throw|;
block|}
name|lease
operator|.
name|setExpirationTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|leasePeriod
argument_list|)
expr_stmt|;
name|leaseQueue
operator|.
name|add
argument_list|(
name|lease
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Client explicitly cancels a lease.    * @param leaseName name of lease    * @throws LeaseException    */
specifier|public
name|void
name|cancelLease
parameter_list|(
specifier|final
name|String
name|leaseName
parameter_list|)
throws|throws
name|LeaseException
block|{
name|removeLease
argument_list|(
name|leaseName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove named lease.    * Lease is removed from the list of leases and removed from the delay queue.    * Lease can be resinserted using {@link #addLease(Lease)}    *    * @param leaseName name of lease    * @throws LeaseException    * @return Removed lease    */
name|Lease
name|removeLease
parameter_list|(
specifier|final
name|String
name|leaseName
parameter_list|)
throws|throws
name|LeaseException
block|{
name|Lease
name|lease
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|leaseQueue
init|)
block|{
name|lease
operator|=
name|leases
operator|.
name|remove
argument_list|(
name|leaseName
argument_list|)
expr_stmt|;
if|if
condition|(
name|lease
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|LeaseException
argument_list|(
literal|"lease '"
operator|+
name|leaseName
operator|+
literal|"' does not exist"
argument_list|)
throw|;
block|}
name|leaseQueue
operator|.
name|remove
argument_list|(
name|lease
argument_list|)
expr_stmt|;
block|}
return|return
name|lease
return|;
block|}
comment|/** This class tracks a single Lease. */
specifier|static
class|class
name|Lease
implements|implements
name|Delayed
block|{
specifier|private
specifier|final
name|String
name|leaseName
decl_stmt|;
specifier|private
specifier|final
name|LeaseListener
name|listener
decl_stmt|;
specifier|private
name|long
name|expirationTime
decl_stmt|;
name|Lease
parameter_list|(
specifier|final
name|String
name|leaseName
parameter_list|,
name|LeaseListener
name|listener
parameter_list|)
block|{
name|this
argument_list|(
name|leaseName
argument_list|,
name|listener
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|Lease
parameter_list|(
specifier|final
name|String
name|leaseName
parameter_list|,
name|LeaseListener
name|listener
parameter_list|,
name|long
name|expirationTime
parameter_list|)
block|{
name|this
operator|.
name|leaseName
operator|=
name|leaseName
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|expirationTime
operator|=
name|expirationTime
expr_stmt|;
block|}
comment|/** @return the lease name */
specifier|public
name|String
name|getLeaseName
parameter_list|()
block|{
return|return
name|leaseName
return|;
block|}
comment|/** @return listener */
specifier|public
name|LeaseListener
name|getListener
parameter_list|()
block|{
return|return
name|this
operator|.
name|listener
return|;
block|}
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
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|this
operator|.
name|hashCode
argument_list|()
operator|==
operator|(
operator|(
name|Lease
operator|)
name|obj
operator|)
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|leaseName
operator|.
name|hashCode
argument_list|()
return|;
block|}
specifier|public
name|long
name|getDelay
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|unit
operator|.
name|convert
argument_list|(
name|this
operator|.
name|expirationTime
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|Delayed
name|o
parameter_list|)
block|{
name|long
name|delta
init|=
name|this
operator|.
name|getDelay
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|-
name|o
operator|.
name|getDelay
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
return|return
name|this
operator|.
name|equals
argument_list|(
name|o
argument_list|)
condition|?
literal|0
else|:
operator|(
name|delta
operator|>
literal|0
condition|?
literal|1
else|:
operator|-
literal|1
operator|)
return|;
block|}
comment|/** @param expirationTime the expirationTime to set */
specifier|public
name|void
name|setExpirationTime
parameter_list|(
name|long
name|expirationTime
parameter_list|)
block|{
name|this
operator|.
name|expirationTime
operator|=
name|expirationTime
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

