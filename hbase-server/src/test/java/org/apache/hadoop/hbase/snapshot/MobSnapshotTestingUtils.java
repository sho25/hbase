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
name|snapshot
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
name|FSDataOutputStream
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|BloomType
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
name|HRegionFileSystem
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
name|FSTableDescriptors
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
name|junit
operator|.
name|Assert
import|;
end_import

begin_class
specifier|public
class|class
name|MobSnapshotTestingUtils
block|{
comment|/**    * Create the Mob Table.    */
specifier|public
specifier|static
name|void
name|createMobTable
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|util
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|int
name|regionReplication
parameter_list|,
specifier|final
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
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
name|htd
operator|.
name|setRegionReplication
argument_list|(
name|regionReplication
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
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
literal|0L
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
name|byte
index|[]
index|[]
name|splitKeys
init|=
name|SnapshotTestingUtils
operator|.
name|getSplitKeys
argument_list|()
decl_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|waitForTableToBeOnline
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|splitKeys
operator|.
name|length
operator|+
literal|1
operator|)
operator|*
name|regionReplication
argument_list|,
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Mob table.    *    * @param util    * @param tableName    * @param families    * @return An HTable instance for the created table.    * @throws IOException    */
specifier|public
specifier|static
name|Table
name|createMobTable
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|util
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
modifier|...
name|families
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
comment|// Disable blooms (they are on by default as of 0.95) but we disable them
comment|// here because
comment|// tests have hard coded counts of what to expect in block cache, etc.,
comment|// and blooms being
comment|// on is interfering.
name|hcd
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
literal|0L
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
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
comment|// HBaseAdmin only waits for regions to appear in hbase:meta we should wait
comment|// until they are assigned
name|util
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getTable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Return the number of rows in the given table.    */
specifier|public
specifier|static
name|int
name|countMobRows
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|count
operator|++
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// Verify the value
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|count
return|;
block|}
comment|/**    * Return the number of rows in the given table.    */
specifier|public
specifier|static
name|int
name|countMobRows
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|,
specifier|final
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
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
name|scan
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|count
operator|++
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// Verify the value
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|count
return|;
block|}
specifier|public
specifier|static
name|void
name|verifyMobRowCount
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|util
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|long
name|expectedRows
parameter_list|)
throws|throws
name|IOException
block|{
name|Table
name|table
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
name|expectedRows
argument_list|,
name|countMobRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|// ==========================================================================
comment|// Snapshot Mock
comment|// ==========================================================================
specifier|public
specifier|static
class|class
name|SnapshotMock
extends|extends
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
block|{
specifier|public
name|SnapshotMock
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|createHtd
parameter_list|(
specifier|final
name|String
name|tableName
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
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
return|return
name|htd
return|;
block|}
block|}
block|}
end_class

end_unit

