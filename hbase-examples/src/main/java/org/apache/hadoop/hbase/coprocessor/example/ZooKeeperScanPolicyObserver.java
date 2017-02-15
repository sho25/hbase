begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|coprocessor
operator|.
name|example
package|;
end_package

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
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
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
name|CoprocessorEnvironment
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
name|HConstants
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
name|client
operator|.
name|IsolationLevel
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
name|client
operator|.
name|Scan
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionObserver
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
name|regionserver
operator|.
name|HStore
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
name|regionserver
operator|.
name|InternalScanner
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
name|regionserver
operator|.
name|KeyValueScanner
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
name|regionserver
operator|.
name|ScanInfo
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
name|regionserver
operator|.
name|ScanType
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
name|regionserver
operator|.
name|Store
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
name|regionserver
operator|.
name|StoreScanner
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|WatchedEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|Watcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
import|;
end_import

begin_comment
comment|/**  * This is an example showing how a RegionObserver could configured  * via ZooKeeper in order to control a Region compaction, flush, and scan policy.  *  * This also demonstrated the use of shared   * {@link org.apache.hadoop.hbase.coprocessor.RegionObserver} state.  * See {@link RegionCoprocessorEnvironment#getSharedData()}.  *  * This would be useful for an incremental backup tool, which would indicate the last  * time of a successful backup via ZK and instruct HBase to not delete data that was  * inserted since (based on wall clock time).   *  * This implements org.apache.zookeeper.Watcher directly instead of using  * {@link org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher},   * because RegionObservers come and go and currently  * listeners registered with ZooKeeperWatcher cannot be removed.  */
end_comment

begin_class
specifier|public
class|class
name|ZooKeeperScanPolicyObserver
implements|implements
name|RegionObserver
block|{
specifier|public
specifier|static
specifier|final
name|String
name|node
init|=
literal|"/backup/example/lastbackup"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|zkkey
init|=
literal|"ZK"
decl_stmt|;
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
name|ZooKeeperScanPolicyObserver
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Internal watcher that keep "data" up to date asynchronously.    */
specifier|private
specifier|static
class|class
name|ZKWatcher
implements|implements
name|Watcher
block|{
specifier|private
name|byte
index|[]
name|data
init|=
literal|null
decl_stmt|;
specifier|private
name|ZooKeeper
name|zk
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|needSetup
init|=
literal|true
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|lastSetupTry
init|=
literal|0
decl_stmt|;
specifier|public
name|ZKWatcher
parameter_list|(
name|ZooKeeper
name|zk
parameter_list|)
block|{
name|this
operator|.
name|zk
operator|=
name|zk
expr_stmt|;
comment|// trigger the listening
name|getData
argument_list|()
expr_stmt|;
block|}
comment|/**      * Get the maintained data. In case of any ZK exceptions this will retry      * establishing the connection (but not more than twice/minute).      *      * getData is on the critical path, so make sure it is fast unless there is      * a problem (network partion, ZK ensemble down, etc)      * Make sure at most one (unlucky) thread retries and other threads don't pile up      * while that threads tries to recreate the connection.      *      * @return the last know version of the data      */
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
literal|"REC_CATCH_EXCEPTION"
argument_list|)
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
comment|// try at most twice/minute
if|if
condition|(
name|needSetup
operator|&&
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|>
name|lastSetupTry
operator|+
literal|30000
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
comment|// make sure only one thread tries to reconnect
if|if
condition|(
name|needSetup
condition|)
block|{
name|needSetup
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
return|return
name|data
return|;
block|}
block|}
comment|// do this without the lock held to avoid threads piling up on this lock,
comment|// as it can take a while
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connecting to ZK"
argument_list|)
expr_stmt|;
comment|// record this attempt
name|lastSetupTry
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
if|if
condition|(
name|zk
operator|.
name|exists
argument_list|(
name|node
argument_list|,
literal|false
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|data
operator|=
name|zk
operator|.
name|getData
argument_list|(
name|node
argument_list|,
name|this
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Read synchronously: "
operator|+
operator|(
name|data
operator|==
literal|null
condition|?
literal|"null"
else|:
name|Bytes
operator|.
name|toLong
argument_list|(
name|data
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|zk
operator|.
name|exists
argument_list|(
name|node
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|x
parameter_list|)
block|{
comment|// try again if this fails
name|needSetup
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|data
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{
switch|switch
condition|(
name|event
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|NodeDataChanged
case|:
case|case
name|NodeCreated
case|:
try|try
block|{
comment|// get data and re-watch
name|data
operator|=
name|zk
operator|.
name|getData
argument_list|(
name|node
argument_list|,
name|this
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Read asynchronously: "
operator|+
operator|(
name|data
operator|==
literal|null
condition|?
literal|"null"
else|:
name|Bytes
operator|.
name|toLong
argument_list|(
name|data
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ix
parameter_list|)
block|{       }
catch|catch
parameter_list|(
name|KeeperException
name|kx
parameter_list|)
block|{
name|needSetup
operator|=
literal|true
expr_stmt|;
block|}
break|break;
case|case
name|NodeDeleted
case|:
try|try
block|{
comment|// just re-watch
name|zk
operator|.
name|exists
argument_list|(
name|node
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|data
operator|=
literal|null
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ix
parameter_list|)
block|{       }
catch|catch
parameter_list|(
name|KeeperException
name|kx
parameter_list|)
block|{
name|needSetup
operator|=
literal|true
expr_stmt|;
block|}
break|break;
default|default:
comment|// ignore
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionCoprocessorEnvironment
name|re
init|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|e
decl_stmt|;
if|if
condition|(
operator|!
name|re
operator|.
name|getSharedData
argument_list|()
operator|.
name|containsKey
argument_list|(
name|zkkey
argument_list|)
condition|)
block|{
comment|// there is a short race here
comment|// in the worst case we create a watcher that will be notified once
name|re
operator|.
name|getSharedData
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|zkkey
argument_list|,
operator|new
name|ZKWatcher
argument_list|(
name|re
operator|.
name|getRegionServerServices
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|ScanInfo
name|getScanInfo
parameter_list|(
name|Store
name|store
parameter_list|,
name|RegionCoprocessorEnvironment
name|e
parameter_list|)
block|{
name|byte
index|[]
name|data
init|=
operator|(
operator|(
name|ZKWatcher
operator|)
name|e
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
name|zkkey
argument_list|)
operator|)
operator|.
name|getData
argument_list|()
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ScanInfo
name|oldSI
init|=
name|store
operator|.
name|getScanInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldSI
operator|.
name|getTtl
argument_list|()
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
literal|null
return|;
block|}
name|long
name|ttl
init|=
name|Math
operator|.
name|max
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|Bytes
operator|.
name|toLong
argument_list|(
name|data
argument_list|)
argument_list|,
name|oldSI
operator|.
name|getTtl
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|ScanInfo
argument_list|(
name|oldSI
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|store
operator|.
name|getFamily
argument_list|()
argument_list|,
name|ttl
argument_list|,
name|oldSI
operator|.
name|getTimeToPurgeDeletes
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getComparator
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preFlushScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|KeyValueScanner
name|memstoreScanner
parameter_list|,
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|ScanInfo
name|scanInfo
init|=
name|getScanInfo
argument_list|(
name|store
argument_list|,
name|c
operator|.
name|getEnvironment
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanInfo
operator|==
literal|null
condition|)
block|{
comment|// take default action
return|return
literal|null
return|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|scanInfo
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|scanInfo
argument_list|,
name|scan
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|memstoreScanner
argument_list|)
argument_list|,
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
argument_list|,
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preCompactScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|long
name|earliestPutTs
parameter_list|,
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|ScanInfo
name|scanInfo
init|=
name|getScanInfo
argument_list|(
name|store
argument_list|,
name|c
operator|.
name|getEnvironment
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanInfo
operator|==
literal|null
condition|)
block|{
comment|// take default action
return|return
literal|null
return|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|scanInfo
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|scanInfo
argument_list|,
name|scan
argument_list|,
name|scanners
argument_list|,
name|scanType
argument_list|,
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
argument_list|,
name|earliestPutTs
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|KeyValueScanner
name|preStoreScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|targetCols
parameter_list|,
specifier|final
name|KeyValueScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|ScanInfo
name|scanInfo
init|=
name|getScanInfo
argument_list|(
name|store
argument_list|,
name|c
operator|.
name|getEnvironment
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanInfo
operator|==
literal|null
condition|)
block|{
comment|// take default action
return|return
literal|null
return|;
block|}
return|return
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|scanInfo
argument_list|,
name|scan
argument_list|,
name|targetCols
argument_list|,
operator|(
operator|(
name|HStore
operator|)
name|store
operator|)
operator|.
name|getHRegion
argument_list|()
operator|.
name|getReadPoint
argument_list|(
name|IsolationLevel
operator|.
name|READ_COMMITTED
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

