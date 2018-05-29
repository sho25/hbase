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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|Map
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|FilterFileSystem
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
name|fs
operator|.
name|Path
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|hdfs
operator|.
name|server
operator|.
name|namenode
operator|.
name|LeaseExpiredException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_comment
comment|/**  * Implementation for hdfs  */
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
class|class
name|FSHDFSUtils
extends|extends
name|FSUtils
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FSHDFSUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Class
name|dfsUtilClazz
decl_stmt|;
specifier|private
specifier|static
name|Method
name|getNNAddressesMethod
decl_stmt|;
comment|/**    * @param fs    * @param conf    * @return A set containing all namenode addresses of fs    */
specifier|private
specifier|static
name|Set
argument_list|<
name|InetSocketAddress
argument_list|>
name|getNNAddresses
parameter_list|(
name|DistributedFileSystem
name|fs
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|Set
argument_list|<
name|InetSocketAddress
argument_list|>
name|addresses
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|serviceName
init|=
name|fs
operator|.
name|getCanonicalServiceName
argument_list|()
decl_stmt|;
if|if
condition|(
name|serviceName
operator|.
name|startsWith
argument_list|(
literal|"ha-hdfs"
argument_list|)
condition|)
block|{
try|try
block|{
if|if
condition|(
name|dfsUtilClazz
operator|==
literal|null
condition|)
block|{
name|dfsUtilClazz
operator|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hdfs.DFSUtil"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getNNAddressesMethod
operator|==
literal|null
condition|)
block|{
try|try
block|{
comment|// getNNServiceRpcAddressesForCluster is available only in version
comment|// equal to or later than Hadoop 2.6
name|getNNAddressesMethod
operator|=
name|dfsUtilClazz
operator|.
name|getMethod
argument_list|(
literal|"getNNServiceRpcAddressesForCluster"
argument_list|,
name|Configuration
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
comment|// If hadoop version is older than hadoop 2.6
name|getNNAddressesMethod
operator|=
name|dfsUtilClazz
operator|.
name|getMethod
argument_list|(
literal|"getNNServiceRpcAddresses"
argument_list|,
name|Configuration
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|InetSocketAddress
argument_list|>
argument_list|>
name|addressMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|InetSocketAddress
argument_list|>
argument_list|>
operator|)
name|getNNAddressesMethod
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|String
name|nameService
init|=
name|serviceName
operator|.
name|substring
argument_list|(
name|serviceName
operator|.
name|indexOf
argument_list|(
literal|":"
argument_list|)
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|addressMap
operator|.
name|containsKey
argument_list|(
name|nameService
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|InetSocketAddress
argument_list|>
name|nnMap
init|=
name|addressMap
operator|.
name|get
argument_list|(
name|nameService
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|InetSocketAddress
argument_list|>
name|e2
range|:
name|nnMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|InetSocketAddress
name|addr
init|=
name|e2
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|addresses
operator|.
name|add
argument_list|(
name|addr
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"DFSUtil.getNNServiceRpcAddresses failed. serviceName="
operator|+
name|serviceName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|URI
name|uri
init|=
name|fs
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|uri
operator|.
name|getPort
argument_list|()
decl_stmt|;
if|if
condition|(
name|port
operator|<
literal|0
condition|)
block|{
name|int
name|idx
init|=
name|serviceName
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|port
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|serviceName
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|InetSocketAddress
name|addr
init|=
operator|new
name|InetSocketAddress
argument_list|(
name|uri
operator|.
name|getHost
argument_list|()
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|addresses
operator|.
name|add
argument_list|(
name|addr
argument_list|)
expr_stmt|;
block|}
return|return
name|addresses
return|;
block|}
comment|/**    * @param conf the Configuration of HBase    * @param srcFs    * @param desFs    * @return Whether srcFs and desFs are on same hdfs or not    */
specifier|public
specifier|static
name|boolean
name|isSameHdfs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|srcFs
parameter_list|,
name|FileSystem
name|desFs
parameter_list|)
block|{
comment|// By getCanonicalServiceName, we could make sure both srcFs and desFs
comment|// show a unified format which contains scheme, host and port.
name|String
name|srcServiceName
init|=
name|srcFs
operator|.
name|getCanonicalServiceName
argument_list|()
decl_stmt|;
name|String
name|desServiceName
init|=
name|desFs
operator|.
name|getCanonicalServiceName
argument_list|()
decl_stmt|;
if|if
condition|(
name|srcServiceName
operator|==
literal|null
operator|||
name|desServiceName
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
name|srcServiceName
operator|.
name|equals
argument_list|(
name|desServiceName
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|srcServiceName
operator|.
name|startsWith
argument_list|(
literal|"ha-hdfs"
argument_list|)
operator|&&
name|desServiceName
operator|.
name|startsWith
argument_list|(
literal|"ha-hdfs"
argument_list|)
condition|)
block|{
name|Collection
argument_list|<
name|String
argument_list|>
name|internalNameServices
init|=
name|conf
operator|.
name|getTrimmedStringCollection
argument_list|(
literal|"dfs.internal.nameservices"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|internalNameServices
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|internalNameServices
operator|.
name|contains
argument_list|(
name|srcServiceName
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
index|[
literal|1
index|]
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
if|if
condition|(
name|srcFs
operator|instanceof
name|DistributedFileSystem
operator|&&
name|desFs
operator|instanceof
name|DistributedFileSystem
condition|)
block|{
comment|//If one serviceName is an HA format while the other is a non-HA format,
comment|// maybe they refer to the same FileSystem.
comment|//For example, srcFs is "ha-hdfs://nameservices" and desFs is "hdfs://activeNamenode:port"
name|Set
argument_list|<
name|InetSocketAddress
argument_list|>
name|srcAddrs
init|=
name|getNNAddresses
argument_list|(
operator|(
name|DistributedFileSystem
operator|)
name|srcFs
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|InetSocketAddress
argument_list|>
name|desAddrs
init|=
name|getNNAddresses
argument_list|(
operator|(
name|DistributedFileSystem
operator|)
name|desFs
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|Sets
operator|.
name|intersection
argument_list|(
name|srcAddrs
argument_list|,
name|desAddrs
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Recover the lease from HDFS, retrying multiple times.    */
annotation|@
name|Override
specifier|public
name|void
name|recoverFileLease
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|p
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|CancelableProgressable
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fs
operator|instanceof
name|FilterFileSystem
condition|)
block|{
name|fs
operator|=
operator|(
operator|(
name|FilterFileSystem
operator|)
name|fs
operator|)
operator|.
name|getRawFileSystem
argument_list|()
expr_stmt|;
block|}
comment|// lease recovery not needed for local file system case.
if|if
condition|(
operator|!
operator|(
name|fs
operator|instanceof
name|DistributedFileSystem
operator|)
condition|)
block|{
return|return;
block|}
name|recoverDFSFileLease
argument_list|(
operator|(
name|DistributedFileSystem
operator|)
name|fs
argument_list|,
name|p
argument_list|,
name|conf
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
block|}
comment|/*    * Run the dfs recover lease. recoverLease is asynchronous. It returns:    *    -false when it starts the lease recovery (i.e. lease recovery not *yet* done)    *    - true when the lease recovery has succeeded or the file is closed.    * But, we have to be careful.  Each time we call recoverLease, it starts the recover lease    * process over from the beginning.  We could put ourselves in a situation where we are    * doing nothing but starting a recovery, interrupting it to start again, and so on.    * The findings over in HBASE-8354 have it that the namenode will try to recover the lease    * on the file's primary node.  If all is well, it should return near immediately.  But,    * as is common, it is the very primary node that has crashed and so the namenode will be    * stuck waiting on a socket timeout before it will ask another datanode to start the    * recovery. It does not help if we call recoverLease in the meantime and in particular,    * subsequent to the socket timeout, a recoverLease invocation will cause us to start    * over from square one (possibly waiting on socket timeout against primary node).  So,    * in the below, we do the following:    * 1. Call recoverLease.    * 2. If it returns true, break.    * 3. If it returns false, wait a few seconds and then call it again.    * 4. If it returns true, break.    * 5. If it returns false, wait for what we think the datanode socket timeout is    * (configurable) and then try again.    * 6. If it returns true, break.    * 7. If it returns false, repeat starting at step 5. above.    *    * If HDFS-4525 is available, call it every second and we might be able to exit early.    */
name|boolean
name|recoverDFSFileLease
parameter_list|(
specifier|final
name|DistributedFileSystem
name|dfs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|CancelableProgressable
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Recover lease on dfs file "
operator|+
name|p
argument_list|)
expr_stmt|;
name|long
name|startWaiting
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
comment|// Default is 15 minutes. It's huge, but the idea is that if we have a major issue, HDFS
comment|// usually needs 10 minutes before marking the nodes as dead. So we're putting ourselves
comment|// beyond that limit 'to be safe'.
name|long
name|recoveryTimeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.lease.recovery.timeout"
argument_list|,
literal|900000
argument_list|)
operator|+
name|startWaiting
decl_stmt|;
comment|// This setting should be a little bit above what the cluster dfs heartbeat is set to.
name|long
name|firstPause
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.lease.recovery.first.pause"
argument_list|,
literal|4000
argument_list|)
decl_stmt|;
comment|// This should be set to how long it'll take for us to timeout against primary datanode if it
comment|// is dead.  We set it to 64 seconds, 4 second than the default READ_TIMEOUT in HDFS, the
comment|// default value for DFS_CLIENT_SOCKET_TIMEOUT_KEY. If recovery is still failing after this
comment|// timeout, then further recovery will take liner backoff with this base, to avoid endless
comment|// preemptions when this value is not properly configured.
name|long
name|subsequentPauseBase
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.lease.recovery.dfs.timeout"
argument_list|,
literal|64
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|Method
name|isFileClosedMeth
init|=
literal|null
decl_stmt|;
comment|// whether we need to look for isFileClosed method
name|boolean
name|findIsFileClosedMeth
init|=
literal|true
decl_stmt|;
name|boolean
name|recovered
init|=
literal|false
decl_stmt|;
comment|// We break the loop if we succeed the lease recovery, timeout, or we throw an exception.
for|for
control|(
name|int
name|nbAttempt
init|=
literal|0
init|;
operator|!
name|recovered
condition|;
name|nbAttempt
operator|++
control|)
block|{
name|recovered
operator|=
name|recoverLease
argument_list|(
name|dfs
argument_list|,
name|nbAttempt
argument_list|,
name|p
argument_list|,
name|startWaiting
argument_list|)
expr_stmt|;
if|if
condition|(
name|recovered
condition|)
break|break;
name|checkIfCancelled
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
if|if
condition|(
name|checkIfTimedout
argument_list|(
name|conf
argument_list|,
name|recoveryTimeout
argument_list|,
name|nbAttempt
argument_list|,
name|p
argument_list|,
name|startWaiting
argument_list|)
condition|)
break|break;
try|try
block|{
comment|// On the first time through wait the short 'firstPause'.
if|if
condition|(
name|nbAttempt
operator|==
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|firstPause
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Cycle here until (subsequentPause * nbAttempt) elapses.  While spinning, check
comment|// isFileClosed if available (should be in hadoop 2.0.5... not in hadoop 1 though.
name|long
name|localStartWaiting
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|localStartWaiting
operator|)
operator|<
name|subsequentPauseBase
operator|*
name|nbAttempt
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.lease.recovery.pause"
argument_list|,
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|findIsFileClosedMeth
condition|)
block|{
try|try
block|{
name|isFileClosedMeth
operator|=
name|dfs
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"isFileClosed"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Path
operator|.
name|class
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|nsme
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"isFileClosed not available"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|findIsFileClosedMeth
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|isFileClosedMeth
operator|!=
literal|null
operator|&&
name|isFileClosed
argument_list|(
name|dfs
argument_list|,
name|isFileClosedMeth
argument_list|,
name|p
argument_list|)
condition|)
block|{
name|recovered
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|checkIfCancelled
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|InterruptedIOException
name|iioe
init|=
operator|new
name|InterruptedIOException
argument_list|()
decl_stmt|;
name|iioe
operator|.
name|initCause
argument_list|(
name|ie
argument_list|)
expr_stmt|;
throw|throw
name|iioe
throw|;
block|}
block|}
return|return
name|recovered
return|;
block|}
name|boolean
name|checkIfTimedout
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|long
name|recoveryTimeout
parameter_list|,
specifier|final
name|int
name|nbAttempt
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|long
name|startWaiting
parameter_list|)
block|{
if|if
condition|(
name|recoveryTimeout
operator|<
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot recoverLease after trying for "
operator|+
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.lease.recovery.timeout"
argument_list|,
literal|900000
argument_list|)
operator|+
literal|"ms (hbase.lease.recovery.timeout); continuing, but may be DATALOSS!!!; "
operator|+
name|getLogMessageDetail
argument_list|(
name|nbAttempt
argument_list|,
name|p
argument_list|,
name|startWaiting
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Try to recover the lease.    * @param dfs    * @param nbAttempt    * @param p    * @param startWaiting    * @return True if dfs#recoverLease came by true.    * @throws FileNotFoundException    */
name|boolean
name|recoverLease
parameter_list|(
specifier|final
name|DistributedFileSystem
name|dfs
parameter_list|,
specifier|final
name|int
name|nbAttempt
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|long
name|startWaiting
parameter_list|)
throws|throws
name|FileNotFoundException
block|{
name|boolean
name|recovered
init|=
literal|false
decl_stmt|;
try|try
block|{
name|recovered
operator|=
name|dfs
operator|.
name|recoverLease
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
operator|(
name|recovered
condition|?
literal|"Recovered lease, "
else|:
literal|"Failed to recover lease, "
operator|)
operator|+
name|getLogMessageDetail
argument_list|(
name|nbAttempt
argument_list|,
name|p
argument_list|,
name|startWaiting
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|LeaseExpiredException
operator|&&
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"File does not exist"
argument_list|)
condition|)
block|{
comment|// This exception comes out instead of FNFE, fix it
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"The given WAL wasn't found at "
operator|+
name|p
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|FileNotFoundException
condition|)
block|{
throw|throw
operator|(
name|FileNotFoundException
operator|)
name|e
throw|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
name|getLogMessageDetail
argument_list|(
name|nbAttempt
argument_list|,
name|p
argument_list|,
name|startWaiting
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|recovered
return|;
block|}
comment|/**    * @param nbAttempt    * @param p    * @param startWaiting    * @return Detail to append to any log message around lease recovering.    */
specifier|private
name|String
name|getLogMessageDetail
parameter_list|(
specifier|final
name|int
name|nbAttempt
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|long
name|startWaiting
parameter_list|)
block|{
return|return
literal|"attempt="
operator|+
name|nbAttempt
operator|+
literal|" on file="
operator|+
name|p
operator|+
literal|" after "
operator|+
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startWaiting
operator|)
operator|+
literal|"ms"
return|;
block|}
comment|/**    * Call HDFS-4525 isFileClosed if it is available.    * @param dfs    * @param m    * @param p    * @return True if file is closed.    */
specifier|private
name|boolean
name|isFileClosed
parameter_list|(
specifier|final
name|DistributedFileSystem
name|dfs
parameter_list|,
specifier|final
name|Method
name|m
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
block|{
try|try
block|{
return|return
operator|(
name|Boolean
operator|)
name|m
operator|.
name|invoke
argument_list|(
name|dfs
argument_list|,
name|p
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No access"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed invocation for "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
name|void
name|checkIfCancelled
parameter_list|(
specifier|final
name|CancelableProgressable
name|reporter
parameter_list|)
throws|throws
name|InterruptedIOException
block|{
if|if
condition|(
name|reporter
operator|==
literal|null
condition|)
return|return;
if|if
condition|(
operator|!
name|reporter
operator|.
name|progress
argument_list|()
condition|)
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Operation cancelled"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

