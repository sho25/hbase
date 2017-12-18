begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|token
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
name|assertFalse
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
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
name|testclassification
operator|.
name|LargeTests
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
name|SecurityTests
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
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKWatcher
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

begin_comment
comment|/**  * Test the synchronization of token authentication master keys through  * ZKSecretWatcher  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SecurityTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestZKSecretWatcher
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
name|TestZKSecretWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
specifier|static
name|AuthenticationTokenSecretManager
name|KEY_MASTER
decl_stmt|;
specifier|private
specifier|static
name|AuthenticationTokenSecretManagerForTest
name|KEY_SLAVE
decl_stmt|;
specifier|private
specifier|static
name|AuthenticationTokenSecretManager
name|KEY_SLAVE2
decl_stmt|;
specifier|private
specifier|static
name|AuthenticationTokenSecretManager
name|KEY_SLAVE3
decl_stmt|;
specifier|private
specifier|static
class|class
name|MockAbortable
implements|implements
name|Abortable
block|{
specifier|private
name|boolean
name|abort
decl_stmt|;
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|reason
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting: "
operator|+
name|reason
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|abort
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|abort
return|;
block|}
block|}
comment|// We subclass AuthenticationTokenSecretManager so that testKeyUpdate can receive
comment|// notification on the removal of keyId
specifier|private
specifier|static
class|class
name|AuthenticationTokenSecretManagerForTest
extends|extends
name|AuthenticationTokenSecretManager
block|{
specifier|private
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|public
name|AuthenticationTokenSecretManagerForTest
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZKWatcher
name|zk
parameter_list|,
name|String
name|serverName
parameter_list|,
name|long
name|keyUpdateInterval
parameter_list|,
name|long
name|tokenMaxLifetime
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|zk
argument_list|,
name|serverName
argument_list|,
name|keyUpdateInterval
argument_list|,
name|tokenMaxLifetime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|synchronized
name|boolean
name|removeKey
parameter_list|(
name|Integer
name|keyId
parameter_list|)
block|{
name|boolean
name|b
init|=
name|super
operator|.
name|removeKey
argument_list|(
name|keyId
argument_list|)
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
return|return
name|b
return|;
block|}
name|CountDownLatch
name|getLatch
parameter_list|()
block|{
return|return
name|latch
return|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ZKWatcher
name|zk
init|=
name|newZK
argument_list|(
name|conf
argument_list|,
literal|"server1"
argument_list|,
operator|new
name|MockAbortable
argument_list|()
argument_list|)
decl_stmt|;
name|AuthenticationTokenSecretManagerForTest
index|[]
name|tmp
init|=
operator|new
name|AuthenticationTokenSecretManagerForTest
index|[
literal|2
index|]
decl_stmt|;
name|tmp
index|[
literal|0
index|]
operator|=
operator|new
name|AuthenticationTokenSecretManagerForTest
argument_list|(
name|conf
argument_list|,
name|zk
argument_list|,
literal|"server1"
argument_list|,
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|,
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|tmp
index|[
literal|0
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
name|zk
operator|=
name|newZK
argument_list|(
name|conf
argument_list|,
literal|"server2"
argument_list|,
operator|new
name|MockAbortable
argument_list|()
argument_list|)
expr_stmt|;
name|tmp
index|[
literal|1
index|]
operator|=
operator|new
name|AuthenticationTokenSecretManagerForTest
argument_list|(
name|conf
argument_list|,
name|zk
argument_list|,
literal|"server2"
argument_list|,
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|,
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|tmp
index|[
literal|1
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
while|while
condition|(
name|KEY_MASTER
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|2
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|tmp
index|[
name|i
index|]
operator|.
name|isMaster
argument_list|()
condition|)
block|{
name|KEY_MASTER
operator|=
name|tmp
index|[
name|i
index|]
expr_stmt|;
name|KEY_SLAVE
operator|=
name|tmp
index|[
operator|(
name|i
operator|+
literal|1
operator|)
operator|%
literal|2
index|]
expr_stmt|;
break|break;
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Master is "
operator|+
name|KEY_MASTER
operator|.
name|getName
argument_list|()
operator|+
literal|", slave is "
operator|+
name|KEY_SLAVE
operator|.
name|getName
argument_list|()
argument_list|)
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
name|testKeyUpdate
parameter_list|()
throws|throws
name|Exception
block|{
comment|// sanity check
name|assertTrue
argument_list|(
name|KEY_MASTER
operator|.
name|isMaster
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|KEY_SLAVE
operator|.
name|isMaster
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|maxKeyId
init|=
literal|0
decl_stmt|;
name|KEY_MASTER
operator|.
name|rollCurrentKey
argument_list|()
expr_stmt|;
name|AuthenticationKey
name|key1
init|=
name|KEY_MASTER
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|key1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Master current key: "
operator|+
name|key1
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait for slave to update
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|AuthenticationKey
name|slaveCurrent
init|=
name|KEY_SLAVE
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|slaveCurrent
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|key1
argument_list|,
name|slaveCurrent
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Slave current key: "
operator|+
name|slaveCurrent
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
comment|// generate two more keys then expire the original
name|KEY_MASTER
operator|.
name|rollCurrentKey
argument_list|()
expr_stmt|;
name|AuthenticationKey
name|key2
init|=
name|KEY_MASTER
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Master new current key: "
operator|+
name|key2
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|KEY_MASTER
operator|.
name|rollCurrentKey
argument_list|()
expr_stmt|;
name|AuthenticationKey
name|key3
init|=
name|KEY_MASTER
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Master new current key: "
operator|+
name|key3
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
comment|// force expire the original key
name|key1
operator|.
name|setExpiration
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
literal|1000
argument_list|)
expr_stmt|;
name|KEY_MASTER
operator|.
name|removeExpiredKeys
argument_list|()
expr_stmt|;
comment|// verify removed from master
name|assertNull
argument_list|(
name|KEY_MASTER
operator|.
name|getKey
argument_list|(
name|key1
operator|.
name|getKeyId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait for slave to catch up
name|KEY_SLAVE
operator|.
name|getLatch
argument_list|()
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// make sure the slave has both new keys
name|AuthenticationKey
name|slave2
init|=
name|KEY_SLAVE
operator|.
name|getKey
argument_list|(
name|key2
operator|.
name|getKeyId
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|slave2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|key2
argument_list|,
name|slave2
argument_list|)
expr_stmt|;
name|AuthenticationKey
name|slave3
init|=
name|KEY_SLAVE
operator|.
name|getKey
argument_list|(
name|key3
operator|.
name|getKeyId
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|slave3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|key3
argument_list|,
name|slave3
argument_list|)
expr_stmt|;
name|slaveCurrent
operator|=
name|KEY_SLAVE
operator|.
name|getCurrentKey
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|key3
argument_list|,
name|slaveCurrent
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Slave current key: "
operator|+
name|slaveCurrent
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify that the expired key has been removed
name|assertNull
argument_list|(
name|KEY_SLAVE
operator|.
name|getKey
argument_list|(
name|key1
operator|.
name|getKeyId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// bring up a new slave
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ZKWatcher
name|zk
init|=
name|newZK
argument_list|(
name|conf
argument_list|,
literal|"server3"
argument_list|,
operator|new
name|MockAbortable
argument_list|()
argument_list|)
decl_stmt|;
name|KEY_SLAVE2
operator|=
operator|new
name|AuthenticationTokenSecretManager
argument_list|(
name|conf
argument_list|,
name|zk
argument_list|,
literal|"server3"
argument_list|,
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|,
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|KEY_SLAVE2
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
comment|// verify the new slave has current keys (and not expired)
name|slave2
operator|=
name|KEY_SLAVE2
operator|.
name|getKey
argument_list|(
name|key2
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|slave2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|key2
argument_list|,
name|slave2
argument_list|)
expr_stmt|;
name|slave3
operator|=
name|KEY_SLAVE2
operator|.
name|getKey
argument_list|(
name|key3
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|slave3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|key3
argument_list|,
name|slave3
argument_list|)
expr_stmt|;
name|slaveCurrent
operator|=
name|KEY_SLAVE2
operator|.
name|getCurrentKey
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|key3
argument_list|,
name|slaveCurrent
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|KEY_SLAVE2
operator|.
name|getKey
argument_list|(
name|key1
operator|.
name|getKeyId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// test leader failover
name|KEY_MASTER
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// wait for master to stop
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|KEY_MASTER
operator|.
name|isMaster
argument_list|()
argument_list|)
expr_stmt|;
comment|// check for a new master
name|AuthenticationTokenSecretManager
index|[]
name|mgrs
init|=
operator|new
name|AuthenticationTokenSecretManager
index|[]
block|{
name|KEY_SLAVE
block|,
name|KEY_SLAVE2
block|}
decl_stmt|;
name|AuthenticationTokenSecretManager
name|newMaster
init|=
literal|null
decl_stmt|;
name|int
name|tries
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|newMaster
operator|==
literal|null
operator|&&
name|tries
operator|++
operator|<
literal|5
condition|)
block|{
for|for
control|(
name|AuthenticationTokenSecretManager
name|mgr
range|:
name|mgrs
control|)
block|{
if|if
condition|(
name|mgr
operator|.
name|isMaster
argument_list|()
condition|)
block|{
name|newMaster
operator|=
name|mgr
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|newMaster
operator|==
literal|null
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
block|}
name|assertNotNull
argument_list|(
name|newMaster
argument_list|)
expr_stmt|;
name|AuthenticationKey
name|current
init|=
name|newMaster
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
comment|// new master will immediately roll the current key, so it's current may be greater
name|assertTrue
argument_list|(
name|current
operator|.
name|getKeyId
argument_list|()
operator|>=
name|slaveCurrent
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"New master, current key: "
operator|+
name|current
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
comment|// roll the current key again on new master and verify the key ID increments
name|newMaster
operator|.
name|rollCurrentKey
argument_list|()
expr_stmt|;
name|AuthenticationKey
name|newCurrent
init|=
name|newMaster
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"New master, rolled new current key: "
operator|+
name|newCurrent
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newCurrent
operator|.
name|getKeyId
argument_list|()
operator|>
name|current
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
comment|// add another slave
name|ZKWatcher
name|zk3
init|=
name|newZK
argument_list|(
name|conf
argument_list|,
literal|"server4"
argument_list|,
operator|new
name|MockAbortable
argument_list|()
argument_list|)
decl_stmt|;
name|KEY_SLAVE3
operator|=
operator|new
name|AuthenticationTokenSecretManager
argument_list|(
name|conf
argument_list|,
name|zk3
argument_list|,
literal|"server4"
argument_list|,
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|,
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|KEY_SLAVE3
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// check master failover again
name|newMaster
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// wait for master to stop
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|newMaster
operator|.
name|isMaster
argument_list|()
argument_list|)
expr_stmt|;
comment|// check for a new master
name|mgrs
operator|=
operator|new
name|AuthenticationTokenSecretManager
index|[]
block|{
name|KEY_SLAVE
block|,
name|KEY_SLAVE2
block|,
name|KEY_SLAVE3
block|}
expr_stmt|;
name|newMaster
operator|=
literal|null
expr_stmt|;
name|tries
operator|=
literal|0
expr_stmt|;
while|while
condition|(
name|newMaster
operator|==
literal|null
operator|&&
name|tries
operator|++
operator|<
literal|5
condition|)
block|{
for|for
control|(
name|AuthenticationTokenSecretManager
name|mgr
range|:
name|mgrs
control|)
block|{
if|if
condition|(
name|mgr
operator|.
name|isMaster
argument_list|()
condition|)
block|{
name|newMaster
operator|=
name|mgr
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|newMaster
operator|==
literal|null
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
block|}
name|assertNotNull
argument_list|(
name|newMaster
argument_list|)
expr_stmt|;
name|AuthenticationKey
name|current2
init|=
name|newMaster
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
comment|// new master will immediately roll the current key, so it's current may be greater
name|assertTrue
argument_list|(
name|current2
operator|.
name|getKeyId
argument_list|()
operator|>=
name|newCurrent
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"New master 2, current key: "
operator|+
name|current2
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
comment|// roll the current key again on new master and verify the key ID increments
name|newMaster
operator|.
name|rollCurrentKey
argument_list|()
expr_stmt|;
name|AuthenticationKey
name|newCurrent2
init|=
name|newMaster
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"New master 2, rolled new current key: "
operator|+
name|newCurrent2
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newCurrent2
operator|.
name|getKeyId
argument_list|()
operator|>
name|current2
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|ZKWatcher
name|newZK
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|Abortable
name|abort
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|copy
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ZKWatcher
name|zk
init|=
operator|new
name|ZKWatcher
argument_list|(
name|copy
argument_list|,
name|name
argument_list|,
name|abort
argument_list|)
decl_stmt|;
return|return
name|zk
return|;
block|}
block|}
end_class

end_unit

