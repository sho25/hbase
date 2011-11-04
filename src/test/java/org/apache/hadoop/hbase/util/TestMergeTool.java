begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|InternalScanner
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
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_comment
comment|/** Test stand alone merge tool that can merge arbitrary regions */
end_comment

begin_class
specifier|public
class|class
name|TestMergeTool
extends|extends
name|HBaseTestCase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestMergeTool
operator|.
name|class
argument_list|)
decl_stmt|;
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
comment|//  static final byte [] COLUMN_NAME = Bytes.toBytes("contents:");
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"contents"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dc"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
index|[]
name|sourceRegions
init|=
operator|new
name|HRegionInfo
index|[
literal|5
index|]
decl_stmt|;
specifier|private
specifier|final
name|HRegion
index|[]
name|regions
init|=
operator|new
name|HRegion
index|[
literal|5
index|]
decl_stmt|;
specifier|private
name|HTableDescriptor
name|desc
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
index|[]
name|rows
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|dfsCluster
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Set the timeout down else this test will take a while to complete.
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.zookeeper.recoverable.waittime"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Make it so we try and connect to a zk that is not there (else we might
comment|// find a zk ensemble put up by another concurrent test and this will
comment|// mess up this test.  Choose unlikely port. Default test port is 21818.
comment|// Default zk port is 2181.
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.zookeeper.property.clientPort"
argument_list|,
literal|10001
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
comment|// Create table description
name|this
operator|.
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"TestMergeTool"
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
comment|/*      * Create the HRegionInfos for the regions.      */
comment|// Region 0 will contain the key range [row_0200,row_0300)
name|sourceRegions
index|[
literal|0
index|]
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|this
operator|.
name|desc
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0200"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0300"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Region 1 will contain the key range [row_0250,row_0400) and overlaps
comment|// with Region 0
name|sourceRegions
index|[
literal|1
index|]
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|this
operator|.
name|desc
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0250"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0400"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Region 2 will contain the key range [row_0100,row_0200) and is adjacent
comment|// to Region 0 or the region resulting from the merge of Regions 0 and 1
name|sourceRegions
index|[
literal|2
index|]
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|this
operator|.
name|desc
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0100"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0200"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Region 3 will contain the key range [row_0500,row_0600) and is not
comment|// adjacent to any of Regions 0, 1, 2 or the merged result of any or all
comment|// of those regions
name|sourceRegions
index|[
literal|3
index|]
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|this
operator|.
name|desc
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0500"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0600"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Region 4 will have empty start and end keys and overlaps all regions.
name|sourceRegions
index|[
literal|4
index|]
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|this
operator|.
name|desc
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
comment|/*      * Now create some row keys      */
name|this
operator|.
name|rows
operator|=
operator|new
name|byte
index|[
literal|5
index|]
index|[]
index|[]
expr_stmt|;
name|this
operator|.
name|rows
index|[
literal|0
index|]
operator|=
name|Bytes
operator|.
name|toByteArrays
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"row_0210"
block|,
literal|"row_0280"
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|rows
index|[
literal|1
index|]
operator|=
name|Bytes
operator|.
name|toByteArrays
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"row_0260"
block|,
literal|"row_0350"
block|,
literal|"row_035"
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|rows
index|[
literal|2
index|]
operator|=
name|Bytes
operator|.
name|toByteArrays
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"row_0110"
block|,
literal|"row_0175"
block|,
literal|"row_0175"
block|,
literal|"row_0175"
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|rows
index|[
literal|3
index|]
operator|=
name|Bytes
operator|.
name|toByteArrays
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"row_0525"
block|,
literal|"row_0560"
block|,
literal|"row_0560"
block|,
literal|"row_0560"
block|,
literal|"row_0560"
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|rows
index|[
literal|4
index|]
operator|=
name|Bytes
operator|.
name|toByteArrays
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"row_0050"
block|,
literal|"row_1000"
block|,
literal|"row_1000"
block|,
literal|"row_1000"
block|,
literal|"row_1000"
block|,
literal|"row_1000"
block|}
argument_list|)
expr_stmt|;
comment|// Start up dfs
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|dfsCluster
operator|=
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|this
operator|.
name|dfsCluster
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"fs="
operator|+
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|parentdir
init|=
name|fs
operator|.
name|getHomeDirectory
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|parentdir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|parentdir
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setVersion
argument_list|(
name|fs
argument_list|,
name|parentdir
argument_list|)
expr_stmt|;
comment|// Note: we must call super.setUp after starting the mini cluster or
comment|// we will end up with a local file system
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Create root and meta regions
name|createRootAndMetaRegions
argument_list|()
expr_stmt|;
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|testDir
argument_list|,
name|this
operator|.
name|desc
argument_list|)
expr_stmt|;
comment|/*        * Create the regions we will merge        */
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sourceRegions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|regions
index|[
name|i
index|]
operator|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|this
operator|.
name|sourceRegions
index|[
name|i
index|]
argument_list|,
name|this
operator|.
name|testDir
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|desc
argument_list|)
expr_stmt|;
comment|/*          * Insert data          */
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|rows
index|[
name|i
index|]
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|rows
index|[
name|i
index|]
index|[
name|j
index|]
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|regions
index|[
name|i
index|]
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|HRegion
operator|.
name|addRegionToMETA
argument_list|(
name|meta
argument_list|,
name|regions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Close root and meta regions
name|closeRootAndMeta
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/*    * @param msg Message that describes this merge    * @param regionName1    * @param regionName2    * @param log Log to use merging.    * @param upperbound Verifying, how high up in this.rows to go.    * @return Merged region.    * @throws Exception    */
specifier|private
name|HRegion
name|mergeAndVerify
parameter_list|(
specifier|final
name|String
name|msg
parameter_list|,
specifier|final
name|String
name|regionName1
parameter_list|,
specifier|final
name|String
name|regionName2
parameter_list|,
specifier|final
name|HLog
name|log
parameter_list|,
specifier|final
name|int
name|upperbound
parameter_list|)
throws|throws
name|Exception
block|{
name|Merge
name|merger
init|=
operator|new
name|Merge
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"fs2="
operator|+
name|this
operator|.
name|conf
operator|.
name|get
argument_list|(
literal|"fs.defaultFS"
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|errCode
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|merger
argument_list|,
operator|new
name|String
index|[]
block|{
name|this
operator|.
name|desc
operator|.
name|getNameAsString
argument_list|()
block|,
name|regionName1
block|,
name|regionName2
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"'"
operator|+
name|msg
operator|+
literal|"' failed with errCode "
operator|+
name|errCode
argument_list|,
name|errCode
operator|==
literal|0
argument_list|)
expr_stmt|;
name|HRegionInfo
name|mergedInfo
init|=
name|merger
operator|.
name|getMergedHRegionInfo
argument_list|()
decl_stmt|;
comment|// Now verify that we can read all the rows from regions 0, 1
comment|// in the new merged region.
name|HRegion
name|merged
init|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|mergedInfo
argument_list|,
name|this
operator|.
name|desc
argument_list|,
name|log
argument_list|,
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|verifyMerge
argument_list|(
name|merged
argument_list|,
name|upperbound
argument_list|)
expr_stmt|;
name|merged
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Verified "
operator|+
name|msg
argument_list|)
expr_stmt|;
return|return
name|merged
return|;
block|}
specifier|private
name|void
name|verifyMerge
parameter_list|(
specifier|final
name|HRegion
name|merged
parameter_list|,
specifier|final
name|int
name|upperbound
parameter_list|)
throws|throws
name|IOException
block|{
comment|//Test
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|FAMILY
argument_list|)
expr_stmt|;
name|InternalScanner
name|scanner
init|=
name|merged
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|testRes
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|testRes
operator|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
expr_stmt|;
name|boolean
name|hasNext
init|=
name|scanner
operator|.
name|next
argument_list|(
name|testRes
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|hasNext
condition|)
block|{
break|break;
block|}
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|//!Test
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|upperbound
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|rows
index|[
name|i
index|]
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|rows
index|[
name|i
index|]
index|[
name|j
index|]
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|FAMILY
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|merged
operator|.
name|get
argument_list|(
name|get
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes
init|=
name|result
operator|.
name|raw
argument_list|()
index|[
literal|0
index|]
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rows
index|[
name|i
index|]
index|[
name|j
index|]
argument_list|)
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|bytes
argument_list|,
name|rows
index|[
name|i
index|]
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Test merge tool.    * @throws Exception    */
specifier|public
name|void
name|testMergeTool
parameter_list|()
throws|throws
name|Exception
block|{
comment|// First verify we can read the rows from the source regions and that they
comment|// contain the right data.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|rows
index|[
name|i
index|]
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|rows
index|[
name|i
index|]
index|[
name|j
index|]
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|FAMILY
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|regions
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|get
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|result
operator|.
name|raw
argument_list|()
index|[
literal|0
index|]
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|bytes
argument_list|,
name|rows
index|[
name|i
index|]
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Close the region and delete the log
name|regions
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
name|regions
index|[
name|i
index|]
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
comment|// Create a log that we can reuse when we need to open regions
name|Path
name|logPath
init|=
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
operator|+
literal|"_"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating log "
operator|+
name|logPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|oldLogDir
init|=
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
decl_stmt|;
name|HLog
name|log
init|=
operator|new
name|HLog
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|logPath
argument_list|,
name|oldLogDir
argument_list|,
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Merge Region 0 and Region 1
name|HRegion
name|merged
init|=
name|mergeAndVerify
argument_list|(
literal|"merging regions 0 and 1"
argument_list|,
name|this
operator|.
name|sourceRegions
index|[
literal|0
index|]
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|this
operator|.
name|sourceRegions
index|[
literal|1
index|]
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|log
argument_list|,
literal|2
argument_list|)
decl_stmt|;
comment|// Merge the result of merging regions 0 and 1 with region 2
name|merged
operator|=
name|mergeAndVerify
argument_list|(
literal|"merging regions 0+1 and 2"
argument_list|,
name|merged
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|this
operator|.
name|sourceRegions
index|[
literal|2
index|]
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|log
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// Merge the result of merging regions 0, 1 and 2 with region 3
name|merged
operator|=
name|mergeAndVerify
argument_list|(
literal|"merging regions 0+1+2 and 3"
argument_list|,
name|merged
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|this
operator|.
name|sourceRegions
index|[
literal|3
index|]
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|log
argument_list|,
literal|4
argument_list|)
expr_stmt|;
comment|// Merge the result of merging regions 0, 1, 2 and 3 with region 4
name|merged
operator|=
name|mergeAndVerify
argument_list|(
literal|"merging regions 0+1+2+3 and 4"
argument_list|,
name|merged
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|this
operator|.
name|sourceRegions
index|[
literal|4
index|]
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|log
argument_list|,
name|rows
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|log
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

