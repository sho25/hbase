begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimaps
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
name|Scan
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|snapshot
operator|.
name|SnapshotTestingUtils
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
name|VerySlowMapReduceTests
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
name|mapreduce
operator|.
name|Job
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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|VerySlowMapReduceTests
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
name|TestMultiTableSnapshotInputFormat
extends|extends
name|MultiTableInputFormatTestBase
block|{
specifier|protected
name|Path
name|restoreDir
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpSnapshots
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|enableDebug
argument_list|(
name|MultiTableSnapshotInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|enableDebug
argument_list|(
name|MultiTableSnapshotInputFormatImpl
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// take a snapshot of every table we have.
for|for
control|(
name|String
name|tableName
range|:
name|TABLES
control|)
block|{
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MultiTableInputFormatTestBase
operator|.
name|INPUT_FAMILY
argument_list|)
argument_list|,
literal|null
argument_list|,
name|snapshotNameForTable
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|restoreDir
operator|=
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initJob
parameter_list|(
name|List
argument_list|<
name|Scan
argument_list|>
name|scans
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|TableMapReduceUtil
operator|.
name|initMultiTableSnapshotMapperJob
argument_list|(
name|getSnapshotScanMapping
argument_list|(
name|scans
argument_list|)
argument_list|,
name|ScanMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|job
argument_list|,
literal|true
argument_list|,
name|restoreDir
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|getSnapshotScanMapping
parameter_list|(
specifier|final
name|List
argument_list|<
name|Scan
argument_list|>
name|scans
parameter_list|)
block|{
return|return
name|Multimaps
operator|.
name|index
argument_list|(
name|scans
argument_list|,
operator|new
name|Function
argument_list|<
name|Scan
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|apply
parameter_list|(
name|Scan
name|input
parameter_list|)
block|{
return|return
name|snapshotNameForTable
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|input
operator|.
name|getAttribute
argument_list|(
name|Scan
operator|.
name|SCAN_ATTRIBUTES_TABLE_NAME
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
operator|.
name|asMap
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|snapshotNameForTable
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
return|return
name|tableName
operator|+
literal|"_snapshot"
return|;
block|}
block|}
end_class

end_unit

