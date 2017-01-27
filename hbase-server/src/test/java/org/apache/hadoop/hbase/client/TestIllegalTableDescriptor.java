begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertArrayEquals
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
name|assertSame
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|log4j
operator|.
name|AppenderSkeleton
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|spi
operator|.
name|LoggingEvent
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
comment|// NOTE: Increment tests were moved to their own class, TestIncrementsFromClientSide.
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
name|TestFromClientSide
operator|.
name|class
argument_list|)
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
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"hbase.table.sanity.checks"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// enable for below tests
comment|// We need more than one region server in this test
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testIllegalTableDescriptor"
argument_list|)
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
comment|// create table with 0 families
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setMaxFileSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
comment|// 1K
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setMaxFileSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htd
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
name|htd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htd
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
name|htd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionSplitPolicyClassName
argument_list|(
literal|"nonexisting.foo.class"
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionSplitPolicyClassName
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setBlocksize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
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
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setBlocksize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setTimeToLive
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setTimeToLive
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setTimeToLive
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMinVersions
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMinVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
try|try
block|{
name|hcd
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
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|checkTableIsLegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
comment|// HBASE-13776 Setting illegal versions for HColumnDescriptor
comment|//  does not throw IllegalArgumentException
comment|// finally, minVersions must be less than or equal to maxVersions
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMinVersions
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMinVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setScope
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setScope
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|checkTableIsLegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
try|try
block|{
name|hcd
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
name|hcd
operator|.
name|setValue
argument_list|(
name|HColumnDescriptor
operator|.
name|DFS_REPLICATION
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
name|checkTableIsIllegal
argument_list|(
name|htd
argument_list|)
expr_stmt|;
try|try
block|{
name|hcd
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
name|htd
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Check that logs warn on invalid table but allow it.
name|ListAppender
name|listAppender
init|=
operator|new
name|ListAppender
argument_list|()
decl_stmt|;
name|Logger
name|log
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|HMaster
operator|.
name|class
argument_list|)
decl_stmt|;
name|log
operator|.
name|addAppender
argument_list|(
name|listAppender
argument_list|)
expr_stmt|;
name|log
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setConfiguration
argument_list|(
literal|"hbase.table.sanity.checks"
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
name|htd
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|listAppender
operator|.
name|getMessages
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|listAppender
operator|.
name|getMessages
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|startsWith
argument_list|(
literal|"MEMSTORE_FLUSHSIZE for table "
operator|+
literal|"descriptor or \"hbase.hregion.memstore.flush.size\" (0) is too small, which might "
operator|+
literal|"cause very frequent flushing."
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|removeAppender
argument_list|(
name|listAppender
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkTableIsLegal
parameter_list|(
name|HTableDescriptor
name|htd
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
name|htd
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|htd
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
name|htd
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
name|HTableDescriptor
name|htd
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
name|htd
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
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|ListAppender
extends|extends
name|AppenderSkeleton
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|append
parameter_list|(
name|LoggingEvent
name|event
parameter_list|)
block|{
name|messages
operator|.
name|add
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|requiresLayout
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getMessages
parameter_list|()
block|{
return|return
name|messages
return|;
block|}
block|}
block|}
end_class

end_unit

