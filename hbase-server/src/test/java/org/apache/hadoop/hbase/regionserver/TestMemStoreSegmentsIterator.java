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
name|CellComparator
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
name|KeyValue
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
name|EnvironmentEdgeManagerTestHelper
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

begin_comment
comment|/**  * Test the {@link MemStoreCompactorSegmentsIterator} and {@link MemStoreMergerSegmentsIterator}  * class, Test for bug : HBASE-22324  */
end_comment

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
name|TestMemStoreSegmentsIterator
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
name|TestMemStoreSegmentsIterator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|String
name|TABLE
init|=
literal|"test_mscsi"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|FAMILY
init|=
literal|"f"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|COLUMN
init|=
literal|"c"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|ROOT_SUB_PATH
init|=
literal|"testMemStoreSegmentsIterator"
decl_stmt|;
specifier|protected
specifier|static
name|long
name|LESS_THAN_INTEGER_MAX_VALUE_SEQ_ID
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|-
literal|1
decl_stmt|;
specifier|protected
specifier|static
name|long
name|GREATER_THAN_INTEGER_MAX_VALUE_SEQ_ID
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|+
literal|1
decl_stmt|;
specifier|protected
name|CellComparator
name|comparator
decl_stmt|;
specifier|protected
name|int
name|compactionKVMax
decl_stmt|;
specifier|protected
name|WAL
name|wal
decl_stmt|;
specifier|protected
name|HRegion
name|region
decl_stmt|;
specifier|protected
name|HStore
name|store
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|HBaseTestingUtility
name|hbaseUtility
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|(
name|conf
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
name|TABLE
argument_list|)
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
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Path
name|rootPath
init|=
name|hbaseUtility
operator|.
name|getDataTestDir
argument_list|(
name|ROOT_SUB_PATH
argument_list|)
decl_stmt|;
name|this
operator|.
name|wal
operator|=
name|hbaseUtility
operator|.
name|createWal
argument_list|(
name|conf
argument_list|,
name|rootPath
argument_list|,
name|info
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|rootPath
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|,
name|this
operator|.
name|wal
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|=
operator|new
name|HStore
argument_list|(
name|this
operator|.
name|region
argument_list|,
name|hcd
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|CellComparator
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|this
operator|.
name|compactionKVMax
operator|=
name|HConstants
operator|.
name|COMPACTION_KV_MAX_DEFAULT
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMemStoreCompactorSegmentsIteratorNext
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|createTestImmutableSegment
argument_list|()
argument_list|)
decl_stmt|;
name|MemStoreCompactorSegmentsIterator
name|iterator
init|=
operator|new
name|MemStoreCompactorSegmentsIterator
argument_list|(
name|segments
argument_list|,
name|this
operator|.
name|comparator
argument_list|,
name|this
operator|.
name|compactionKVMax
argument_list|,
name|this
operator|.
name|store
argument_list|)
decl_stmt|;
name|verifyNext
argument_list|(
name|iterator
argument_list|)
expr_stmt|;
name|closeTestSegments
argument_list|(
name|segments
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMemStoreMergerSegmentsIteratorNext
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|createTestImmutableSegment
argument_list|()
argument_list|)
decl_stmt|;
name|MemStoreMergerSegmentsIterator
name|iterator
init|=
operator|new
name|MemStoreMergerSegmentsIterator
argument_list|(
name|segments
argument_list|,
name|this
operator|.
name|comparator
argument_list|,
name|this
operator|.
name|compactionKVMax
argument_list|)
decl_stmt|;
name|verifyNext
argument_list|(
name|iterator
argument_list|)
expr_stmt|;
name|closeTestSegments
argument_list|(
name|segments
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|ImmutableSegment
name|createTestImmutableSegment
parameter_list|()
block|{
name|ImmutableSegment
name|segment1
init|=
name|SegmentFactory
operator|.
name|instance
argument_list|()
operator|.
name|createImmutableSegment
argument_list|(
name|this
operator|.
name|comparator
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|one
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|two
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|f
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|v
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|3
argument_list|)
decl_stmt|;
specifier|final
name|KeyValue
name|kv1
init|=
operator|new
name|KeyValue
argument_list|(
name|one
argument_list|,
name|f
argument_list|,
name|q
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|v
argument_list|)
decl_stmt|;
specifier|final
name|KeyValue
name|kv2
init|=
operator|new
name|KeyValue
argument_list|(
name|two
argument_list|,
name|f
argument_list|,
name|q
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|v
argument_list|)
decl_stmt|;
comment|// the seqId of first cell less than Integer.MAX_VALUE,
comment|// the seqId of second cell greater than integer.MAX_VALUE
name|kv1
operator|.
name|setSequenceId
argument_list|(
name|LESS_THAN_INTEGER_MAX_VALUE_SEQ_ID
argument_list|)
expr_stmt|;
name|kv2
operator|.
name|setSequenceId
argument_list|(
name|GREATER_THAN_INTEGER_MAX_VALUE_SEQ_ID
argument_list|)
expr_stmt|;
name|segment1
operator|.
name|internalAdd
argument_list|(
name|kv1
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|segment1
operator|.
name|internalAdd
argument_list|(
name|kv2
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
name|segment1
return|;
block|}
specifier|protected
name|void
name|closeTestSegments
parameter_list|(
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
parameter_list|)
block|{
for|for
control|(
name|Segment
name|segment
range|:
name|segments
control|)
block|{
name|segment
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|verifyNext
parameter_list|(
name|MemStoreSegmentsIterator
name|iterator
parameter_list|)
block|{
comment|// check first cell
name|assertTrue
argument_list|(
name|iterator
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|firstCell
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|LESS_THAN_INTEGER_MAX_VALUE_SEQ_ID
argument_list|,
name|firstCell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
comment|// check second cell
name|assertTrue
argument_list|(
name|iterator
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|secondCell
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|GREATER_THAN_INTEGER_MAX_VALUE_SEQ_ID
argument_list|,
name|secondCell
operator|.
name|getSequenceId
argument_list|()
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
name|Exception
block|{
name|EnvironmentEdgeManagerTestHelper
operator|.
name|reset
argument_list|()
expr_stmt|;
if|if
condition|(
name|store
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|store
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{       }
name|store
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|wal
operator|!=
literal|null
condition|)
block|{
name|wal
operator|.
name|close
argument_list|()
expr_stmt|;
name|wal
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

