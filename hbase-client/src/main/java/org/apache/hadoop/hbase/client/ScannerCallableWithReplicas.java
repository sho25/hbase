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
name|client
package|;
end_package

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
name|client
operator|.
name|ClientScanner
operator|.
name|createClosestRowBefore
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|Callable
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
name|CancellationException
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
name|ExecutionException
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
name|ExecutorService
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
name|Future
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
name|TimeoutException
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
name|AtomicBoolean
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
name|conf
operator|.
name|Configuration
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
name|HRegionInfo
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
name|RegionLocations
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
name|TableName
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
name|util
operator|.
name|Bytes
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
name|Pair
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
comment|/**  * This class has the logic for handling scanners for regions with and without replicas.  * 1. A scan is attempted on the default (primary) region  * 2. The scanner sends all the RPCs to the default region until it is done, or, there  * is a timeout on the default (a timeout of zero is disallowed).  * 3. If there is a timeout in (2) above, scanner(s) is opened on the non-default replica(s)  * 4. The results from the first successful scanner are taken, and it is stored which server  * returned the results.  * 5. The next RPCs are done on the above stored server until it is done or there is a timeout,  * in which case, the other replicas are queried (as in (3) above).  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ScannerCallableWithReplicas
implements|implements
name|RetryingCallable
argument_list|<
name|Result
index|[]
argument_list|>
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
name|ScannerCallableWithReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|volatile
name|ScannerCallable
name|currentScannerCallable
decl_stmt|;
name|AtomicBoolean
name|replicaSwitched
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|ClusterConnection
name|cConnection
decl_stmt|;
specifier|protected
specifier|final
name|ExecutorService
name|pool
decl_stmt|;
specifier|protected
specifier|final
name|int
name|timeBeforeReplicas
decl_stmt|;
specifier|private
specifier|final
name|Scan
name|scan
decl_stmt|;
specifier|private
specifier|final
name|int
name|retries
decl_stmt|;
specifier|private
name|Result
name|lastResult
decl_stmt|;
specifier|private
specifier|final
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|int
name|scannerTimeout
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|ScannerCallable
argument_list|>
name|outstandingCallables
init|=
operator|new
name|HashSet
argument_list|<
name|ScannerCallable
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|someRPCcancelled
init|=
literal|false
decl_stmt|;
comment|//required for testing purposes only
specifier|public
name|ScannerCallableWithReplicas
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ClusterConnection
name|cConnection
parameter_list|,
name|ScannerCallable
name|baseCallable
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|int
name|timeBeforeReplicas
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|int
name|retries
parameter_list|,
name|int
name|scannerTimeout
parameter_list|,
name|int
name|caching
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
parameter_list|)
block|{
name|this
operator|.
name|currentScannerCallable
operator|=
name|baseCallable
expr_stmt|;
name|this
operator|.
name|cConnection
operator|=
name|cConnection
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
if|if
condition|(
name|timeBeforeReplicas
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid value of operation timeout on the primary"
argument_list|)
throw|;
block|}
name|this
operator|.
name|timeBeforeReplicas
operator|=
name|timeBeforeReplicas
expr_stmt|;
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
name|this
operator|.
name|retries
operator|=
name|retries
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|scannerTimeout
operator|=
name|scannerTimeout
expr_stmt|;
name|this
operator|.
name|caller
operator|=
name|caller
expr_stmt|;
block|}
specifier|public
name|void
name|setClose
parameter_list|()
block|{
name|currentScannerCallable
operator|.
name|setClose
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setRenew
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
name|currentScannerCallable
operator|.
name|setRenew
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setCaching
parameter_list|(
name|int
name|caching
parameter_list|)
block|{
name|currentScannerCallable
operator|.
name|setCaching
argument_list|(
name|caching
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getCaching
parameter_list|()
block|{
return|return
name|currentScannerCallable
operator|.
name|getCaching
argument_list|()
return|;
block|}
specifier|public
name|HRegionInfo
name|getHRegionInfo
parameter_list|()
block|{
return|return
name|currentScannerCallable
operator|.
name|getHRegionInfo
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|getServerHasMoreResults
parameter_list|()
block|{
return|return
name|currentScannerCallable
operator|.
name|getServerHasMoreResults
argument_list|()
return|;
block|}
specifier|public
name|void
name|setServerHasMoreResults
parameter_list|(
name|boolean
name|serverHasMoreResults
parameter_list|)
block|{
name|currentScannerCallable
operator|.
name|setServerHasMoreResults
argument_list|(
name|serverHasMoreResults
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasMoreResultsContext
parameter_list|()
block|{
return|return
name|currentScannerCallable
operator|.
name|hasMoreResultsContext
argument_list|()
return|;
block|}
specifier|public
name|void
name|setHasMoreResultsContext
parameter_list|(
name|boolean
name|serverHasMoreResultsContext
parameter_list|)
block|{
name|currentScannerCallable
operator|.
name|setHasMoreResultsContext
argument_list|(
name|serverHasMoreResultsContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|call
parameter_list|(
name|int
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If the active replica callable was closed somewhere, invoke the RPC to
comment|// really close it. In the case of regular scanners, this applies. We make couple
comment|// of RPCs to a RegionServer, and when that region is exhausted, we set
comment|// the closed flag. Then an RPC is required to actually close the scanner.
if|if
condition|(
name|currentScannerCallable
operator|!=
literal|null
operator|&&
name|currentScannerCallable
operator|.
name|closed
condition|)
block|{
comment|// For closing we target that exact scanner (and not do replica fallback like in
comment|// the case of normal reads)
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Closing scanner id="
operator|+
name|currentScannerCallable
operator|.
name|scannerId
argument_list|)
expr_stmt|;
block|}
name|Result
index|[]
name|r
init|=
name|currentScannerCallable
operator|.
name|call
argument_list|(
name|timeout
argument_list|)
decl_stmt|;
name|currentScannerCallable
operator|=
literal|null
expr_stmt|;
return|return
name|r
return|;
block|}
comment|// We need to do the following:
comment|//1. When a scan goes out to a certain replica (default or not), we need to
comment|//   continue to hit that until there is a failure. So store the last successfully invoked
comment|//   replica
comment|//2. We should close the "losing" scanners (scanners other than the ones we hear back
comment|//   from first)
comment|//
name|RegionLocations
name|rl
init|=
name|RpcRetryingCallerWithReadReplicas
operator|.
name|getRegionLocations
argument_list|(
literal|true
argument_list|,
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
name|cConnection
argument_list|,
name|tableName
argument_list|,
name|currentScannerCallable
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
comment|// allocate a boundedcompletion pool of some multiple of number of replicas.
comment|// We want to accomodate some RPCs for redundant replica scans (but are still in progress)
name|ResultBoundedCompletionService
argument_list|<
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
argument_list|>
name|cs
init|=
operator|new
name|ResultBoundedCompletionService
argument_list|<
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
argument_list|>
argument_list|(
name|RpcRetryingCallerFactory
operator|.
name|instantiate
argument_list|(
name|ScannerCallableWithReplicas
operator|.
name|this
operator|.
name|conf
argument_list|)
argument_list|,
name|pool
argument_list|,
name|rl
operator|.
name|size
argument_list|()
operator|*
literal|5
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|done
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|replicaSwitched
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// submit call for the primary replica.
name|addCallsForCurrentReplica
argument_list|(
name|cs
argument_list|,
name|rl
argument_list|)
expr_stmt|;
name|int
name|startIndex
init|=
literal|0
decl_stmt|;
try|try
block|{
comment|// wait for the timeout to see whether the primary responds back
name|Future
argument_list|<
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
argument_list|>
name|f
init|=
name|cs
operator|.
name|poll
argument_list|(
name|timeBeforeReplicas
argument_list|,
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|)
decl_stmt|;
comment|// Yes, microseconds
if|if
condition|(
name|f
operator|!=
literal|null
condition|)
block|{
comment|// After poll, if f is not null, there must be a completed task
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
name|r
init|=
name|f
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|r
operator|!=
literal|null
operator|&&
name|r
operator|.
name|getSecond
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|updateCurrentlyServingReplica
argument_list|(
name|r
operator|.
name|getSecond
argument_list|()
argument_list|,
name|r
operator|.
name|getFirst
argument_list|()
argument_list|,
name|done
argument_list|,
name|pool
argument_list|)
expr_stmt|;
block|}
return|return
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|getFirst
argument_list|()
return|;
comment|//great we got a response
block|}
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
comment|// We ignore the ExecutionException and continue with the replicas
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
literal|"Scan with primary region returns "
operator|+
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// If rl's size is 1 or scan's consitency is strong, it needs to throw
comment|// out the exception from the primary replica
if|if
condition|(
operator|(
name|rl
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
operator|||
operator|(
name|scan
operator|.
name|getConsistency
argument_list|()
operator|==
name|Consistency
operator|.
name|STRONG
operator|)
condition|)
block|{
comment|// Rethrow the first exception
name|RpcRetryingCallerWithReadReplicas
operator|.
name|throwEnrichedException
argument_list|(
name|e
argument_list|,
name|retries
argument_list|)
expr_stmt|;
block|}
name|startIndex
operator|=
literal|1
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CancellationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
comment|// submit call for the all of the secondaries at once
name|int
name|endIndex
init|=
name|rl
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|scan
operator|.
name|getConsistency
argument_list|()
operator|==
name|Consistency
operator|.
name|STRONG
condition|)
block|{
comment|// When scan's consistency is strong, do not send to the secondaries
name|endIndex
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: this may be an overkill for large region replication
name|addCallsForOtherReplicas
argument_list|(
name|cs
argument_list|,
name|rl
argument_list|,
literal|0
argument_list|,
name|rl
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Future
argument_list|<
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
argument_list|>
name|f
init|=
name|cs
operator|.
name|pollForFirstSuccessfullyCompletedTask
argument_list|(
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|startIndex
argument_list|,
name|endIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to get result within timeout, timeout="
operator|+
name|timeout
operator|+
literal|"ms"
argument_list|)
throw|;
block|}
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
name|r
init|=
name|f
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|r
operator|!=
literal|null
operator|&&
name|r
operator|.
name|getSecond
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|updateCurrentlyServingReplica
argument_list|(
name|r
operator|.
name|getSecond
argument_list|()
argument_list|,
name|r
operator|.
name|getFirst
argument_list|()
argument_list|,
name|done
argument_list|,
name|pool
argument_list|)
expr_stmt|;
block|}
return|return
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|getFirst
argument_list|()
return|;
comment|// great we got an answer
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|RpcRetryingCallerWithReadReplicas
operator|.
name|throwEnrichedException
argument_list|(
name|e
argument_list|,
name|retries
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CancellationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
finally|finally
block|{
comment|// We get there because we were interrupted or because one or more of the
comment|// calls succeeded or failed. In all case, we stop all our tasks.
name|cs
operator|.
name|cancelAll
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Imposible? Arrive at an unreachable line..."
argument_list|)
expr_stmt|;
comment|// unreachable
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Imposible? Arrive at an unreachable line..."
argument_list|)
throw|;
block|}
specifier|private
name|void
name|updateCurrentlyServingReplica
parameter_list|(
name|ScannerCallable
name|scanner
parameter_list|,
name|Result
index|[]
name|result
parameter_list|,
name|AtomicBoolean
name|done
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
if|if
condition|(
name|done
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
if|if
condition|(
name|currentScannerCallable
operator|!=
name|scanner
condition|)
name|replicaSwitched
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|currentScannerCallable
operator|=
name|scanner
expr_stmt|;
comment|// store where to start the replica scanner from if we need to.
if|if
condition|(
name|result
operator|!=
literal|null
operator|&&
name|result
operator|.
name|length
operator|!=
literal|0
condition|)
name|this
operator|.
name|lastResult
operator|=
name|result
index|[
name|result
operator|.
name|length
operator|-
literal|1
index|]
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Setting current scanner as id="
operator|+
name|currentScannerCallable
operator|.
name|scannerId
operator|+
literal|" associated with replica="
operator|+
name|currentScannerCallable
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// close all outstanding replica scanners but the one we heard back from
name|outstandingCallables
operator|.
name|remove
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
for|for
control|(
name|ScannerCallable
name|s
range|:
name|outstandingCallables
control|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Closing scanner id="
operator|+
name|s
operator|.
name|scannerId
operator|+
literal|", replica="
operator|+
name|s
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getRegionId
argument_list|()
operator|+
literal|" because slow and replica="
operator|+
name|this
operator|.
name|currentScannerCallable
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
operator|+
literal|" succeeded"
argument_list|)
expr_stmt|;
block|}
comment|// Submit the "close" to the pool since this might take time, and we don't
comment|// want to wait for the "close" to happen yet. The "wait" will happen when
comment|// the table is closed (when the awaitTermination of the underlying pool is called)
name|s
operator|.
name|setClose
argument_list|()
expr_stmt|;
specifier|final
name|RetryingRPC
name|r
init|=
operator|new
name|RetryingRPC
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|pool
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|r
operator|.
name|call
argument_list|(
name|scannerTimeout
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|// now clear outstandingCallables since we scheduled a close for all the contained scanners
name|outstandingCallables
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * When a scanner switches in the middle of scanning (the 'next' call fails    * for example), the upper layer {@link ClientScanner} needs to know    */
specifier|public
name|boolean
name|switchedToADifferentReplica
parameter_list|()
block|{
return|return
name|replicaSwitched
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @return true when the most recent RPC response indicated that the response was a heartbeat    *         message. Heartbeat messages are sent back from the server when the processing of the    *         scan request exceeds a certain time threshold. Heartbeats allow the server to avoid    *         timeouts during long running scan operations.    */
specifier|public
name|boolean
name|isHeartbeatMessage
parameter_list|()
block|{
return|return
name|currentScannerCallable
operator|!=
literal|null
operator|&&
name|currentScannerCallable
operator|.
name|isHeartbeatMessage
argument_list|()
return|;
block|}
specifier|private
name|void
name|addCallsForCurrentReplica
parameter_list|(
name|ResultBoundedCompletionService
argument_list|<
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
argument_list|>
name|cs
parameter_list|,
name|RegionLocations
name|rl
parameter_list|)
block|{
name|RetryingRPC
name|retryingOnReplica
init|=
operator|new
name|RetryingRPC
argument_list|(
name|currentScannerCallable
argument_list|)
decl_stmt|;
name|outstandingCallables
operator|.
name|add
argument_list|(
name|currentScannerCallable
argument_list|)
expr_stmt|;
name|cs
operator|.
name|submit
argument_list|(
name|retryingOnReplica
argument_list|,
name|scannerTimeout
argument_list|,
name|currentScannerCallable
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addCallsForOtherReplicas
parameter_list|(
name|ResultBoundedCompletionService
argument_list|<
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
argument_list|>
name|cs
parameter_list|,
name|RegionLocations
name|rl
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|)
block|{
for|for
control|(
name|int
name|id
init|=
name|min
init|;
name|id
operator|<=
name|max
condition|;
name|id
operator|++
control|)
block|{
if|if
condition|(
name|currentScannerCallable
operator|.
name|id
operator|==
name|id
condition|)
block|{
continue|continue;
comment|//this was already scheduled earlier
block|}
name|ScannerCallable
name|s
init|=
name|currentScannerCallable
operator|.
name|getScannerCallableForReplica
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|setStartRowForReplicaCallable
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|outstandingCallables
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|RetryingRPC
name|retryingOnReplica
init|=
operator|new
name|RetryingRPC
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|cs
operator|.
name|submit
argument_list|(
name|retryingOnReplica
argument_list|,
name|scannerTimeout
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Set the start row for the replica callable based on the state of the last result received.    * @param callable The callable to set the start row on    */
specifier|private
name|void
name|setStartRowForReplicaCallable
parameter_list|(
name|ScannerCallable
name|callable
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|lastResult
operator|==
literal|null
operator|||
name|callable
operator|==
literal|null
condition|)
return|return;
if|if
condition|(
name|this
operator|.
name|lastResult
operator|.
name|isPartial
argument_list|()
condition|)
block|{
comment|// The last result was a partial result which means we have not received all of the cells
comment|// for this row. Thus, use the last result's row as the start row. If a replica switch
comment|// occurs, the scanner will ensure that any accumulated partial results are cleared,
comment|// and the scan can resume from this row.
name|callable
operator|.
name|getScan
argument_list|()
operator|.
name|setStartRow
argument_list|(
name|this
operator|.
name|lastResult
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// The last result was not a partial result which means it contained all of the cells for
comment|// that row (we no longer need any information from it). Set the start row to the next
comment|// closest row that could be seen.
if|if
condition|(
name|callable
operator|.
name|getScan
argument_list|()
operator|.
name|isReversed
argument_list|()
condition|)
block|{
name|callable
operator|.
name|getScan
argument_list|()
operator|.
name|setStartRow
argument_list|(
name|createClosestRowBefore
argument_list|(
name|this
operator|.
name|lastResult
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|callable
operator|.
name|getScan
argument_list|()
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|this
operator|.
name|lastResult
operator|.
name|getRow
argument_list|()
argument_list|,
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|VisibleForTesting
name|boolean
name|isAnyRPCcancelled
parameter_list|()
block|{
return|return
name|someRPCcancelled
return|;
block|}
class|class
name|RetryingRPC
implements|implements
name|RetryingCallable
argument_list|<
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
argument_list|>
implements|,
name|Cancellable
block|{
specifier|final
name|ScannerCallable
name|callable
decl_stmt|;
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|cancelled
init|=
literal|false
decl_stmt|;
name|RetryingRPC
parameter_list|(
name|ScannerCallable
name|callable
parameter_list|)
block|{
name|this
operator|.
name|callable
operator|=
name|callable
expr_stmt|;
comment|// For the Consistency.STRONG (default case), we reuse the caller
comment|// to keep compatibility with what is done in the past
comment|// For the Consistency.TIMELINE case, we can't reuse the caller
comment|// since we could be making parallel RPCs (caller.callWithRetries is synchronized
comment|// and we can't invoke it multiple times at the same time)
name|this
operator|.
name|caller
operator|=
name|ScannerCallableWithReplicas
operator|.
name|this
operator|.
name|caller
expr_stmt|;
if|if
condition|(
name|scan
operator|.
name|getConsistency
argument_list|()
operator|==
name|Consistency
operator|.
name|TIMELINE
condition|)
block|{
name|this
operator|.
name|caller
operator|=
name|RpcRetryingCallerFactory
operator|.
name|instantiate
argument_list|(
name|ScannerCallableWithReplicas
operator|.
name|this
operator|.
name|conf
argument_list|)
operator|.
operator|<
name|Result
index|[]
operator|>
name|newCaller
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
name|call
parameter_list|(
name|int
name|callTimeout
parameter_list|)
throws|throws
name|IOException
block|{
comment|// since the retries is done within the ResultBoundedCompletionService,
comment|// we don't invoke callWithRetries here
if|if
condition|(
name|cancelled
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Result
index|[]
name|res
init|=
name|this
operator|.
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|this
operator|.
name|callable
argument_list|,
name|callTimeout
argument_list|)
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<
name|Result
index|[]
argument_list|,
name|ScannerCallable
argument_list|>
argument_list|(
name|res
argument_list|,
name|this
operator|.
name|callable
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|cancelled
condition|)
return|return;
if|if
condition|(
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
name|callable
operator|.
name|prepare
argument_list|(
name|reload
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|throwable
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|boolean
name|retrying
parameter_list|)
block|{
name|callable
operator|.
name|throwable
argument_list|(
name|t
argument_list|,
name|retrying
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getExceptionMessageAdditionalDetail
parameter_list|()
block|{
return|return
name|callable
operator|.
name|getExceptionMessageAdditionalDetail
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|sleep
parameter_list|(
name|long
name|pause
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
return|return
name|callable
operator|.
name|sleep
argument_list|(
name|pause
argument_list|,
name|tries
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancel
parameter_list|()
block|{
name|cancelled
operator|=
literal|true
expr_stmt|;
name|caller
operator|.
name|cancel
argument_list|()
expr_stmt|;
if|if
condition|(
name|callable
operator|.
name|getRpcController
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|callable
operator|.
name|getRpcController
argument_list|()
operator|.
name|startCancel
argument_list|()
expr_stmt|;
block|}
name|someRPCcancelled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCancelled
parameter_list|()
block|{
return|return
name|cancelled
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|throwable
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|boolean
name|retrying
parameter_list|)
block|{
name|currentScannerCallable
operator|.
name|throwable
argument_list|(
name|t
argument_list|,
name|retrying
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getExceptionMessageAdditionalDetail
parameter_list|()
block|{
return|return
name|currentScannerCallable
operator|.
name|getExceptionMessageAdditionalDetail
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|sleep
parameter_list|(
name|long
name|pause
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
return|return
name|currentScannerCallable
operator|.
name|sleep
argument_list|(
name|pause
argument_list|,
name|tries
argument_list|)
return|;
block|}
block|}
end_class

end_unit

