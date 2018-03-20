begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|OptionalLong
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFramework
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFrameworkFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|recipes
operator|.
name|cache
operator|.
name|ChildData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|recipes
operator|.
name|cache
operator|.
name|NodeCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|retry
operator|.
name|RetryForever
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
name|RegionCoprocessor
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
name|FlushLifeCycleTracker
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
name|ScanOptions
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
name|compactions
operator|.
name|CompactionLifeCycleTracker
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
name|compactions
operator|.
name|CompactionRequest
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * This is an example showing how a RegionObserver could configured via ZooKeeper in order to  * control a Region compaction, flush, and scan policy. This also demonstrated the use of shared  * {@link org.apache.hadoop.hbase.coprocessor.RegionObserver} state. See  * {@link RegionCoprocessorEnvironment#getSharedData()}.  *<p>  * This would be useful for an incremental backup tool, which would indicate the last time of a  * successful backup via ZK and instruct HBase that to safely delete the data which has already been  * backup.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZooKeeperScanPolicyObserver
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|// The zk ensemble info is put in hbase config xml with given custom key.
specifier|public
specifier|static
specifier|final
name|String
name|ZK_ENSEMBLE_KEY
init|=
literal|"ZooKeeperScanPolicyObserver.zookeeper.ensemble"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZK_SESSION_TIMEOUT_KEY
init|=
literal|"ZooKeeperScanPolicyObserver.zookeeper.session.timeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|ZK_SESSION_TIMEOUT_DEFAULT
init|=
literal|30
operator|*
literal|1000
decl_stmt|;
comment|// 30 secs
specifier|public
specifier|static
specifier|final
name|String
name|NODE
init|=
literal|"/backup/example/lastbackup"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ZKKEY
init|=
literal|"ZK"
decl_stmt|;
specifier|private
name|NodeCache
name|cache
decl_stmt|;
comment|/**    * Internal watcher that keep "data" up to date asynchronously.    */
specifier|private
specifier|static
specifier|final
class|class
name|ZKDataHolder
block|{
specifier|private
specifier|final
name|String
name|ensemble
decl_stmt|;
specifier|private
specifier|final
name|int
name|sessionTimeout
decl_stmt|;
specifier|private
name|CuratorFramework
name|client
decl_stmt|;
specifier|private
name|NodeCache
name|cache
decl_stmt|;
specifier|private
name|int
name|ref
decl_stmt|;
specifier|public
name|ZKDataHolder
parameter_list|(
name|String
name|ensemble
parameter_list|,
name|int
name|sessionTimeout
parameter_list|)
block|{
name|this
operator|.
name|ensemble
operator|=
name|ensemble
expr_stmt|;
name|this
operator|.
name|sessionTimeout
operator|=
name|sessionTimeout
expr_stmt|;
block|}
specifier|private
name|void
name|create
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|=
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|connectString
argument_list|(
name|ensemble
argument_list|)
operator|.
name|sessionTimeoutMs
argument_list|(
name|sessionTimeout
argument_list|)
operator|.
name|retryPolicy
argument_list|(
operator|new
name|RetryForever
argument_list|(
literal|1000
argument_list|)
argument_list|)
operator|.
name|canBeReadOnly
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|client
operator|.
name|start
argument_list|()
expr_stmt|;
name|cache
operator|=
operator|new
name|NodeCache
argument_list|(
name|client
argument_list|,
name|NODE
argument_list|)
expr_stmt|;
name|cache
operator|.
name|start
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|cache
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|cache
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// should not happen
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|cache
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|client
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|NodeCache
name|acquire
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|ref
operator|==
literal|0
condition|)
block|{
try|try
block|{
name|create
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|close
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
name|ref
operator|++
expr_stmt|;
return|return
name|cache
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|release
parameter_list|()
block|{
name|ref
operator|--
expr_stmt|;
if|if
condition|(
name|ref
operator|==
literal|0
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
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
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionCoprocessorEnvironment
name|renv
init|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
decl_stmt|;
try|try
block|{
name|this
operator|.
name|cache
operator|=
operator|(
operator|(
name|ZKDataHolder
operator|)
name|renv
operator|.
name|getSharedData
argument_list|()
operator|.
name|computeIfAbsent
argument_list|(
name|ZKKEY
argument_list|,
name|k
lambda|->
block|{
name|String
name|ensemble
init|=
name|renv
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|ZK_ENSEMBLE_KEY
argument_list|)
decl_stmt|;
name|int
name|sessionTimeout
init|=
name|renv
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|ZK_SESSION_TIMEOUT_KEY
argument_list|,
name|ZK_SESSION_TIMEOUT_DEFAULT
argument_list|)
decl_stmt|;
return|return
operator|new
name|ZKDataHolder
argument_list|(
name|ensemble
argument_list|,
name|sessionTimeout
argument_list|)
return|;
block|}
argument_list|)
operator|)
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionCoprocessorEnvironment
name|renv
init|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
decl_stmt|;
name|this
operator|.
name|cache
operator|=
literal|null
expr_stmt|;
operator|(
operator|(
name|ZKDataHolder
operator|)
name|renv
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
name|ZKKEY
argument_list|)
operator|)
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
specifier|private
name|OptionalLong
name|getExpireBefore
parameter_list|()
block|{
name|ChildData
name|data
init|=
name|cache
operator|.
name|getCurrentData
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
name|OptionalLong
operator|.
name|empty
argument_list|()
return|;
block|}
name|byte
index|[]
name|bytes
init|=
name|data
operator|.
name|getData
argument_list|()
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|!=
name|Long
operator|.
name|BYTES
condition|)
block|{
return|return
name|OptionalLong
operator|.
name|empty
argument_list|()
return|;
block|}
return|return
name|OptionalLong
operator|.
name|of
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|bytes
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|resetTTL
parameter_list|(
name|ScanOptions
name|options
parameter_list|)
block|{
name|OptionalLong
name|expireBefore
init|=
name|getExpireBefore
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|expireBefore
operator|.
name|isPresent
argument_list|()
condition|)
block|{
return|return;
block|}
name|options
operator|.
name|setTTL
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|expireBefore
operator|.
name|getAsLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preFlushScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|ScanOptions
name|options
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
name|resetTTL
argument_list|(
name|options
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCompactScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|ScanOptions
name|options
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|resetTTL
argument_list|(
name|options
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

