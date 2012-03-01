begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|net
operator|.
name|InetAddress
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
name|HashMap
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
name|javax
operator|.
name|naming
operator|.
name|NamingException
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
name|HServerAddress
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|net
operator|.
name|DNS
import|;
end_import

begin_comment
comment|/**  * A base for {@link TableInputFormat}s. Receives a {@link HTable}, an  * {@link Scan} instance that defines the input columns etc. Subclasses may use  * other TableRecordReader implementations.  *<p>  * An example of a subclass:  *<pre>  *   class ExampleTIF extends TableInputFormatBase implements JobConfigurable {  *  *     public void configure(JobConf job) {  *       HTable exampleTable = new HTable(HBaseConfiguration.create(job),  *         Bytes.toBytes("exampleTable"));  *       // mandatory  *       setHTable(exampleTable);  *       Text[] inputColumns = new byte [][] { Bytes.toBytes("columnA"),  *         Bytes.toBytes("columnB") };  *       // mandatory  *       setInputColumns(inputColumns);  *       RowFilterInterface exampleFilter = new RegExpRowFilter("keyPrefix.*");  *       // optional  *       setRowFilter(exampleFilter);  *     }  *  *     public void validateInput(JobConf job) throws IOException {  *     }  *  }  *</pre>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|abstract
class|class
name|TableInputFormatBase
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
name|TableInputFormatBase
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Holds the details for the internal scanner. */
specifier|private
name|Scan
name|scan
init|=
literal|null
decl_stmt|;
comment|/** The table to scan. */
specifier|private
name|HTable
name|table
init|=
literal|null
decl_stmt|;
comment|/** The reader scanning the table, can be a custom one. */
specifier|private
name|TableRecordReader
name|tableRecordReader
init|=
literal|null
decl_stmt|;
comment|/** The reverse DNS lookup cache mapping: IPAddress => HostName */
specifier|private
name|HashMap
argument_list|<
name|InetAddress
argument_list|,
name|String
argument_list|>
name|reverseDNSCacheMap
init|=
operator|new
name|HashMap
argument_list|<
name|InetAddress
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/** The NameServer address */
specifier|private
name|String
name|nameServer
init|=
literal|null
decl_stmt|;
comment|/**    * Builds a TableRecordReader. If no TableRecordReader was provided, uses    * the default.    *    * @param split  The split to work with.    * @param context  The current context.    * @return The newly created record reader.    * @throws IOException When creating the reader fails.    * @see org.apache.hadoop.mapreduce.InputFormat#createRecordReader(    *   org.apache.hadoop.mapreduce.InputSplit,    *   org.apache.hadoop.mapreduce.TaskAttemptContext)    */
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
block|{
if|if
condition|(
name|table
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
name|TableSplit
name|tSplit
init|=
operator|(
name|TableSplit
operator|)
name|split
decl_stmt|;
name|TableRecordReader
name|trr
init|=
name|this
operator|.
name|tableRecordReader
decl_stmt|;
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
operator|new
name|Scan
argument_list|(
name|this
operator|.
name|scan
argument_list|)
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
return|return
name|trr
return|;
block|}
comment|/**    * Calculates the splits that will serve as input for the map tasks. The    * number of splits matches the number of regions in a table.    *    * @param context  The current job context.    * @return The list of input splits.    * @throws IOException When creating the list of splits fails.    * @see org.apache.hadoop.mapreduce.InputFormat#getSplits(    *   org.apache.hadoop.mapreduce.JobContext)    */
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
name|table
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No table was provided."
argument_list|)
throw|;
block|}
comment|// Get the name server address and the default value is null.
name|this
operator|.
name|nameServer
operator|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"hbase.nameserver.address"
argument_list|,
literal|null
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
literal|"Expecting at least one region."
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
argument_list|(
name|keys
operator|.
name|getFirst
argument_list|()
operator|.
name|length
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
name|HServerAddress
name|regionServerAddress
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
argument_list|)
operator|.
name|getServerAddress
argument_list|()
decl_stmt|;
name|InetAddress
name|regionAddress
init|=
name|regionServerAddress
operator|.
name|getInetSocketAddress
argument_list|()
operator|.
name|getAddress
argument_list|()
decl_stmt|;
name|String
name|regionLocation
decl_stmt|;
try|try
block|{
name|regionLocation
operator|=
name|reverseDNS
argument_list|(
name|regionAddress
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NamingException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot resolve the host name for "
operator|+
name|regionAddress
operator|+
literal|" because of "
operator|+
name|e
argument_list|)
expr_stmt|;
name|regionLocation
operator|=
name|regionServerAddress
operator|.
name|getHostname
argument_list|()
expr_stmt|;
block|}
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
comment|// determine if the given start an stop key fall into the region
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
name|InputSplit
name|split
init|=
operator|new
name|TableSplit
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|splitStart
argument_list|,
name|splitStop
argument_list|,
name|regionLocation
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
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"getSplits: split -> "
operator|+
name|i
operator|+
literal|" -> "
operator|+
name|split
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|splits
return|;
block|}
specifier|private
name|String
name|reverseDNS
parameter_list|(
name|InetAddress
name|ipAddress
parameter_list|)
throws|throws
name|NamingException
block|{
name|String
name|hostName
init|=
name|this
operator|.
name|reverseDNSCacheMap
operator|.
name|get
argument_list|(
name|ipAddress
argument_list|)
decl_stmt|;
if|if
condition|(
name|hostName
operator|==
literal|null
condition|)
block|{
name|hostName
operator|=
name|DNS
operator|.
name|reverseDns
argument_list|(
name|ipAddress
argument_list|,
name|this
operator|.
name|nameServer
argument_list|)
expr_stmt|;
name|this
operator|.
name|reverseDNSCacheMap
operator|.
name|put
argument_list|(
name|ipAddress
argument_list|,
name|hostName
argument_list|)
expr_stmt|;
block|}
return|return
name|hostName
return|;
block|}
comment|/**    *    *    * Test if the given region is to be included in the InputSplit while splitting    * the regions of a table.    *<p>    * This optimization is effective when there is a specific reasoning to exclude an entire region from the M-R job,    * (and hence, not contributing to the InputSplit), given the start and end keys of the same.<br>    * Useful when we need to remember the last-processed top record and revisit the [last, current) interval for M-R processing,    * continuously. In addition to reducing InputSplits, reduces the load on the region server as well, due to the ordering of the keys.    *<br>    *<br>    * Note: It is possible that<code>endKey.length() == 0</code> , for the last (recent) region.    *<br>    * Override this method, if you want to bulk exclude regions altogether from M-R. By default, no region is excluded( i.e. all regions are included).    *    *    * @param startKey Start key of the region    * @param endKey End key of the region    * @return true, if this region needs to be included as part of the input (default).    *    */
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
comment|/**    * Allows subclasses to get the {@link HTable}.    */
specifier|protected
name|HTable
name|getHTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
return|;
block|}
comment|/**    * Allows subclasses to set the {@link HTable}.    *    * @param table  The table to get the data from.    */
specifier|protected
name|void
name|setHTable
parameter_list|(
name|HTable
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
comment|/**    * Gets the scan defining the actual details like columns etc.    *    * @return The internal scan instance.    */
specifier|public
name|Scan
name|getScan
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|scan
operator|==
literal|null
condition|)
name|this
operator|.
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
return|return
name|scan
return|;
block|}
comment|/**    * Sets the scan defining the actual details like columns etc.    *    * @param scan  The scan to set.    */
specifier|public
name|void
name|setScan
parameter_list|(
name|Scan
name|scan
parameter_list|)
block|{
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
block|}
comment|/**    * Allows subclasses to set the {@link TableRecordReader}.    *    * @param tableRecordReader A different {@link TableRecordReader}    *   implementation.    */
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

