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
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|Abortable
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
name|HBaseZKTestingUtility
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
name|MediumTests
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
name|ZKTests
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
name|zookeeper
operator|.
name|CreateMode
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
name|ZooDefs
operator|.
name|Ids
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|ZKTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRecoverableZooKeeper
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
name|TestRecoverableZooKeeper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseZKTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseZKTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|Abortable
name|abortable
init|=
operator|new
name|Abortable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetDataVersionMismatchInLoop
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|znode
init|=
literal|"/hbase/splitWAL/9af7cfc9b15910a0b3d714bf40a3248f"
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ZKWatcher
name|zkw
init|=
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"testSetDataVersionMismatchInLoop"
argument_list|,
name|abortable
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|String
name|ensemble
init|=
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|RecoverableZooKeeper
name|rzk
init|=
name|ZKUtil
operator|.
name|connect
argument_list|(
name|conf
argument_list|,
name|ensemble
argument_list|,
name|zkw
argument_list|)
decl_stmt|;
name|rzk
operator|.
name|create
argument_list|(
name|znode
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
expr_stmt|;
name|rzk
operator|.
name|setData
argument_list|(
name|znode
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"OPENING"
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Field
name|zkField
init|=
name|RecoverableZooKeeper
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"zk"
argument_list|)
decl_stmt|;
name|zkField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|int
name|timeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZK_SESSION_TIMEOUT
argument_list|)
decl_stmt|;
name|ZookeeperStub
name|zkStub
init|=
operator|new
name|ZookeeperStub
argument_list|(
name|ensemble
argument_list|,
name|timeout
argument_list|,
name|zkw
argument_list|)
decl_stmt|;
name|zkStub
operator|.
name|setThrowExceptionInNumOperations
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|zkField
operator|.
name|set
argument_list|(
name|rzk
argument_list|,
name|zkStub
argument_list|)
expr_stmt|;
name|byte
index|[]
name|opened
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"OPENED"
argument_list|)
decl_stmt|;
name|rzk
operator|.
name|setData
argument_list|(
name|znode
argument_list|,
name|opened
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|byte
index|[]
name|data
init|=
name|rzk
operator|.
name|getData
argument_list|(
name|znode
argument_list|,
literal|false
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|opened
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|ZookeeperStub
extends|extends
name|ZooKeeper
block|{
specifier|private
name|int
name|throwExceptionInNumOperations
decl_stmt|;
name|ZookeeperStub
parameter_list|(
name|String
name|connectString
parameter_list|,
name|int
name|sessionTimeout
parameter_list|,
name|Watcher
name|watcher
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|connectString
argument_list|,
name|sessionTimeout
argument_list|,
name|watcher
argument_list|)
expr_stmt|;
block|}
name|void
name|setThrowExceptionInNumOperations
parameter_list|(
name|int
name|throwExceptionInNumOperations
parameter_list|)
block|{
name|this
operator|.
name|throwExceptionInNumOperations
operator|=
name|throwExceptionInNumOperations
expr_stmt|;
block|}
specifier|private
name|void
name|checkThrowKeeperException
parameter_list|()
throws|throws
name|KeeperException
block|{
if|if
condition|(
name|throwExceptionInNumOperations
operator|==
literal|1
condition|)
block|{
name|throwExceptionInNumOperations
operator|=
literal|0
expr_stmt|;
throw|throw
operator|new
name|KeeperException
operator|.
name|ConnectionLossException
argument_list|()
throw|;
block|}
if|if
condition|(
name|throwExceptionInNumOperations
operator|>
literal|0
condition|)
block|{
name|throwExceptionInNumOperations
operator|--
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Stat
name|setData
parameter_list|(
name|String
name|path
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|int
name|version
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|Stat
name|stat
init|=
name|super
operator|.
name|setData
argument_list|(
name|path
argument_list|,
name|data
argument_list|,
name|version
argument_list|)
decl_stmt|;
name|checkThrowKeeperException
argument_list|()
expr_stmt|;
return|return
name|stat
return|;
block|}
block|}
block|}
end_class

end_unit

