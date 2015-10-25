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
name|when
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
name|Collection
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
name|Cell
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
name|CellUtil
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
name|testclassification
operator|.
name|SmallTests
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
name|Stoppable
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
name|util
operator|.
name|StoppableImplementation
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
name|Before
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
name|RegionServerTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestStoreFileRefresherChore
block|{
specifier|private
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
name|Path
name|testDir
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|testDir
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestStoreFileRefresherChore"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|testDir
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HTableDescriptor
name|getTableDesc
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
comment|// Set default to be three versions.
name|hcd
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
return|return
name|htd
return|;
block|}
specifier|static
class|class
name|FailingHRegionFileSystem
extends|extends
name|HRegionFileSystem
block|{
name|boolean
name|fail
init|=
literal|false
decl_stmt|;
name|FailingHRegionFileSystem
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|tableDir
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|tableDir
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|StoreFileInfo
argument_list|>
name|getStoreFiles
parameter_list|(
name|String
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fail
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"simulating FS failure"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|getStoreFiles
argument_list|(
name|familyName
argument_list|)
return|;
block|}
block|}
specifier|private
name|Region
name|initHRegion
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|stopKey
parameter_list|,
name|int
name|replicaId
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|testDir
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
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
name|startKey
argument_list|,
name|stopKey
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|replicaId
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|fs
init|=
operator|new
name|FailingHRegionFileSystem
argument_list|(
name|conf
argument_list|,
name|tableDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|tableDir
argument_list|,
name|info
argument_list|)
decl_stmt|;
specifier|final
name|Configuration
name|walConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|walConf
argument_list|,
name|tableDir
argument_list|)
expr_stmt|;
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|walConf
argument_list|,
literal|null
argument_list|,
literal|"log_"
operator|+
name|replicaId
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
operator|new
name|HRegion
argument_list|(
name|fs
argument_list|,
name|wals
operator|.
name|getWAL
argument_list|(
name|info
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|info
operator|.
name|getTable
argument_list|()
operator|.
name|getNamespace
argument_list|()
argument_list|)
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|region
operator|.
name|initialize
argument_list|()
expr_stmt|;
return|return
name|region
return|;
block|}
specifier|private
name|void
name|putData
parameter_list|(
name|Region
name|region
parameter_list|,
name|int
name|startRow
parameter_list|,
name|int
name|numRows
parameter_list|,
name|byte
index|[]
name|qf
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
name|startRow
init|;
name|i
operator|<
name|startRow
operator|+
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verifyData
parameter_list|(
name|Region
name|newReg
parameter_list|,
name|int
name|startRow
parameter_list|,
name|int
name|numRows
parameter_list|,
name|byte
index|[]
name|qf
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
name|startRow
init|;
name|i
operator|<
name|startRow
operator|+
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|i
argument_list|)
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|get
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
expr_stmt|;
block|}
name|Result
name|result
init|=
name|newReg
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|Cell
index|[]
name|raw
init|=
name|result
operator|.
name|rawCells
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|families
operator|.
name|length
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|families
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|raw
index|[
name|j
index|]
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|raw
index|[
name|j
index|]
argument_list|,
name|families
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|raw
index|[
name|j
index|]
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|static
class|class
name|StaleStorefileRefresherChore
extends|extends
name|StorefileRefresherChore
block|{
name|boolean
name|isStale
init|=
literal|false
decl_stmt|;
specifier|public
name|StaleStorefileRefresherChore
parameter_list|(
name|int
name|period
parameter_list|,
name|HRegionServer
name|regionServer
parameter_list|,
name|Stoppable
name|stoppable
parameter_list|)
block|{
name|super
argument_list|(
name|period
argument_list|,
literal|false
argument_list|,
name|regionServer
argument_list|,
name|stoppable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isRegionStale
parameter_list|(
name|String
name|encodedName
parameter_list|,
name|long
name|time
parameter_list|)
block|{
return|return
name|isStale
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIsStale
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|period
init|=
literal|0
decl_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
block|}
decl_stmt|;
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|mock
argument_list|(
name|HRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Region
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|Region
argument_list|>
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|regionServer
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regions
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|regionServer
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|getTableDesc
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testIsStale"
argument_list|)
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|Region
name|primary
init|=
name|initHRegion
argument_list|(
name|htd
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Region
name|replica1
init|=
name|initHRegion
argument_list|(
name|htd
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|primary
argument_list|)
expr_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|replica1
argument_list|)
expr_stmt|;
name|StaleStorefileRefresherChore
name|chore
init|=
operator|new
name|StaleStorefileRefresherChore
argument_list|(
name|period
argument_list|,
name|regionServer
argument_list|,
operator|new
name|StoppableImplementation
argument_list|()
argument_list|)
decl_stmt|;
comment|// write some data to primary and flush
name|putData
argument_list|(
name|primary
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|primary
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|primary
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
try|try
block|{
name|verifyData
argument_list|(
name|replica1
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|ex
parameter_list|)
block|{
comment|// expected
block|}
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
name|verifyData
argument_list|(
name|replica1
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
comment|// simulate an fs failure where we cannot refresh the store files for the replica
operator|(
call|(
name|FailingHRegionFileSystem
call|)
argument_list|(
operator|(
name|HRegion
operator|)
name|replica1
argument_list|)
operator|.
name|getRegionFileSystem
argument_list|()
operator|)
operator|.
name|fail
operator|=
literal|true
expr_stmt|;
comment|// write some more data to primary and flush
name|putData
argument_list|(
name|primary
argument_list|,
literal|100
argument_list|,
literal|100
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|primary
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|primary
argument_list|,
literal|0
argument_list|,
literal|200
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
comment|// should not throw ex, but we cannot refresh the store files
name|verifyData
argument_list|(
name|replica1
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
try|try
block|{
name|verifyData
argument_list|(
name|replica1
argument_list|,
literal|100
argument_list|,
literal|100
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|ex
parameter_list|)
block|{
comment|// expected
block|}
name|chore
operator|.
name|isStale
operator|=
literal|true
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
comment|//now after this, we cannot read back any value
try|try
block|{
name|verifyData
argument_list|(
name|replica1
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|qf
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"should have failed with IOException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
block|}
block|}
end_class

end_unit

