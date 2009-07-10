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
name|ResultScanner
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
name|Writables
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * A base for {@link TableInputFormat}s. Receives a {@link HTable}, an   * {@link Scan} instance that defines the input columns etc. Subclasses may use   * other TableRecordReader implementations.  *<p>  * An example of a subclass:  *<pre>  *   class ExampleTIF extends TableInputFormatBase implements JobConfigurable {  *  *     public void configure(JobConf job) {  *       HTable exampleTable = new HTable(new HBaseConfiguration(job),  *         Bytes.toBytes("exampleTable"));  *       // mandatory  *       setHTable(exampleTable);  *       Text[] inputColumns = new byte [][] { Bytes.toBytes("columnA"),  *         Bytes.toBytes("columnB") };  *       // mandatory  *       setInputColumns(inputColumns);  *       RowFilterInterface exampleFilter = new RegExpRowFilter("keyPrefix.*");  *       // optional  *       setRowFilter(exampleFilter);  *     }  *  *     public void validateInput(JobConf job) throws IOException {  *     }  *  }  *</pre>  */
end_comment

begin_class
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
comment|/**    * Iterate over an HBase table data, return (ImmutableBytesWritable, Result)     * pairs.    */
specifier|protected
class|class
name|TableRecordReader
extends|extends
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|private
name|ResultScanner
name|scanner
init|=
literal|null
decl_stmt|;
specifier|private
name|Scan
name|scan
init|=
literal|null
decl_stmt|;
specifier|private
name|HTable
name|htable
init|=
literal|null
decl_stmt|;
specifier|private
name|byte
index|[]
name|lastRow
init|=
literal|null
decl_stmt|;
specifier|private
name|ImmutableBytesWritable
name|key
init|=
literal|null
decl_stmt|;
specifier|private
name|Result
name|value
init|=
literal|null
decl_stmt|;
comment|/**      * Restart from survivable exceptions by creating a new scanner.      *      * @param firstRow  The first row to start at.      * @throws IOException When restarting fails.      */
specifier|public
name|void
name|restart
parameter_list|(
name|byte
index|[]
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|newScan
init|=
operator|new
name|Scan
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|newScan
operator|.
name|setStartRow
argument_list|(
name|firstRow
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanner
operator|=
name|this
operator|.
name|htable
operator|.
name|getScanner
argument_list|(
name|newScan
argument_list|)
expr_stmt|;
block|}
comment|/**      * Build the scanner. Not done in constructor to allow for extension.      *      * @throws IOException When restarting the scan fails.       */
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|restart
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the HBase table.      *       * @param htable  The {@link HTable} to scan.      */
specifier|public
name|void
name|setHTable
parameter_list|(
name|HTable
name|htable
parameter_list|)
block|{
name|this
operator|.
name|htable
operator|=
name|htable
expr_stmt|;
block|}
comment|/**      * Sets the scan defining the actual details like columns etc.      *        * @param scan  The scan to set.      */
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
comment|/**      * Closes the split.      *       * @see org.apache.hadoop.mapreduce.RecordReader#close()      */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the current key.      *        * @return The current key.      * @throws IOException      * @throws InterruptedException When the job is aborted.      * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentKey()      */
annotation|@
name|Override
specifier|public
name|ImmutableBytesWritable
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|key
return|;
block|}
comment|/**      * Returns the current value.      *       * @return The current value.      * @throws IOException When the value is faulty.      * @throws InterruptedException When the job is aborted.      * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentValue()      */
annotation|@
name|Override
specifier|public
name|Result
name|getCurrentValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|value
return|;
block|}
comment|/**      * Initializes the reader.      *       * @param inputsplit  The split to work with.      * @param context  The current task context.      * @throws IOException When setting up the reader fails.      * @throws InterruptedException When the job is aborted.      * @see org.apache.hadoop.mapreduce.RecordReader#initialize(      *   org.apache.hadoop.mapreduce.InputSplit,       *   org.apache.hadoop.mapreduce.TaskAttemptContext)      */
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|inputsplit
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{     }
comment|/**      * Positions the record reader to the next record.      *        * @return<code>true</code> if there was another record.      * @throws IOException When reading the record failed.      * @throws InterruptedException When the job was aborted.      * @see org.apache.hadoop.mapreduce.RecordReader#nextKeyValue()      */
annotation|@
name|Override
specifier|public
name|boolean
name|nextKeyValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
name|key
operator|=
operator|new
name|ImmutableBytesWritable
argument_list|()
expr_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
name|value
operator|=
operator|new
name|Result
argument_list|()
expr_stmt|;
try|try
block|{
name|value
operator|=
name|this
operator|.
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"recovered from "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|restart
argument_list|(
name|lastRow
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
comment|// skip presumed already mapped row
name|value
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|!=
literal|null
operator|&&
name|value
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|key
operator|.
name|set
argument_list|(
name|value
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|lastRow
operator|=
name|key
operator|.
name|get
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**      * The current progress of the record reader through its data.      *       * @return A number between 0.0 and 1.0, the fraction of the data read.      * @see org.apache.hadoop.mapreduce.RecordReader#getProgress()      */
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
comment|// Depends on the total number of tuples
return|return
literal|0
return|;
block|}
block|}
comment|/**    * Builds a TableRecordReader. If no TableRecordReader was provided, uses    * the default.    *     * @param split  The split to work with.    * @param context  The current context.    * @return The newly created record reader.    * @throws IOException When creating the reader fails.    * @see org.apache.hadoop.mapreduce.InputFormat#createRecordReader(    *   org.apache.hadoop.mapreduce.InputSplit,     *   org.apache.hadoop.mapreduce.TaskAttemptContext)    */
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
name|trr
operator|.
name|init
argument_list|()
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
name|byte
index|[]
index|[]
name|startKeys
init|=
name|table
operator|.
name|getStartKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|startKeys
operator|==
literal|null
operator|||
name|startKeys
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
if|if
condition|(
operator|!
name|scan
operator|.
name|hasFamilies
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expecting at least one column."
argument_list|)
throw|;
block|}
name|int
name|realNumSplits
init|=
name|startKeys
operator|.
name|length
decl_stmt|;
name|InputSplit
index|[]
name|splits
init|=
operator|new
name|InputSplit
index|[
name|realNumSplits
index|]
decl_stmt|;
name|int
name|middle
init|=
name|startKeys
operator|.
name|length
operator|/
name|realNumSplits
decl_stmt|;
name|int
name|startPos
init|=
literal|0
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
name|realNumSplits
condition|;
name|i
operator|++
control|)
block|{
name|int
name|lastPos
init|=
name|startPos
operator|+
name|middle
decl_stmt|;
name|lastPos
operator|=
name|startKeys
operator|.
name|length
operator|%
name|realNumSplits
operator|>
name|i
condition|?
name|lastPos
operator|+
literal|1
else|:
name|lastPos
expr_stmt|;
name|String
name|regionLocation
init|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|startKeys
index|[
name|startPos
index|]
argument_list|)
operator|.
name|getServerAddress
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|splits
index|[
name|i
index|]
operator|=
operator|new
name|TableSplit
argument_list|(
name|this
operator|.
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|startKeys
index|[
name|startPos
index|]
argument_list|,
operator|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|<
name|realNumSplits
operator|)
condition|?
name|startKeys
index|[
name|lastPos
index|]
else|:
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|regionLocation
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"split: "
operator|+
name|i
operator|+
literal|"->"
operator|+
name|splits
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|startPos
operator|=
name|lastPos
expr_stmt|;
block|}
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|splits
argument_list|)
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
comment|/**    * Gets the scan defining the actual details like columns etc.    *      * @return The internal scan instance.    */
specifier|public
name|Scan
name|getScan
parameter_list|()
block|{
if|if
condition|(
name|scan
operator|==
literal|null
condition|)
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
comment|/**    * Sets the scan defining the actual details like columns etc.    *      * @param scan  The scan to set.    */
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
comment|/**    * Allows subclasses to set the {@link TableRecordReader}.    *    * @param tableRecordReader A different {@link TableRecordReader}     *   implementation.    */
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

