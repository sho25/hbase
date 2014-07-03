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
name|util
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
name|MetaTableAccessor
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
name|HBaseAdmin
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
name|HConnection
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
name|HConnectionManager
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
name|regionserver
operator|.
name|HRegion
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
comment|/**  * Tests merging a normal table's regions  */
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
name|TestMergeTable
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
name|TestMergeTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|UTIL
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
name|COLUMN_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"contents"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
decl_stmt|;
static|static
block|{
comment|// We will use the same value for the rows as that is not really important here
name|String
name|partialValue
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|StringBuilder
name|val
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|val
operator|.
name|length
argument_list|()
operator|<
literal|1024
condition|)
block|{
name|val
operator|.
name|append
argument_list|(
name|partialValue
argument_list|)
expr_stmt|;
block|}
name|VALUE
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|val
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test merge.    * Hand-makes regions of a mergeable size and adds the hand-made regions to    * hand-made meta.  The hand-made regions are created offline.  We then start    * up mini cluster, disables the hand-made table and starts in on merging.    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testMergeTable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Table we are manually creating offline.
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// Set maximum regionsize down.
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MAX_FILESIZE
argument_list|,
literal|64L
operator|*
literal|1024L
operator|*
literal|1024L
argument_list|)
expr_stmt|;
comment|// Make it so we don't split.
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.regionSplitLimit"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Startup hdfs.  Its in here we'll be putting our manually made regions.
name|UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// Create hdfs hbase rootdir.
name|Path
name|rootdir
init|=
name|UTIL
operator|.
name|createRootDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|rootdir
argument_list|)
condition|)
block|{
if|if
condition|(
name|fs
operator|.
name|delete
argument_list|(
name|rootdir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaned up existing "
operator|+
name|rootdir
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Now create three data regions: The first is too large to merge since it
comment|// will be> 64 MB in size. The second two will be smaller and will be
comment|// selected for merging.
comment|// To ensure that the first region is larger than 64MB we need to write at
comment|// least 65536 rows. We will make certain by writing 70000
name|byte
index|[]
name|row_70001
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_70001"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row_80001
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_80001"
argument_list|)
decl_stmt|;
comment|// Create regions and populate them at same time.  Create the tabledir
comment|// for them first.
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
operator|.
name|createTableDescriptor
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HRegion
index|[]
name|regions
init|=
block|{
name|createRegion
argument_list|(
name|desc
argument_list|,
literal|null
argument_list|,
name|row_70001
argument_list|,
literal|1
argument_list|,
literal|70000
argument_list|,
name|rootdir
argument_list|)
block|,
name|createRegion
argument_list|(
name|desc
argument_list|,
name|row_70001
argument_list|,
name|row_80001
argument_list|,
literal|70001
argument_list|,
literal|10000
argument_list|,
name|rootdir
argument_list|)
block|,
name|createRegion
argument_list|(
name|desc
argument_list|,
name|row_80001
argument_list|,
literal|null
argument_list|,
literal|80001
argument_list|,
literal|11000
argument_list|,
name|rootdir
argument_list|)
block|}
decl_stmt|;
comment|// Now create the root and meta regions and insert the data regions
comment|// created above into hbase:meta
name|setupMeta
argument_list|(
name|rootdir
argument_list|,
name|regions
argument_list|)
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting mini zk cluster"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting mini hbase cluster"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HConnection
name|hConnection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|originalTableRegions
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hConnection
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"originalTableRegions size="
operator|+
name|originalTableRegions
operator|.
name|size
argument_list|()
operator|+
literal|"; "
operator|+
name|originalTableRegions
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HMerge
operator|.
name|merge
argument_list|(
name|c
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|c
argument_list|)
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|postMergeTableRegions
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hConnection
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"postMergeTableRegions size="
operator|+
name|postMergeTableRegions
operator|.
name|size
argument_list|()
operator|+
literal|"; "
operator|+
name|postMergeTableRegions
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"originalTableRegions="
operator|+
name|originalTableRegions
operator|.
name|size
argument_list|()
operator|+
literal|", postMergeTableRegions="
operator|+
name|postMergeTableRegions
operator|.
name|size
argument_list|()
argument_list|,
name|postMergeTableRegions
operator|.
name|size
argument_list|()
operator|<
name|originalTableRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done with merge"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"After cluster shutdown"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HRegion
name|createRegion
parameter_list|(
specifier|final
name|HTableDescriptor
name|desc
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|int
name|firstRow
parameter_list|,
name|int
name|nrows
parameter_list|,
name|Path
name|rootdir
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hri
argument_list|,
name|rootdir
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|desc
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created region "
operator|+
name|region
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|firstRow
init|;
name|i
operator|<
name|firstRow
operator|+
name|nrows
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
literal|"row_"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
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
name|put
operator|.
name|add
argument_list|(
name|COLUMN_NAME
argument_list|,
literal|null
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|10000
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushing write #"
operator|+
name|i
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return
name|region
return|;
block|}
specifier|protected
name|void
name|setupMeta
parameter_list|(
name|Path
name|rootdir
parameter_list|,
specifier|final
name|HRegion
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|meta
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|rootdir
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HTableDescriptor
operator|.
name|META_TABLEDESC
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|regions
control|)
block|{
name|HRegion
operator|.
name|addRegionToMETA
argument_list|(
name|meta
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|meta
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

