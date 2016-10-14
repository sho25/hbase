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
name|HConstants
operator|.
name|DEFAULT_ZK_SESSION_TIMEOUT
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
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
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
name|ClusterId
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
name|zookeeper
operator|.
name|RecoverableZooKeeper
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
name|zookeeper
operator|.
name|ZKConfig
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
name|zookeeper
operator|.
name|ZNodePaths
import|;
end_import

begin_comment
comment|/**  * Cache the cluster registry data in memory and use zk watcher to update. The only exception is  * {@link #getClusterId()}, it will fetch the data from zk directly.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ZKClusterRegistry
implements|implements
name|ClusterRegistry
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
name|ZKClusterRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RecoverableZooKeeper
name|zk
decl_stmt|;
specifier|private
specifier|final
name|ZNodePaths
name|znodePaths
decl_stmt|;
name|ZKClusterRegistry
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|znodePaths
operator|=
operator|new
name|ZNodePaths
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|zkSessionTimeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|ZK_SESSION_TIMEOUT
argument_list|,
name|DEFAULT_ZK_SESSION_TIMEOUT
argument_list|)
decl_stmt|;
name|int
name|zkRetry
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|int
name|zkRetryIntervalMs
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"zookeeper.recovery.retry.intervalmill"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|this
operator|.
name|zk
operator|=
operator|new
name|RecoverableZooKeeper
argument_list|(
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|conf
argument_list|)
argument_list|,
name|zkSessionTimeout
argument_list|,
literal|null
argument_list|,
name|zkRetry
argument_list|,
name|zkRetryIntervalMs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
try|try
block|{
name|byte
index|[]
name|data
init|=
name|zk
operator|.
name|getData
argument_list|(
name|znodePaths
operator|.
name|clusterIdZNode
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|ClusterId
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
operator|.
name|toString
argument_list|()
return|;
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
literal|"failed to get cluster id"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"close zookeeper failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

