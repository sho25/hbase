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
name|zookeeper
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|util
operator|.
name|Properties
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
name|HBaseClassTestRule
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
name|HBaseConfiguration
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
name|testclassification
operator|.
name|MiscTests
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestZKConfig
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestZKConfig
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testZKConfigLoading
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// Test that we read only from the config instance
comment|// (i.e. via hbase-default.xml and hbase-site.xml)
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
literal|2181
argument_list|)
expr_stmt|;
name|Properties
name|props
init|=
name|ZKConfig
operator|.
name|makeZKProps
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Property client port should have been default from the HBase config"
argument_list|,
literal|"2181"
argument_list|,
name|props
operator|.
name|getProperty
argument_list|(
literal|"clientPort"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetZooKeeperClusterKey
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
literal|"\tlocalhost\n"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
literal|"3333"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|String
name|clusterKey
init|=
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
operator|!
name|clusterKey
operator|.
name|contains
argument_list|(
literal|"\t"
argument_list|)
operator|&&
operator|!
name|clusterKey
operator|.
name|contains
argument_list|(
literal|"\n"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"localhost:3333:hbase,test"
argument_list|,
name|clusterKey
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClusterKey
parameter_list|()
throws|throws
name|Exception
block|{
name|testKey
argument_list|(
literal|"server"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|)
expr_stmt|;
name|testKey
argument_list|(
literal|"server1,server2,server3"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKConfig
operator|.
name|validateClusterKey
argument_list|(
literal|"2181:/hbase"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// OK
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClusterKeyWithMultiplePorts
parameter_list|()
throws|throws
name|Exception
block|{
comment|// server has different port than the default port
name|testKey
argument_list|(
literal|"server1:2182"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// multiple servers have their own port
name|testKey
argument_list|(
literal|"server1:2182,server2:2183,server3:2184"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// one server has no specified port, should use default port
name|testKey
argument_list|(
literal|"server1:2182,server2,server3:2184"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// the last server has no specified port, should use default port
name|testKey
argument_list|(
literal|"server1:2182,server2:2183,server3"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// multiple servers have no specified port, should use default port for those servers
name|testKey
argument_list|(
literal|"server1:2182,server2,server3:2184,server4"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// same server, different ports
name|testKey
argument_list|(
literal|"server1:2182,server1:2183,server1"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// mix of same server/different port and different server
name|testKey
argument_list|(
literal|"server1:2182,server2:2183,server1"
argument_list|,
literal|2181
argument_list|,
literal|"/hbase"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testKey
parameter_list|(
name|String
name|ensemble
parameter_list|,
name|int
name|port
parameter_list|,
name|String
name|znode
parameter_list|)
throws|throws
name|IOException
block|{
name|testKey
argument_list|(
name|ensemble
argument_list|,
name|port
argument_list|,
name|znode
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// not support multiple client ports
block|}
specifier|private
name|void
name|testKey
parameter_list|(
name|String
name|ensemble
parameter_list|,
name|int
name|port
parameter_list|,
name|String
name|znode
parameter_list|,
name|Boolean
name|multiplePortSupport
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|String
name|key
init|=
name|ensemble
operator|+
literal|":"
operator|+
name|port
operator|+
literal|":"
operator|+
name|znode
decl_stmt|;
name|String
name|ensemble2
init|=
literal|null
decl_stmt|;
name|ZKConfig
operator|.
name|ZKClusterKey
name|zkClusterKey
init|=
name|ZKConfig
operator|.
name|transformClusterKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|multiplePortSupport
condition|)
block|{
name|ensemble2
operator|=
name|ZKConfig
operator|.
name|standardizeZKQuorumServerString
argument_list|(
name|ensemble
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|port
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ensemble2
argument_list|,
name|zkClusterKey
operator|.
name|getQuorumString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|ensemble
argument_list|,
name|zkClusterKey
operator|.
name|getQuorumString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|port
argument_list|,
name|zkClusterKey
operator|.
name|getClientPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|znode
argument_list|,
name|zkClusterKey
operator|.
name|getZnodeParent
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|createClusterConf
argument_list|(
name|conf
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|zkClusterKey
operator|.
name|getQuorumString
argument_list|()
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|zkClusterKey
operator|.
name|getClientPort
argument_list|()
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|zkClusterKey
operator|.
name|getZnodeParent
argument_list|()
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|reconstructedKey
init|=
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|multiplePortSupport
condition|)
block|{
name|String
name|key2
init|=
name|ensemble2
operator|+
literal|":"
operator|+
name|port
operator|+
literal|":"
operator|+
name|znode
decl_stmt|;
name|assertEquals
argument_list|(
name|key2
argument_list|,
name|reconstructedKey
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|key
argument_list|,
name|reconstructedKey
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

