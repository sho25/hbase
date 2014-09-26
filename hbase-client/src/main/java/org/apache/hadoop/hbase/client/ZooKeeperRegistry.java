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
name|HRegionLocation
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
name|HTableDescriptor
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
name|ServerName
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
name|zookeeper
operator|.
name|MetaTableLocator
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
name|ZKClusterId
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
name|ZKUtil
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

begin_comment
comment|/**  * A cluster registry that stores to zookeeper.  */
end_comment

begin_class
class|class
name|ZooKeeperRegistry
implements|implements
name|Registry
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ZooKeeperRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Needs an instance of hci to function.  Set after construct this instance.
name|ConnectionManager
operator|.
name|HConnectionImplementation
name|hci
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Connection
name|connection
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|connection
operator|instanceof
name|ConnectionManager
operator|.
name|HConnectionImplementation
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"This registry depends on HConnectionImplementation"
argument_list|)
throw|;
block|}
name|this
operator|.
name|hci
operator|=
operator|(
name|ConnectionManager
operator|.
name|HConnectionImplementation
operator|)
name|connection
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RegionLocations
name|getMetaRegionLocation
parameter_list|()
throws|throws
name|IOException
block|{
name|ZooKeeperKeepAliveConnection
name|zkw
init|=
name|hci
operator|.
name|getKeepAliveZooKeeperWatcher
argument_list|()
decl_stmt|;
try|try
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
literal|"Looking up meta region location in ZK,"
operator|+
literal|" connection="
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
name|ServerName
name|servername
init|=
operator|new
name|MetaTableLocator
argument_list|()
operator|.
name|blockUntilAvailable
argument_list|(
name|zkw
argument_list|,
name|hci
operator|.
name|rpcTimeout
argument_list|)
decl_stmt|;
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
literal|"Looked up meta region location, connection="
operator|+
name|this
operator|+
literal|"; serverName="
operator|+
operator|(
operator|(
name|servername
operator|==
literal|null
operator|)
condition|?
literal|"null"
else|:
name|servername
operator|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|servername
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|HRegionLocation
name|loc
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|servername
argument_list|,
literal|0
argument_list|)
decl_stmt|;
return|return
operator|new
name|RegionLocations
argument_list|(
operator|new
name|HRegionLocation
index|[]
block|{
name|loc
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|clusterId
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|clusterId
operator|!=
literal|null
condition|)
return|return
name|this
operator|.
name|clusterId
return|;
comment|// No synchronized here, worse case we will retrieve it twice, that's
comment|//  not an issue.
name|ZooKeeperKeepAliveConnection
name|zkw
init|=
literal|null
decl_stmt|;
try|try
block|{
name|zkw
operator|=
name|hci
operator|.
name|getKeepAliveZooKeeperWatcher
argument_list|()
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|ZKClusterId
operator|.
name|readClusterIdZNode
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|clusterId
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ClusterId read in ZooKeeper is null"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't retrieve clusterId from Zookeeper"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't retrieve clusterId from Zookeeper"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|zkw
operator|!=
literal|null
condition|)
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|clusterId
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getCurrentNrHRS
parameter_list|()
throws|throws
name|IOException
block|{
name|ZooKeeperKeepAliveConnection
name|zkw
init|=
name|hci
operator|.
name|getKeepAliveZooKeeperWatcher
argument_list|()
decl_stmt|;
try|try
block|{
comment|// We go to zk rather than to master to get count of regions to avoid
comment|// HTable having a Master dependency.  See HBase-2828
return|return
name|ZKUtil
operator|.
name|getNumberOfChildren
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|rsZNode
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected ZooKeeper exception"
argument_list|,
name|ke
argument_list|)
throw|;
block|}
finally|finally
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

