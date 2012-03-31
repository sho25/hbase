begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertFalse
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|Random
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
name|SynchronousQueue
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
name|ThreadPoolExecutor
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
name|TimeUnit
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
name|*
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
name|HTable
operator|.
name|DaemonThreadFactory
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
name|Assert
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

begin_comment
comment|/**  * This class is for testing HCM features  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHCM
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
name|TestHCM
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLE_NAME1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAM_NAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
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
name|startMiniCluster
argument_list|(
literal|1
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
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws InterruptedException     * @throws IllegalAccessException     * @throws NoSuchFieldException     * @throws ZooKeeperConnectionException     * @throws IllegalArgumentException     * @throws SecurityException     * @see https://issues.apache.org/jira/browse/HBASE-2925    */
comment|// Disabling.  Of course this test will OOME using new Configuration each time
comment|// St.Ack 20110428
comment|// @Test
specifier|public
name|void
name|testManyNewConnectionsDoesnotOOME
parameter_list|()
throws|throws
name|SecurityException
throws|,
name|IllegalArgumentException
throws|,
name|ZooKeeperConnectionException
throws|,
name|NoSuchFieldException
throws|,
name|IllegalAccessException
throws|,
name|InterruptedException
block|{
name|createNewConfigurations
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|Random
name|_randy
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|void
name|createNewConfigurations
parameter_list|()
throws|throws
name|SecurityException
throws|,
name|IllegalArgumentException
throws|,
name|NoSuchFieldException
throws|,
name|IllegalAccessException
throws|,
name|InterruptedException
throws|,
name|ZooKeeperConnectionException
block|{
name|HConnection
name|last
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
operator|(
name|HConnectionManager
operator|.
name|MAX_CACHED_HBASE_INSTANCES
operator|*
literal|2
operator|)
condition|;
name|i
operator|++
control|)
block|{
comment|// set random key to differentiate the connection from previous ones
name|Configuration
name|configuration
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"somekey"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|_randy
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Hash Code: "
operator|+
name|configuration
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
if|if
condition|(
name|last
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|last
operator|==
name|connection
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"!! Got same connection for once !!"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// change the configuration once, and the cached connection is lost forever:
comment|//      the hashtable holding the cache won't be able to find its own keys
comment|//      to remove them, so the LRU strategy does not work.
name|configuration
operator|.
name|set
argument_list|(
literal|"someotherkey"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|_randy
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|last
operator|=
name|connection
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cache Size: "
operator|+
name|getHConnectionManagerCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|getHConnectionManagerCacheSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|int
name|getHConnectionManagerCacheSize
parameter_list|()
block|{
return|return
name|HConnectionTestingUtility
operator|.
name|getConnectionCount
argument_list|()
return|;
block|}
comment|/**    * Test that when we delete a location using the first row of a region    * that we really delete it.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionCaching
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAM_NAM
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegions
argument_list|(
name|table
argument_list|,
name|FAM_NAM
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAM_NAM
argument_list|,
name|ROW
argument_list|,
name|ROW
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|HConnectionManager
operator|.
name|HConnectionImplementation
name|conn
init|=
operator|(
name|HConnectionManager
operator|.
name|HConnectionImplementation
operator|)
name|table
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|conn
operator|.
name|getCachedLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|ROW
argument_list|)
argument_list|)
expr_stmt|;
name|conn
operator|.
name|deleteCachedLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|ROW
argument_list|)
expr_stmt|;
name|HRegionLocation
name|rl
init|=
name|conn
operator|.
name|getCachedLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|ROW
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"What is this location?? "
operator|+
name|rl
argument_list|,
name|rl
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test that Connection or Pool are not closed when managed externally    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testConnectionManagement
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME1
argument_list|,
name|FAM_NAM
argument_list|)
expr_stmt|;
name|HConnection
name|conn
init|=
name|HConnectionManager
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
operator|new
name|DaemonThreadFactory
argument_list|()
argument_list|)
decl_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TABLE_NAME1
argument_list|,
name|conn
argument_list|,
name|pool
argument_list|)
decl_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|conn
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|pool
operator|.
name|isShutdown
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLE_NAME1
argument_list|,
name|pool
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|pool
operator|.
name|isShutdown
argument_list|()
argument_list|)
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
comment|/**    * Make sure that {@link HConfiguration} instances that are essentially the    * same map to the same {@link HConnection} instance.    */
annotation|@
name|Test
specifier|public
name|void
name|testConnectionSameness
parameter_list|()
throws|throws
name|Exception
block|{
name|HConnection
name|previousConnection
init|=
literal|null
decl_stmt|;
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
comment|// set random key to differentiate the connection from previous ones
name|Configuration
name|configuration
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"some_key"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|_randy
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"The hash code of the current configuration is: "
operator|+
name|configuration
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|HConnection
name|currentConnection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
if|if
condition|(
name|previousConnection
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Did not get the same connection even though its key didn't change"
argument_list|,
name|previousConnection
operator|==
name|currentConnection
argument_list|)
expr_stmt|;
block|}
name|previousConnection
operator|=
name|currentConnection
expr_stmt|;
comment|// change the configuration, so that it is no longer reachable from the
comment|// client's perspective. However, since its part of the LRU doubly linked
comment|// list, it will eventually get thrown out, at which time it should also
comment|// close the corresponding {@link HConnection}.
name|configuration
operator|.
name|set
argument_list|(
literal|"other_key"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|_randy
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Makes sure that there is no leaking of    * {@link HConnectionManager.TableServers} in the {@link HConnectionManager}    * class.    */
annotation|@
name|Test
specifier|public
name|void
name|testConnectionUniqueness
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|zkmaxconnections
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_MAX_CLIENT_CNXNS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEPER_MAX_CLIENT_CNXNS
argument_list|)
decl_stmt|;
comment|// Test up to a max that is< the maximum number of zk connections.  If we
comment|// go above zk connections, we just fall into cycle where we are failing
comment|// to set up a session and test runs for a long time.
name|int
name|maxConnections
init|=
name|Math
operator|.
name|min
argument_list|(
name|zkmaxconnections
operator|-
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HConnection
argument_list|>
name|connections
init|=
operator|new
name|ArrayList
argument_list|<
name|HConnection
argument_list|>
argument_list|(
name|maxConnections
argument_list|)
decl_stmt|;
name|HConnection
name|previousConnection
init|=
literal|null
decl_stmt|;
try|try
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
name|maxConnections
condition|;
name|i
operator|++
control|)
block|{
comment|// set random key to differentiate the connection from previous ones
name|Configuration
name|configuration
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
name|configuration
operator|.
name|set
argument_list|(
literal|"some_key"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|_randy
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_INSTANCE_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|_randy
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"The hash code of the current configuration is: "
operator|+
name|configuration
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|HConnection
name|currentConnection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
if|if
condition|(
name|previousConnection
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Got the same connection even though its key changed!"
argument_list|,
name|previousConnection
operator|!=
name|currentConnection
argument_list|)
expr_stmt|;
block|}
comment|// change the configuration, so that it is no longer reachable from the
comment|// client's perspective. However, since its part of the LRU doubly linked
comment|// list, it will eventually get thrown out, at which time it should also
comment|// close the corresponding {@link HConnection}.
name|configuration
operator|.
name|set
argument_list|(
literal|"other_key"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|_randy
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|previousConnection
operator|=
name|currentConnection
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"The current HConnectionManager#HBASE_INSTANCES cache size is: "
operator|+
name|getHConnectionManagerCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|connections
operator|.
name|add
argument_list|(
name|currentConnection
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
for|for
control|(
name|HConnection
name|c
range|:
name|connections
control|)
block|{
comment|// Clean up connections made so we don't interfere w/ subsequent tests.
name|HConnectionManager
operator|.
name|deleteConnection
argument_list|(
name|c
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClosing
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|configuration
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
name|configuration
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_INSTANCE_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|_randy
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HConnection
name|c1
init|=
name|HConnectionManager
operator|.
name|createConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
comment|// We create two connections with the same key.
name|HConnection
name|c2
init|=
name|HConnectionManager
operator|.
name|createConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|HConnection
name|c3
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|HConnection
name|c4
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|c3
operator|==
name|c4
argument_list|)
expr_stmt|;
name|c1
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|c1
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|c2
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|c3
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|c3
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// still a reference left
name|assertFalse
argument_list|(
name|c3
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|c3
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|c3
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
comment|// c3 was removed from the cache
name|HConnection
name|c5
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|c5
operator|!=
name|c3
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|c2
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|c2
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|c2
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|c5
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|c5
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Trivial test to verify that nobody messes with    * {@link HConnectionManager#createConnection(Configuration)}    */
annotation|@
name|Test
specifier|public
name|void
name|testCreateConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|configuration
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|HConnection
name|c1
init|=
name|HConnectionManager
operator|.
name|createConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|HConnection
name|c2
init|=
name|HConnectionManager
operator|.
name|createConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
comment|// created from the same configuration, yet they are different
name|assertTrue
argument_list|(
name|c1
operator|!=
name|c2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|c1
operator|.
name|getConfiguration
argument_list|()
operator|==
name|c2
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// make sure these were not cached
name|HConnection
name|c3
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|c1
operator|!=
name|c3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|c2
operator|!=
name|c3
argument_list|)
expr_stmt|;
block|}
comment|/**    * This test checks that one can connect to the cluster with only the    *  ZooKeeper quorum set. Other stuff like master address will be read    *  from ZK by the client.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testConnection
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We create an empty config and add the ZK address.
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|c
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|)
argument_list|)
expr_stmt|;
comment|// This should be enough to connect
name|HConnection
name|conn
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|conn
operator|.
name|isMasterRunning
argument_list|()
argument_list|)
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

