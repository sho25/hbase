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
name|HashSet
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
name|Map
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
name|Set
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
name|ZooKeeperConnectionException
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
name|mortbay
operator|.
name|log
operator|.
name|Log
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
name|assertNotNull
import|;
end_import

begin_comment
comment|/**  * This class is for testing HCM features  */
end_comment

begin_class
specifier|public
class|class
name|TestHCM
block|{
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
annotation|@
name|Test
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
name|Log
operator|.
name|info
argument_list|(
literal|"Cache Size: "
operator|+
name|getHConnectionManagerCacheSize
argument_list|()
operator|+
literal|", Valid Keys: "
operator|+
name|getValidKeyCount
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
name|HConnectionManager
operator|.
name|MAX_CACHED_HBASE_INSTANCES
argument_list|,
name|getHConnectionManagerCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HConnectionManager
operator|.
name|MAX_CACHED_HBASE_INSTANCES
argument_list|,
name|getValidKeyCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|int
name|getHConnectionManagerCacheSize
parameter_list|()
throws|throws
name|SecurityException
throws|,
name|NoSuchFieldException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
block|{
name|Field
name|cacheField
init|=
name|HConnectionManager
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"HBASE_INSTANCES"
argument_list|)
decl_stmt|;
name|cacheField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|cache
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|cacheField
operator|.
name|get
argument_list|(
literal|null
argument_list|)
decl_stmt|;
return|return
name|cache
operator|.
name|size
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|int
name|getValidKeyCount
parameter_list|()
throws|throws
name|SecurityException
throws|,
name|NoSuchFieldException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
block|{
name|Field
name|cacheField
init|=
name|HConnectionManager
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"HBASE_INSTANCES"
argument_list|)
decl_stmt|;
name|cacheField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|cache
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|cacheField
operator|.
name|get
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|cache
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|HashSet
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|key
range|:
name|keys
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|values
operator|.
name|size
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
block|}
block|}
end_class

end_unit

