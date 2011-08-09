begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|InputStream
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
name|HBaseTestingUtility
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
name|zookeeper
operator|.
name|server
operator|.
name|quorum
operator|.
name|QuorumPeerConfig
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
name|server
operator|.
name|quorum
operator|.
name|QuorumPeer
operator|.
name|QuorumServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
import|import static
name|junit
operator|.
name|framework
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
name|*
import|;
end_import

begin_comment
comment|/**  * Test for HQuorumPeer.  */
end_comment

begin_class
specifier|public
class|class
name|TestHQuorumPeer
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|int
name|PORT_NO
init|=
literal|21818
decl_stmt|;
specifier|private
name|Path
name|dataDir
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Set it to a non-standard port.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.zookeeper.property.clientPort"
argument_list|,
name|PORT_NO
argument_list|)
expr_stmt|;
name|this
operator|.
name|dataDir
operator|=
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|this
operator|.
name|dataDir
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|this
operator|.
name|dataDir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed cleanup of "
operator|+
name|this
operator|.
name|dataDir
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|this
operator|.
name|dataDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed create of "
operator|+
name|this
operator|.
name|dataDir
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMakeZKProps
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.zookeeper.property.dataDir"
argument_list|,
name|this
operator|.
name|dataDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Properties
name|properties
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
name|dataDir
operator|.
name|toString
argument_list|()
argument_list|,
operator|(
name|String
operator|)
name|properties
operator|.
name|get
argument_list|(
literal|"dataDir"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|PORT_NO
argument_list|)
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|properties
operator|.
name|getProperty
argument_list|(
literal|"clientPort"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"localhost:2888:3888"
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"server.0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"server.1"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|oldValue
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
literal|"a.foo.bar,b.foo.bar,c.foo.bar"
argument_list|)
expr_stmt|;
name|properties
operator|=
name|ZKConfig
operator|.
name|makeZKProps
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|dataDir
operator|.
name|toString
argument_list|()
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"dataDir"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|PORT_NO
argument_list|)
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|properties
operator|.
name|getProperty
argument_list|(
literal|"clientPort"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a.foo.bar:2888:3888"
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"server.0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"b.foo.bar:2888:3888"
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"server.1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c.foo.bar:2888:3888"
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"server.2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"server.3"
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
name|oldValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConfigInjection
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|s
init|=
literal|"dataDir="
operator|+
name|this
operator|.
name|dataDir
operator|.
name|toString
argument_list|()
operator|+
literal|"\n"
operator|+
literal|"clientPort=2181\n"
operator|+
literal|"initLimit=2\n"
operator|+
literal|"syncLimit=2\n"
operator|+
literal|"server.0=${hbase.master.hostname}:2888:3888\n"
operator|+
literal|"server.1=server1:2888:3888\n"
operator|+
literal|"server.2=server2:2888:3888\n"
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hbase.master.hostname"
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
name|InputStream
name|is
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|s
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Properties
name|properties
init|=
name|ZKConfig
operator|.
name|parseZooCfg
argument_list|(
name|conf
argument_list|,
name|is
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|this
operator|.
name|dataDir
operator|.
name|toString
argument_list|()
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"dataDir"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|2181
argument_list|)
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|properties
operator|.
name|getProperty
argument_list|(
literal|"clientPort"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"localhost:2888:3888"
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"server.0"
argument_list|)
argument_list|)
expr_stmt|;
name|HQuorumPeer
operator|.
name|writeMyID
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|QuorumPeerConfig
name|config
init|=
operator|new
name|QuorumPeerConfig
argument_list|()
decl_stmt|;
name|config
operator|.
name|parseProperties
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|this
operator|.
name|dataDir
operator|.
name|toString
argument_list|()
argument_list|,
name|config
operator|.
name|getDataDir
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2181
argument_list|,
name|config
operator|.
name|getClientPortAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Long
argument_list|,
name|QuorumServer
argument_list|>
name|servers
init|=
name|config
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|servers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|servers
operator|.
name|containsKey
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|QuorumServer
name|server
init|=
name|servers
operator|.
name|get
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"localhost"
argument_list|,
name|server
operator|.
name|addr
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Override with system property.
name|System
operator|.
name|setProperty
argument_list|(
literal|"hbase.master.hostname"
argument_list|,
literal|"foo.bar"
argument_list|)
expr_stmt|;
name|is
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|s
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|properties
operator|=
name|ZKConfig
operator|.
name|parseZooCfg
argument_list|(
name|conf
argument_list|,
name|is
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo.bar:2888:3888"
argument_list|,
name|properties
operator|.
name|get
argument_list|(
literal|"server.0"
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|parseProperties
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|servers
operator|=
name|config
operator|.
name|getServers
argument_list|()
expr_stmt|;
name|server
operator|=
name|servers
operator|.
name|get
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo.bar"
argument_list|,
name|server
operator|.
name|addr
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test Case for HBASE-2305    */
annotation|@
name|Test
specifier|public
name|void
name|testShouldAssignDefaultZookeeperClientPort
parameter_list|()
block|{
name|Configuration
name|config
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|config
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Properties
name|p
init|=
name|ZKConfig
operator|.
name|makeZKProps
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2181
argument_list|,
name|p
operator|.
name|get
argument_list|(
literal|"hbase.zookeeper.property.clientPort"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

