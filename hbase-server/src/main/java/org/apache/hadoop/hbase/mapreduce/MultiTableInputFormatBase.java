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
name|mapreduce
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
name|text
operator|.
name|MessageFormat
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|HRegionLocation
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
name|client
operator|.
name|Table
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
name|Pair
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
name|RegionSizeCalculator
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
name|InputFormat
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
name|InputSplit
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
name|JobContext
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
name|RecordReader
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
name|TaskAttemptContext
import|;
end_import

begin_comment
comment|/**  * A base for {@link MultiTableInputFormat}s. Receives a list of  * {@link Scan} instances that define the input tables and  * filters etc. Subclasses may use other TableRecordReader implementations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|MultiTableInputFormatBase
extends|extends
name|InputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MultiTableInputFormatBase
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Holds the set of scans used to define the input. */
specifier|private
name|List
argument_list|<
name|Scan
argument_list|>
name|scans
decl_stmt|;
comment|/** The reader scanning the table, can be a custom one. */
specifier|private
name|TableRecordReader
name|tableRecordReader
init|=
literal|null
decl_stmt|;
comment|/**    * Builds a TableRecordReader. If no TableRecordReader was provided, uses the    * default.    *    * @param split The split to work with.    * @param context The current context.    * @return The newly created record reader.    * @throws IOException When creating the reader fails.    * @throws InterruptedException when record reader initialization fails    * @see org.apache.hadoop.mapreduce.InputFormat#createRecordReader(    *      org.apache.hadoop.mapreduce.InputSplit,    *      org.apache.hadoop.mapreduce.TaskAttemptContext)    */
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|createRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|TableSplit
name|tSplit
init|=
operator|(
name|TableSplit
operator|)
name|split
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Input split length: {0} bytes."
argument_list|,
name|tSplit
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|tSplit
operator|.
name|getTableName
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot create a record reader because of a"
operator|+
literal|" previous error. Please look at the previous logs lines from"
operator|+
literal|" the task's full log for more details."
argument_list|)
throw|;
block|}
name|Table
name|table
init|=
operator|new
name|HTable
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tSplit
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|TableRecordReader
name|trr
init|=
name|this
operator|.
name|tableRecordReader
decl_stmt|;
try|try
block|{
comment|// if no table record reader was provided use default
if|if
condition|(
name|trr
operator|==
literal|null
condition|)
block|{
name|trr
operator|=
operator|new
name|TableRecordReader
argument_list|()
expr_stmt|;
block|}
name|Scan
name|sc
init|=
name|tSplit
operator|.
name|getScan
argument_list|()
decl_stmt|;
name|sc
operator|.
name|setStartRow
argument_list|(
name|tSplit
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
name|sc
operator|.
name|setStopRow
argument_list|(
name|tSplit
operator|.
name|getEndRow
argument_list|()
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setScan
argument_list|(
name|sc
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setHTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// If there is an exception make sure that all
comment|// resources are closed and released.
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|trr
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
name|ioe
throw|;
block|}
return|return
name|trr
return|;
block|}
comment|/**    * Calculates the splits that will serve as input for the map tasks. The    * number of splits matches the number of regions in a table.    *    * @param context The current job context.    * @return The list of input splits.    * @throws IOException When creating the list of splits fails.    * @see org.apache.hadoop.mapreduce.InputFormat#getSplits(org.apache.hadoop.mapreduce.JobContext)    */
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|scans
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No scans were provided."
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Scan
name|scan
range|:
name|scans
control|)
block|{
name|byte
index|[]
name|tableName
init|=
name|scan
operator|.
name|getAttribute
argument_list|(
name|Scan
operator|.
name|SCAN_ATTRIBUTES_TABLE_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableName
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"A scan object did not have a table name"
argument_list|)
throw|;
name|HTable
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|keys
init|=
name|table
operator|.
name|getStartEndKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|keys
operator|==
literal|null
operator|||
name|keys
operator|.
name|getFirst
argument_list|()
operator|==
literal|null
operator|||
name|keys
operator|.
name|getFirst
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expecting at least one region for table : "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
throw|;
block|}
name|int
name|count
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|startRow
init|=
name|scan
operator|.
name|getStartRow
argument_list|()
decl_stmt|;
name|byte
index|[]
name|stopRow
init|=
name|scan
operator|.
name|getStopRow
argument_list|()
decl_stmt|;
name|RegionSizeCalculator
name|sizeCalculator
init|=
operator|new
name|RegionSizeCalculator
argument_list|(
name|table
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
name|keys
operator|.
name|getFirst
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|includeRegionInSplit
argument_list|(
name|keys
operator|.
name|getFirst
argument_list|()
index|[
name|i
index|]
argument_list|,
name|keys
operator|.
name|getSecond
argument_list|()
index|[
name|i
index|]
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|HRegionLocation
name|hregionLocation
init|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|keys
operator|.
name|getFirst
argument_list|()
index|[
name|i
index|]
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|String
name|regionHostname
init|=
name|hregionLocation
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|HRegionInfo
name|regionInfo
init|=
name|hregionLocation
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
comment|// determine if the given start and stop keys fall into the range
if|if
condition|(
operator|(
name|startRow
operator|.
name|length
operator|==
literal|0
operator|||
name|keys
operator|.
name|getSecond
argument_list|()
index|[
name|i
index|]
operator|.
name|length
operator|==
literal|0
operator|||
name|Bytes
operator|.
name|compareTo
argument_list|(
name|startRow
argument_list|,
name|keys
operator|.
name|getSecond
argument_list|()
index|[
name|i
index|]
argument_list|)
operator|<
literal|0
operator|)
operator|&&
operator|(
name|stopRow
operator|.
name|length
operator|==
literal|0
operator|||
name|Bytes
operator|.
name|compareTo
argument_list|(
name|stopRow
argument_list|,
name|keys
operator|.
name|getFirst
argument_list|()
index|[
name|i
index|]
argument_list|)
operator|>
literal|0
operator|)
condition|)
block|{
name|byte
index|[]
name|splitStart
init|=
name|startRow
operator|.
name|length
operator|==
literal|0
operator|||
name|Bytes
operator|.
name|compareTo
argument_list|(
name|keys
operator|.
name|getFirst
argument_list|()
index|[
name|i
index|]
argument_list|,
name|startRow
argument_list|)
operator|>=
literal|0
condition|?
name|keys
operator|.
name|getFirst
argument_list|()
index|[
name|i
index|]
else|:
name|startRow
decl_stmt|;
name|byte
index|[]
name|splitStop
init|=
operator|(
name|stopRow
operator|.
name|length
operator|==
literal|0
operator|||
name|Bytes
operator|.
name|compareTo
argument_list|(
name|keys
operator|.
name|getSecond
argument_list|()
index|[
name|i
index|]
argument_list|,
name|stopRow
argument_list|)
operator|<=
literal|0
operator|)
operator|&&
name|keys
operator|.
name|getSecond
argument_list|()
index|[
name|i
index|]
operator|.
name|length
operator|>
literal|0
condition|?
name|keys
operator|.
name|getSecond
argument_list|()
index|[
name|i
index|]
else|:
name|stopRow
decl_stmt|;
name|long
name|regionSize
init|=
name|sizeCalculator
operator|.
name|getRegionSize
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|TableSplit
name|split
init|=
operator|new
name|TableSplit
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|scan
argument_list|,
name|splitStart
argument_list|,
name|splitStop
argument_list|,
name|regionHostname
argument_list|,
name|regionSize
argument_list|)
decl_stmt|;
name|splits
operator|.
name|add
argument_list|(
name|split
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"getSplits: split -> "
operator|+
operator|(
name|count
operator|++
operator|)
operator|+
literal|" -> "
operator|+
name|split
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
literal|null
operator|!=
name|table
condition|)
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|splits
return|;
block|}
comment|/**    * Test if the given region is to be included in the InputSplit while    * splitting the regions of a table.    *<p>    * This optimization is effective when there is a specific reasoning to    * exclude an entire region from the M-R job, (and hence, not contributing to    * the InputSplit), given the start and end keys of the same.<br>    * Useful when we need to remember the last-processed top record and revisit    * the [last, current) interval for M-R processing, continuously. In addition    * to reducing InputSplits, reduces the load on the region server as well, due    * to the ordering of the keys.<br>    *<br>    * Note: It is possible that<code>endKey.length() == 0</code> , for the last    * (recent) region.<br>    * Override this method, if you want to bulk exclude regions altogether from    * M-R. By default, no region is excluded( i.e. all regions are included).    *    * @param startKey Start key of the region    * @param endKey End key of the region    * @return true, if this region needs to be included as part of the input    *         (default).    */
specifier|protected
name|boolean
name|includeRegionInSplit
parameter_list|(
specifier|final
name|byte
index|[]
name|startKey
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
comment|/**    * Allows subclasses to get the list of {@link Scan} objects.    */
specifier|protected
name|List
argument_list|<
name|Scan
argument_list|>
name|getScans
parameter_list|()
block|{
return|return
name|this
operator|.
name|scans
return|;
block|}
comment|/**    * Allows subclasses to set the list of {@link Scan} objects.    *    * @param scans The list of {@link Scan} used to define the input    */
specifier|protected
name|void
name|setScans
parameter_list|(
name|List
argument_list|<
name|Scan
argument_list|>
name|scans
parameter_list|)
block|{
name|this
operator|.
name|scans
operator|=
name|scans
expr_stmt|;
block|}
comment|/**    * Allows subclasses to set the {@link TableRecordReader}.    *    * @param tableRecordReader A different {@link TableRecordReader}    *          implementation.    */
specifier|protected
name|void
name|setTableRecordReader
parameter_list|(
name|TableRecordReader
name|tableRecordReader
parameter_list|)
block|{
name|this
operator|.
name|tableRecordReader
operator|=
name|tableRecordReader
expr_stmt|;
block|}
block|}
end_class

end_unit

