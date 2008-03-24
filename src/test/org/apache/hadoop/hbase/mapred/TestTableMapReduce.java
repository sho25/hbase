begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapred
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
name|io
operator|.
name|UnsupportedEncodingException
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|HScannerInterface
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
name|HTable
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
name|HStoreKey
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
name|MultiRegionTable
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
name|StaticTestEnvironment
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
name|BatchUpdate
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
name|io
operator|.
name|MapWritable
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
name|io
operator|.
name|Text
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
name|mapred
operator|.
name|JobClient
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|MiniMRCluster
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
name|mapred
operator|.
name|Reporter
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
name|mapred
operator|.
name|OutputCollector
import|;
end_import

begin_comment
comment|/**  * Test Map/Reduce job over HBase tables  */
end_comment

begin_class
specifier|public
class|class
name|TestTableMapReduce
extends|extends
name|MultiRegionTable
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"hiding"
argument_list|)
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
name|TestTableMapReduce
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|SINGLE_REGION_TABLE_NAME
init|=
literal|"srtest"
decl_stmt|;
specifier|static
specifier|final
name|String
name|MULTI_REGION_TABLE_NAME
init|=
literal|"mrtest"
decl_stmt|;
specifier|static
specifier|final
name|String
name|INPUT_COLUMN
init|=
literal|"contents:"
decl_stmt|;
specifier|static
specifier|final
name|Text
name|TEXT_INPUT_COLUMN
init|=
operator|new
name|Text
argument_list|(
name|INPUT_COLUMN
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|OUTPUT_COLUMN
init|=
literal|"text:"
decl_stmt|;
specifier|static
specifier|final
name|Text
name|TEXT_OUTPUT_COLUMN
init|=
operator|new
name|Text
argument_list|(
name|OUTPUT_COLUMN
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
index|[]
name|columns
init|=
block|{
name|TEXT_INPUT_COLUMN
block|,
name|TEXT_OUTPUT_COLUMN
block|}
decl_stmt|;
specifier|private
name|Path
name|dir
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|values
init|=
literal|null
decl_stmt|;
static|static
block|{
try|try
block|{
name|values
operator|=
operator|new
name|byte
index|[]
index|[]
block|{
literal|"0123"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
block|,
literal|"abcd"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
block|,
literal|"wxyz"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
block|,
literal|"6789"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
block|}
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** constructor */
specifier|public
name|TestTableMapReduce
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Make sure the cache gets flushed so we trigger a compaction(s) and
comment|// hence splits.
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memcache.flush.size"
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// Always compact if there is more than one store file.
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// This size should make it so we always split using the addContent
comment|// below. After adding all data, the first region is 1.3M
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// Make lease timeout longer, lease checks less frequent
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.period"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.thread.wakefrequency"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Set client pause to the original default
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|/**    * Pass the given key and processed record reduce    */
specifier|public
specifier|static
class|class
name|ProcessContentsMapper
extends|extends
name|TableMap
argument_list|<
name|Text
argument_list|,
name|MapWritable
argument_list|>
block|{
comment|/**      * Pass the key, and reversed value to reduce      *      * @see org.apache.hadoop.hbase.mapred.TableMap#map(org.apache.hadoop.hbase.HStoreKey, org.apache.hadoop.io.MapWritable, org.apache.hadoop.mapred.OutputCollector, org.apache.hadoop.mapred.Reporter)      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|HStoreKey
name|key
parameter_list|,
name|MapWritable
name|value
parameter_list|,
name|OutputCollector
argument_list|<
name|Text
argument_list|,
name|MapWritable
argument_list|>
name|output
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
name|tKey
init|=
name|key
operator|.
name|getRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"There should only be one input column"
argument_list|)
throw|;
block|}
name|Text
index|[]
name|keys
init|=
name|value
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|Text
index|[
name|value
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|keys
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
name|TEXT_INPUT_COLUMN
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Wrong input column. Expected: "
operator|+
name|INPUT_COLUMN
operator|+
literal|" but got: "
operator|+
name|keys
index|[
literal|0
index|]
argument_list|)
throw|;
block|}
comment|// Get the original value and reverse it
name|String
name|originalValue
init|=
operator|new
name|String
argument_list|(
operator|(
operator|(
name|ImmutableBytesWritable
operator|)
name|value
operator|.
name|get
argument_list|(
name|keys
index|[
literal|0
index|]
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
decl_stmt|;
name|StringBuilder
name|newValue
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|originalValue
operator|.
name|length
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|newValue
operator|.
name|append
argument_list|(
name|originalValue
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Now set the value to be collected
name|MapWritable
name|outval
init|=
operator|new
name|MapWritable
argument_list|()
decl_stmt|;
name|outval
operator|.
name|put
argument_list|(
name|TEXT_OUTPUT_COLUMN
argument_list|,
operator|new
name|ImmutableBytesWritable
argument_list|(
name|newValue
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|collect
argument_list|(
name|tKey
argument_list|,
name|outval
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test hbase mapreduce jobs against single region and multi-region tables.    * @throws IOException    */
specifier|public
name|void
name|testTableMapReduce
parameter_list|()
throws|throws
name|IOException
block|{
name|localTestSingleRegionTable
argument_list|()
expr_stmt|;
name|localTestMultiRegionTable
argument_list|()
expr_stmt|;
block|}
comment|/*    * Test against a single region.    * @throws IOException    */
specifier|private
name|void
name|localTestSingleRegionTable
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|SINGLE_REGION_TABLE_NAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|INPUT_COLUMN
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|OUTPUT_COLUMN
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create a table.
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
comment|// insert some data into the test table
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|SINGLE_REGION_TABLE_NAME
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
operator|new
name|Text
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
name|b
operator|.
name|put
argument_list|(
name|TEXT_INPUT_COLUMN
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Print table contents before map/reduce for "
operator|+
name|SINGLE_REGION_TABLE_NAME
argument_list|)
expr_stmt|;
name|scanTable
argument_list|(
name|SINGLE_REGION_TABLE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
name|MiniMRCluster
name|mrCluster
init|=
operator|new
name|MiniMRCluster
argument_list|(
literal|2
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
try|try
block|{
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|TestTableMapReduce
operator|.
name|class
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|setJobName
argument_list|(
literal|"process column contents"
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setNumMapTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TableMap
operator|.
name|initJob
argument_list|(
name|SINGLE_REGION_TABLE_NAME
argument_list|,
name|INPUT_COLUMN
argument_list|,
name|ProcessContentsMapper
operator|.
name|class
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|TableReduce
operator|.
name|initJob
argument_list|(
name|SINGLE_REGION_TABLE_NAME
argument_list|,
name|IdentityTableReduce
operator|.
name|class
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|SINGLE_REGION_TABLE_NAME
argument_list|)
expr_stmt|;
name|JobClient
operator|.
name|runJob
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Print table contents after map/reduce for "
operator|+
name|SINGLE_REGION_TABLE_NAME
argument_list|)
expr_stmt|;
name|scanTable
argument_list|(
name|SINGLE_REGION_TABLE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// verify map-reduce results
name|verify
argument_list|(
name|SINGLE_REGION_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/*    * Test against multiple regions.    * @throws IOException    */
specifier|private
name|void
name|localTestMultiRegionTable
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|MULTI_REGION_TABLE_NAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|INPUT_COLUMN
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|OUTPUT_COLUMN
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create a table.
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
comment|// Populate a table into multiple regions
name|makeMultiRegionTable
argument_list|(
name|conf
argument_list|,
name|cluster
argument_list|,
name|dfsCluster
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|MULTI_REGION_TABLE_NAME
argument_list|,
name|INPUT_COLUMN
argument_list|)
expr_stmt|;
comment|// Verify table indeed has multiple regions
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|MULTI_REGION_TABLE_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|Text
index|[]
name|startKeys
init|=
name|table
operator|.
name|getStartKeys
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|startKeys
operator|.
name|length
operator|>
literal|1
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
name|MiniMRCluster
name|mrCluster
init|=
operator|new
name|MiniMRCluster
argument_list|(
literal|2
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
try|try
block|{
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|TestTableMapReduce
operator|.
name|class
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|setJobName
argument_list|(
literal|"process column contents"
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setNumMapTasks
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TableMap
operator|.
name|initJob
argument_list|(
name|MULTI_REGION_TABLE_NAME
argument_list|,
name|INPUT_COLUMN
argument_list|,
name|ProcessContentsMapper
operator|.
name|class
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|TableReduce
operator|.
name|initJob
argument_list|(
name|MULTI_REGION_TABLE_NAME
argument_list|,
name|IdentityTableReduce
operator|.
name|class
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|MULTI_REGION_TABLE_NAME
argument_list|)
expr_stmt|;
name|JobClient
operator|.
name|runJob
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
comment|// verify map-reduce results
name|verify
argument_list|(
name|MULTI_REGION_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|scanTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|printValues
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
if|if
condition|(
name|printValues
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"row: "
operator|+
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|" column: "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|" value: "
operator|+
operator|new
name|String
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"null"
argument_list|)
specifier|private
name|void
name|verify
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|verified
init|=
literal|false
decl_stmt|;
name|long
name|pause
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|numRetries
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|5
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRetries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|verifyAttempt
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|verified
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// If here, a cell was empty.  Presume its because updates came in
comment|// after the scanner had been opened.  Wait a while and retry.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Verification attempt failed: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|assertTrue
argument_list|(
name|verified
argument_list|)
expr_stmt|;
block|}
comment|/**    * Looks at every value of the mapreduce output and verifies that indeed    * the values have been reversed.    * @param table Table to scan.    * @throws IOException    * @throws NullPointerException if we failed to find a cell value    */
specifier|private
name|void
name|verifyAttempt
parameter_list|(
specifier|final
name|HTable
name|table
parameter_list|)
throws|throws
name|IOException
throws|,
name|NullPointerException
block|{
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|results
operator|.
name|size
argument_list|()
operator|>
literal|2
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Too many results, expected 2 got "
operator|+
name|results
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|byte
index|[]
name|firstValue
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|secondValue
init|=
literal|null
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
name|firstValue
operator|=
name|e
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|count
operator|==
literal|1
condition|)
block|{
name|secondValue
operator|=
name|e
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|count
operator|==
literal|2
condition|)
block|{
break|break;
block|}
block|}
name|String
name|first
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|firstValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|": first value is null"
argument_list|)
throw|;
block|}
name|first
operator|=
operator|new
name|String
argument_list|(
name|firstValue
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
name|String
name|second
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|secondValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|": second value is null"
argument_list|)
throw|;
block|}
name|byte
index|[]
name|secondReversed
init|=
operator|new
name|byte
index|[
name|secondValue
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|j
init|=
name|secondValue
operator|.
name|length
operator|-
literal|1
init|;
name|j
operator|>=
literal|0
condition|;
name|j
operator|--
operator|,
name|i
operator|++
control|)
block|{
name|secondReversed
index|[
name|i
index|]
operator|=
name|secondValue
index|[
name|j
index|]
expr_stmt|;
block|}
name|second
operator|=
operator|new
name|String
argument_list|(
name|secondReversed
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
operator|.
name|compareTo
argument_list|(
name|second
argument_list|)
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"second key is not the reverse of first. row="
operator|+
name|key
operator|.
name|getRow
argument_list|()
operator|+
literal|", first value="
operator|+
name|first
operator|+
literal|", second value="
operator|+
name|second
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|()
expr_stmt|;
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
block|}
block|}
end_class

end_unit

