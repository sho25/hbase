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
name|regionserver
operator|.
name|wal
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
name|Arrays
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
name|HRegionInfo
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
name|ServerName
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
name|Durability
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
name|Increment
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
name|regionserver
operator|.
name|ChunkCreator
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|MemStoreLABImpl
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
name|RegionServerTests
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
name|FSUtils
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
name|wal
operator|.
name|AbstractFSWALProvider
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
name|wal
operator|.
name|WAL
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
name|wal
operator|.
name|WALFactory
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|AfterClass
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameter
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
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Tests for WAL write durability  */
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
name|RegionServerTests
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
name|TestDurability
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
name|FileSystem
name|FS
decl_stmt|;
specifier|private
specifier|static
name|MiniDFSCluster
name|CLUSTER
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|CONF
decl_stmt|;
specifier|private
specifier|static
name|Path
name|DIR
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
specifier|private
specifier|static
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|COL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
decl_stmt|;
annotation|@
name|Parameter
specifier|public
name|String
name|walProvider
decl_stmt|;
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: provider={0}"
argument_list|)
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"defaultProvider"
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|"asyncfs"
block|}
argument_list|)
return|;
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
name|CONF
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|CLUSTER
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
expr_stmt|;
name|FS
operator|=
name|CLUSTER
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|DIR
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"TestDurability"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|CONF
argument_list|,
name|DIR
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|CONF
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|WAL_PROVIDER
argument_list|,
name|walProvider
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|FS
operator|.
name|delete
argument_list|(
name|DIR
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDurability
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|CONF
argument_list|,
literal|null
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"TestDurability"
argument_list|,
literal|16010
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestDurability"
argument_list|)
decl_stmt|;
specifier|final
name|WAL
name|wal
init|=
name|wals
operator|.
name|getWAL
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createHRegion
argument_list|(
name|tableName
argument_list|,
literal|"region"
argument_list|,
name|wal
argument_list|,
name|Durability
operator|.
name|USE_DEFAULT
argument_list|)
decl_stmt|;
name|HRegion
name|deferredRegion
init|=
name|createHRegion
argument_list|(
name|tableName
argument_list|,
literal|"deferredRegion"
argument_list|,
name|wal
argument_list|,
name|Durability
operator|.
name|ASYNC_WAL
argument_list|)
decl_stmt|;
name|region
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// a put through the deferred table does not write to the wal immediately,
comment|// but maybe has been successfully sync-ed by the underlying AsyncWriter +
comment|// AsyncFlusher thread
name|deferredRegion
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// but will after we sync the wal
name|wal
operator|.
name|sync
argument_list|()
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// a put through a deferred table will be sync with the put sync'ed put
name|deferredRegion
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|wal
operator|.
name|sync
argument_list|()
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|4
argument_list|)
expr_stmt|;
comment|// a put through a deferred table will be sync with the put sync'ed put
name|deferredRegion
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|USE_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|wal
operator|.
name|sync
argument_list|()
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|USE_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|6
argument_list|)
expr_stmt|;
comment|// SKIP_WAL never writes to the wal
name|region
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
argument_list|)
expr_stmt|;
name|deferredRegion
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|wal
operator|.
name|sync
argument_list|()
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|6
argument_list|)
expr_stmt|;
comment|// Async overrides sync table default
name|region
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|ASYNC_WAL
argument_list|)
argument_list|)
expr_stmt|;
name|deferredRegion
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|ASYNC_WAL
argument_list|)
argument_list|)
expr_stmt|;
name|wal
operator|.
name|sync
argument_list|()
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|8
argument_list|)
expr_stmt|;
comment|// sync overrides async table default
name|region
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|SYNC_WAL
argument_list|)
argument_list|)
expr_stmt|;
name|deferredRegion
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|SYNC_WAL
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// fsync behaves like sync
name|region
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|FSYNC_WAL
argument_list|)
argument_list|)
expr_stmt|;
name|deferredRegion
operator|.
name|put
argument_list|(
name|newPut
argument_list|(
name|Durability
operator|.
name|FSYNC_WAL
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|12
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncrement
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col3"
argument_list|)
decl_stmt|;
comment|// Setting up region
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|CONF
argument_list|,
literal|null
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"TestIncrement"
argument_list|,
literal|16010
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestIncrement"
argument_list|)
decl_stmt|;
specifier|final
name|WAL
name|wal
init|=
name|wals
operator|.
name|getWAL
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createHRegion
argument_list|(
name|tableName
argument_list|,
literal|"increment"
argument_list|,
name|wal
argument_list|,
name|Durability
operator|.
name|USE_DEFAULT
argument_list|)
decl_stmt|;
comment|// col1: amount = 0, 1 write back to WAL
name|Increment
name|inc1
init|=
operator|new
name|Increment
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Result
name|res
init|=
name|region
operator|.
name|increment
argument_list|(
name|inc1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// col1: amount = 1, 1 write back to WAL
name|inc1
operator|=
operator|new
name|Increment
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|res
operator|=
name|region
operator|.
name|increment
argument_list|(
name|inc1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// col1: amount = 0, 0 write back to WAL
name|inc1
operator|=
operator|new
name|Increment
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|res
operator|=
name|region
operator|.
name|increment
argument_list|(
name|inc1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// col1: amount = 0, col2: amount = 0, col3: amount = 0
comment|// 1 write back to WAL
name|inc1
operator|=
operator|new
name|Increment
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col3
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|res
operator|=
name|region
operator|.
name|increment
argument_list|(
name|inc1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// col1: amount = 5, col2: amount = 4, col3: amount = 3
comment|// 1 write back to WAL
name|inc1
operator|=
operator|new
name|Increment
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col2
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col3
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|res
operator|=
name|region
operator|.
name|increment
argument_list|(
name|inc1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|col3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWALCount
argument_list|(
name|wals
argument_list|,
name|wal
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
comment|/*    * Test when returnResults set to false in increment it should not return the result instead it    * resturn null.    */
annotation|@
name|Test
specifier|public
name|void
name|testIncrementWithReturnResultsSetToFalse
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
comment|// Setting up region
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|CONF
argument_list|,
literal|null
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"testIncrementWithReturnResultsSetToFalse"
argument_list|,
literal|16010
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testIncrementWithReturnResultsSetToFalse"
argument_list|)
decl_stmt|;
specifier|final
name|WAL
name|wal
init|=
name|wals
operator|.
name|getWAL
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createHRegion
argument_list|(
name|tableName
argument_list|,
literal|"increment"
argument_list|,
name|wal
argument_list|,
name|Durability
operator|.
name|USE_DEFAULT
argument_list|)
decl_stmt|;
name|Increment
name|inc1
init|=
operator|new
name|Increment
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|inc1
operator|.
name|setReturnResults
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|inc1
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|col1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Result
name|res
init|=
name|region
operator|.
name|increment
argument_list|(
name|inc1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|res
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Put
name|newPut
parameter_list|(
name|Durability
name|durability
parameter_list|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COL
argument_list|,
name|COL
argument_list|)
expr_stmt|;
if|if
condition|(
name|durability
operator|!=
literal|null
condition|)
block|{
name|p
operator|.
name|setDurability
argument_list|(
name|durability
argument_list|)
expr_stmt|;
block|}
return|return
name|p
return|;
block|}
specifier|private
name|void
name|verifyWALCount
parameter_list|(
name|WALFactory
name|wals
parameter_list|,
name|WAL
name|log
parameter_list|,
name|int
name|expected
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|walPath
init|=
name|AbstractFSWALProvider
operator|.
name|getCurrentFileName
argument_list|(
name|log
argument_list|)
decl_stmt|;
name|WAL
operator|.
name|Reader
name|reader
init|=
name|wals
operator|.
name|createReader
argument_list|(
name|FS
argument_list|,
name|walPath
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
name|WAL
operator|.
name|Entry
name|entry
init|=
operator|new
name|WAL
operator|.
name|Entry
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|entry
argument_list|)
operator|!=
literal|null
condition|)
name|count
operator|++
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
comment|// lifted from TestAtomicOperation
specifier|private
name|HRegion
name|createHRegion
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|callingMethod
parameter_list|,
name|WAL
name|log
parameter_list|,
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
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
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setDurability
argument_list|(
name|durability
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|DIR
operator|+
name|callingMethod
argument_list|)
decl_stmt|;
if|if
condition|(
name|FS
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|FS
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed delete of "
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
name|ChunkCreator
operator|.
name|initialize
argument_list|(
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_DEFAULT
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|path
argument_list|,
name|CONF
argument_list|,
name|htd
argument_list|,
name|log
argument_list|)
return|;
block|}
block|}
end_class

end_unit

