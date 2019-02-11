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
name|assertNotNull
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
name|assertNull
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|List
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Threads
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
operator|.
name|ZKUtilOp
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
name|ZooDefs
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
name|ACL
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
name|ImmutableList
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
name|io
operator|.
name|Closeables
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
name|TestZKUtil
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
name|TestZKUtil
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestZKUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseZKTestingUtility
name|UTIL
init|=
operator|new
name|HBaseZKTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|ZKWatcher
name|ZKW
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniZKCluster
argument_list|()
operator|.
name|getClientPort
argument_list|()
expr_stmt|;
name|ZKW
operator|=
operator|new
name|ZKWatcher
argument_list|(
operator|new
name|Configuration
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|TestZKUtil
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|WarnOnlyAbortable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|Closeables
operator|.
name|close
argument_list|(
name|ZKW
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create a znode with data    */
annotation|@
name|Test
specifier|public
name|void
name|testCreateWithParents
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|byte
index|[]
name|expectedData
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2/l3/l4/testCreateWithParents"
argument_list|,
name|expectedData
argument_list|)
expr_stmt|;
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2/l3/l4/testCreateWithParents"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|expectedData
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|ZKW
argument_list|,
literal|"/l1"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|ZKW
argument_list|,
literal|"/testCreateWithParents"
argument_list|,
name|expectedData
argument_list|)
expr_stmt|;
name|data
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|ZKW
argument_list|,
literal|"/testCreateWithParents"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|expectedData
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|ZKW
argument_list|,
literal|"/testCreateWithParents"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a bunch of znodes in a hierarchy, try deleting one that has childs (it will fail), then    * delete it recursively, then delete the last znode    */
annotation|@
name|Test
specifier|public
name|void
name|testZNodeDeletes
parameter_list|()
throws|throws
name|Exception
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2/l3/l4"
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"We should not be able to delete if znode has childs"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ex
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2/l3/l4"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2"
argument_list|)
expr_stmt|;
comment|// make sure it really is deleted
name|assertNull
argument_list|(
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2/l3/l4"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// do the same delete again and make sure it doesn't crash
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|ZKW
argument_list|,
literal|"/l1"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|ZKW
argument_list|,
literal|"/l1/l2"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|getZNodeDataVersion
parameter_list|(
name|String
name|znode
parameter_list|)
throws|throws
name|KeeperException
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|ZKW
argument_list|,
name|znode
argument_list|,
name|stat
argument_list|)
expr_stmt|;
return|return
name|stat
operator|.
name|getVersion
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetDataWithVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|ZKW
argument_list|,
literal|"/s1/s2/s3"
argument_list|)
expr_stmt|;
name|int
name|v0
init|=
name|getZNodeDataVersion
argument_list|(
literal|"/s1/s2/s3"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|v0
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|ZKW
argument_list|,
literal|"/s1/s2/s3"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|12L
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|v1
init|=
name|getZNodeDataVersion
argument_list|(
literal|"/s1/s2/s3"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|v1
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|multiOrSequential
argument_list|(
name|ZKW
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|ZKUtilOp
operator|.
name|setData
argument_list|(
literal|"/s1/s2/s3"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|13L
argument_list|)
argument_list|,
name|v1
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|int
name|v2
init|=
name|getZNodeDataVersion
argument_list|(
literal|"/s1/s2/s3"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|v2
argument_list|)
expr_stmt|;
block|}
comment|/**    * A test for HBASE-3238    * @throws IOException A connection attempt to zk failed    * @throws InterruptedException One of the non ZKUtil actions was interrupted    * @throws KeeperException Any of the zookeeper connections had a KeeperException    */
annotation|@
name|Test
specifier|public
name|void
name|testCreateSilentIsReallySilent
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|KeeperException
throws|,
name|IOException
block|{
name|Configuration
name|c
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|String
name|aclZnode
init|=
literal|"/aclRoot"
decl_stmt|;
name|String
name|quorumServers
init|=
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|int
name|sessionTimeout
init|=
literal|5
operator|*
literal|1000
decl_stmt|;
comment|// 5 seconds
name|ZooKeeper
name|zk
init|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
name|EmptyWatcher
operator|.
name|instance
argument_list|)
decl_stmt|;
name|zk
operator|.
name|addAuthInfo
argument_list|(
literal|"digest"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hbase:rox"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Save the previous ACL
name|Stat
name|s
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|ACL
argument_list|>
name|oldACL
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|s
operator|=
operator|new
name|Stat
argument_list|()
expr_stmt|;
name|oldACL
operator|=
name|zk
operator|.
name|getACL
argument_list|(
literal|"/"
argument_list|,
name|s
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|LOG
operator|.
name|warn
argument_list|(
literal|"Possibly transient ZooKeeper exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
block|}
comment|// I set this acl after the attempted creation of the cluster home node.
comment|// Add retries in case of retryable zk exceptions.
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|zk
operator|.
name|setACL
argument_list|(
literal|"/"
argument_list|,
name|ZooDefs
operator|.
name|Ids
operator|.
name|CREATOR_ALL_ACL
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|LOG
operator|.
name|warn
argument_list|(
literal|"Possibly transient ZooKeeper exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
block|}
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|zk
operator|.
name|create
argument_list|(
name|aclZnode
argument_list|,
literal|null
argument_list|,
name|ZooDefs
operator|.
name|Ids
operator|.
name|CREATOR_ALL_ACL
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|LOG
operator|.
name|warn
argument_list|(
literal|"Possibly transient ZooKeeper exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
block|}
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|ZKW
argument_list|,
name|aclZnode
argument_list|)
expr_stmt|;
comment|// Restore the ACL
name|ZooKeeper
name|zk3
init|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
name|EmptyWatcher
operator|.
name|instance
argument_list|)
decl_stmt|;
name|zk3
operator|.
name|addAuthInfo
argument_list|(
literal|"digest"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hbase:rox"
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|zk3
operator|.
name|setACL
argument_list|(
literal|"/"
argument_list|,
name|oldACL
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|zk3
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Test should not fail with NPE when getChildDataAndWatchForNewChildren invoked with wrongNode    */
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|testGetChildDataAndWatchForNewChildrenShouldNotThrowNPE
parameter_list|()
throws|throws
name|Exception
block|{
name|ZKUtil
operator|.
name|getChildDataAndWatchForNewChildren
argument_list|(
name|ZKW
argument_list|,
literal|"/wrongNode"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|WarnOnlyAbortable
implements|implements
name|Abortable
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
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"ZKWatcher received abort, ignoring.  Reason: "
operator|+
name|why
argument_list|)
expr_stmt|;
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
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
block|}
end_class

end_unit

