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
name|rsgroup
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
name|rsgroup
operator|.
name|RSGroupInfoManagerImpl
operator|.
name|META_FAMILY_BYTES
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
name|rsgroup
operator|.
name|RSGroupInfoManagerImpl
operator|.
name|META_QUALIFIER_BYTES
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
name|rsgroup
operator|.
name|RSGroupInfoManagerImpl
operator|.
name|RSGROUP_TABLE_NAME
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
name|TableDescriptors
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
name|client
operator|.
name|Get
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
name|Put
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
name|Result
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
name|Table
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
name|TableDescriptor
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
name|master
operator|.
name|HMaster
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|RSGroupProtos
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
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_comment
comment|/**  * Testcase for HBASE-22819  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMigrateRSGroupInfo
extends|extends
name|TestRSGroupsBase
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
name|TestMigrateRSGroupInfo
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|TABLE_NAME_PREFIX
init|=
literal|"Table_"
decl_stmt|;
specifier|private
specifier|static
name|int
name|NUM_TABLES
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|MASTER_IMPL
argument_list|,
name|HMasterForTest
operator|.
name|class
argument_list|,
name|HMaster
operator|.
name|class
argument_list|)
expr_stmt|;
name|setUpTestBeforeClass
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_TABLES
condition|;
name|i
operator|++
control|)
block|{
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME_PREFIX
operator|+
name|i
argument_list|)
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|tearDownAfterClass
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|CountDownLatch
name|RESUME
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|HMasterForTest
extends|extends
name|HMaster
block|{
specifier|public
name|HMasterForTest
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableDescriptors
name|getTableDescriptors
parameter_list|()
block|{
if|if
condition|(
name|RESUME
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|StackTraceElement
name|element
range|:
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getStackTrace
argument_list|()
control|)
block|{
if|if
condition|(
name|element
operator|.
name|getMethodName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"migrate"
argument_list|)
condition|)
block|{
try|try
block|{
name|RESUME
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{             }
name|RESUME
operator|=
literal|null
expr_stmt|;
break|break;
block|}
block|}
block|}
return|return
name|super
operator|.
name|getTableDescriptors
argument_list|()
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMigrate
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|setAdmin
argument_list|()
expr_stmt|;
name|String
name|groupName
init|=
name|getNameWithoutIndex
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|addGroup
argument_list|(
name|groupName
argument_list|,
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|RSGroupInfo
name|rsGroupInfo
init|=
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|groupName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rsGroupInfo
operator|.
name|getTables
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_TABLES
condition|;
name|i
operator|++
control|)
block|{
name|rsGroupInfo
operator|.
name|addTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME_PREFIX
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|RSGROUP_TABLE_NAME
argument_list|)
init|)
block|{
name|RSGroupProtos
operator|.
name|RSGroupInfo
name|proto
init|=
name|ProtobufUtil
operator|.
name|toProtoGroupInfo
argument_list|(
name|rsGroupInfo
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rsGroupInfo
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|META_FAMILY_BYTES
argument_list|,
name|META_QUALIFIER_BYTES
argument_list|,
name|proto
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|stopMaster
argument_list|(
literal|0
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|RESUME
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|startMaster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|invalidateConnection
argument_list|()
expr_stmt|;
comment|// wait until we can get the rs group info for a table
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
block|{
try|try
block|{
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME_PREFIX
operator|+
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// confirm that before migrating, we could still get the correct rs group for a table.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_TABLES
condition|;
name|i
operator|++
control|)
block|{
name|RSGroupInfo
name|info
init|=
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME_PREFIX
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|rsGroupInfo
operator|.
name|getName
argument_list|()
argument_list|,
name|info
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_TABLES
argument_list|,
name|info
operator|.
name|getTables
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|RESUME
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
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
name|NUM_TABLES
condition|;
name|i
operator|++
control|)
block|{
name|TableDescriptor
name|td
decl_stmt|;
try|try
block|{
name|td
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME_PREFIX
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|rsGroupInfo
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|td
operator|.
name|getRegionServerGroup
argument_list|()
operator|.
name|orElse
argument_list|(
literal|null
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
argument_list|)
expr_stmt|;
comment|// make sure that we persist the result to hbase, where we delete all the tables in the rs
comment|// group.
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
block|{
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|RSGROUP_TABLE_NAME
argument_list|)
init|)
block|{
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rsGroupInfo
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|RSGroupProtos
operator|.
name|RSGroupInfo
name|proto
init|=
name|RSGroupProtos
operator|.
name|RSGroupInfo
operator|.
name|parseFrom
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|META_FAMILY_BYTES
argument_list|,
name|META_QUALIFIER_BYTES
argument_list|)
argument_list|)
decl_stmt|;
name|RSGroupInfo
name|gi
init|=
name|ProtobufUtil
operator|.
name|toGroupInfo
argument_list|(
name|proto
argument_list|)
decl_stmt|;
return|return
name|gi
operator|.
name|getTables
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// make sure that the migrate thread has quit.
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|Thread
operator|.
name|getAllStackTraces
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|noneMatch
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|RSGroupInfoManagerImpl
operator|.
name|MIGRATE_THREAD_NAME
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure we could still get the correct rs group info after migration
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_TABLES
condition|;
name|i
operator|++
control|)
block|{
name|RSGroupInfo
name|info
init|=
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME_PREFIX
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|rsGroupInfo
operator|.
name|getName
argument_list|()
argument_list|,
name|info
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_TABLES
argument_list|,
name|info
operator|.
name|getTables
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

