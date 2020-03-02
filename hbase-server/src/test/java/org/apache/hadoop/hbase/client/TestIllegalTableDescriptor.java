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
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|contains
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|testclassification
operator|.
name|ClientTests
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
name|TableDescriptorChecker
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
name|Rule
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
name|rules
operator|.
name|TestName
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestIllegalTableDescriptor
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
name|TestIllegalTableDescriptor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// NOTE: Increment tests were moved to their own class, TestIncrementsFromClientSide.
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
decl_stmt|;
specifier|protected
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
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
static|static
block|{
name|LOGGER
operator|=
name|mock
argument_list|(
name|Logger
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
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
comment|// replacing HMaster.LOG with our mock logger for verifying logging
name|Field
name|field
init|=
name|TableDescriptorChecker
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"LOG"
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|field
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|LOGGER
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|TableDescriptorChecker
operator|.
name|TABLE_SANITY_CHECKS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// enable for below tests
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
annotation|@
name|Test
specifier|public
name|void
name|testIllegalTableDescriptor
parameter_list|()
throws|throws
name|Exception
block|{
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
name|familyDescriptor
init|=
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
comment|// create table with 0 families
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
name|familyDescriptor
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setMaxFileSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
comment|// 1K
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setMaxFileSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setMaxFileSize
argument_list|(
literal|1024
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// 1G
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|128
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// 128M
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setRegionSplitPolicyClassName
argument_list|(
literal|"nonexisting.foo.class"
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setRegionSplitPolicyClassName
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setValue
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
literal|"nonexisting.foo.class"
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|removeValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setBlocksize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setBlocksize
argument_list|(
literal|1024
operator|*
literal|1024
operator|*
literal|128
argument_list|)
expr_stmt|;
comment|// 128M
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setBlocksize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setTimeToLive
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setTimeToLive
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setTimeToLive
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setMinVersions
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setMinVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
try|try
block|{
name|familyDescriptor
operator|.
name|setMaxVersions
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
name|familyDescriptor
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
comment|// HBASE-13776 Setting illegal versions for HColumnDescriptor
comment|//  does not throw IllegalArgumentException
comment|// finally, minVersions must be less than or equal to maxVersions
name|familyDescriptor
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setMinVersions
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setMinVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setScope
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setScope
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setValue
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|IN_MEMORY_COMPACTION
argument_list|,
literal|"INVALID"
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setValue
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|IN_MEMORY_COMPACTION
argument_list|,
literal|"NONE"
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
try|try
block|{
name|familyDescriptor
operator|.
name|setDFSReplication
argument_list|(
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Illegal value for setDFSReplication did not throw"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// pass
block|}
comment|// set an illegal DFS replication value by hand
name|familyDescriptor
operator|.
name|setValue
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|DFS_REPLICATION
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
try|try
block|{
name|familyDescriptor
operator|.
name|setDFSReplication
argument_list|(
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw exception if an illegal value is explicitly being set"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// pass
block|}
comment|// check the conf settings to disable sanity checks
name|tableDescriptor
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Check that logs warn on invalid table but allow it.
name|tableDescriptor
operator|.
name|setValue
argument_list|(
name|TableDescriptorChecker
operator|.
name|TABLE_SANITY_CHECKS
argument_list|,
name|Boolean
operator|.
name|FALSE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|LOGGER
argument_list|)
operator|.
name|warn
argument_list|(
name|contains
argument_list|(
literal|"MEMSTORE_FLUSHSIZE for table "
operator|+
literal|"descriptor or \"hbase.hregion.memstore.flush.size\" (0) is too small, which might "
operator|+
literal|"cause very frequent flushing."
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkTableIsLegal
parameter_list|(
name|TableDescriptor
name|tableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkTableIsIllegal
parameter_list|(
name|TableDescriptor
name|tableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// should throw ex
block|}
name|assertFalse
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

