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
name|HBaseTestCase
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

begin_comment
comment|/**  * Test for HQuorumPeer.  */
end_comment

begin_class
specifier|public
class|class
name|HQuorumPeerTest
extends|extends
name|HBaseTestCase
block|{
specifier|private
name|Path
name|dataDir
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|String
name|userName
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
decl_stmt|;
name|dataDir
operator|=
operator|new
name|Path
argument_list|(
literal|"/tmp/hbase-"
operator|+
name|userName
argument_list|,
literal|"zookeeper"
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|dataDir
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|isDirectory
argument_list|(
name|dataDir
argument_list|)
condition|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|dataDir
argument_list|)
condition|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|dataDir
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|dataDir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
comment|/** @throws Exception */
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
literal|"tickTime=2000\n"
operator|+
literal|"initLimit=10\n"
operator|+
literal|"syncLimit=5\n"
operator|+
literal|"dataDir=${hbase.tmp.dir}/zookeeper\n"
operator|+
literal|"clientPort=2181\n"
operator|+
literal|"server.0=${hbase.master.hostname}:2888:3888\n"
decl_stmt|;
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
name|Properties
name|properties
init|=
name|HQuorumPeer
operator|.
name|parseConfig
argument_list|(
name|is
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|2000
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
literal|"tickTime"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|10
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
literal|"initLimit"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|5
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
literal|"syncLimit"
argument_list|)
argument_list|)
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
name|int
name|tickTime
init|=
name|config
operator|.
name|getTickTime
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2000
argument_list|,
name|tickTime
argument_list|)
expr_stmt|;
name|int
name|initLimit
init|=
name|config
operator|.
name|getInitLimit
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|initLimit
argument_list|)
expr_stmt|;
name|int
name|syncLimit
init|=
name|config
operator|.
name|getSyncLimit
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|syncLimit
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
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
name|getClientPort
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
literal|1
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
name|HQuorumPeer
operator|.
name|parseConfig
argument_list|(
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
comment|// Special case for property 'hbase.master.hostname' being 'local'
name|System
operator|.
name|setProperty
argument_list|(
literal|"hbase.master.hostname"
argument_list|,
literal|"local"
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
name|HQuorumPeer
operator|.
name|parseConfig
argument_list|(
name|is
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
block|}
block|}
end_class

end_unit

