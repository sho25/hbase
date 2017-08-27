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
name|conf
operator|.
name|Configured
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
name|HBaseConfiguration
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
name|filter
operator|.
name|CompareFilter
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|PrefixFilter
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
name|filter
operator|.
name|RegexStringComparator
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
name|filter
operator|.
name|RowFilter
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
name|io
operator|.
name|IntWritable
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
name|mapreduce
operator|.
name|Job
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
name|Reducer
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
name|lib
operator|.
name|output
operator|.
name|FileOutputFormat
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
name|lib
operator|.
name|output
operator|.
name|TextOutputFormat
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
name|Tool
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * A job with a a map and reduce phase to count cells in a table.  * The counter lists the following stats for a given table:  *<pre>  * 1. Total number of rows in the table  * 2. Total number of CFs across all rows  * 3. Total qualifiers across all rows  * 4. Total occurrence of each CF  * 5. Total occurrence  of each qualifier  * 6. Total number of versions of each qualifier.  *</pre>  *  * The cellcounter can take optional parameters to use a user  * supplied row/family/qualifier string to use in the report and  * second a regex based or prefix based row filter to restrict the  * count operation to a limited subset of rows from the table or a  * start time and/or end time to limit the count to a time range.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|CellCounter
extends|extends
name|Configured
implements|implements
name|Tool
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
name|CellCounter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Name of this 'program'.    */
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"CellCounter"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|JOB_NAME_CONF_KEY
init|=
literal|"mapreduce.job.name"
decl_stmt|;
comment|/**    * Mapper that runs the count.    */
specifier|static
class|class
name|CellCounterMapper
extends|extends
name|TableMapper
argument_list|<
name|Text
argument_list|,
name|IntWritable
argument_list|>
block|{
comment|/**      * Counter enumeration to count the actual rows.      */
specifier|public
specifier|static
enum|enum
name|Counters
block|{
name|ROWS
block|,
name|CELLS
block|}
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|String
name|separator
decl_stmt|;
comment|// state of current row, family, column needs to persist across map() invocations
comment|// in order to properly handle scanner batching, where a single qualifier may have too
comment|// many versions for a single map() call
specifier|private
name|byte
index|[]
name|lastRow
decl_stmt|;
specifier|private
name|String
name|currentRowKey
decl_stmt|;
name|byte
index|[]
name|currentFamily
init|=
literal|null
decl_stmt|;
name|String
name|currentFamilyName
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|currentQualifier
init|=
literal|null
decl_stmt|;
comment|// family + qualifier
name|String
name|currentQualifierName
init|=
literal|null
decl_stmt|;
comment|// rowkey + family + qualifier
name|String
name|currentRowQualifierName
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setup
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|conf
operator|=
name|context
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|separator
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"ReportSeparator"
argument_list|,
literal|":"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Maps the data.      *      * @param row     The current table row key.      * @param values  The columns.      * @param context The current context.      * @throws IOException When something is broken with the data.      * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN,      *      org.apache.hadoop.mapreduce.Mapper.Context)      */
annotation|@
name|Override
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"NP_NULL_ON_SOME_PATH"
argument_list|,
name|justification
operator|=
literal|"Findbugs is blind to the Precondition null check"
argument_list|)
specifier|public
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|row
parameter_list|,
name|Result
name|values
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|values
operator|!=
literal|null
argument_list|,
literal|"values passed to the map is null"
argument_list|)
expr_stmt|;
try|try
block|{
name|byte
index|[]
name|currentRow
init|=
name|values
operator|.
name|getRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastRow
operator|==
literal|null
operator|||
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|lastRow
argument_list|,
name|currentRow
argument_list|)
condition|)
block|{
name|lastRow
operator|=
name|currentRow
expr_stmt|;
name|currentRowKey
operator|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|currentRow
argument_list|)
expr_stmt|;
name|currentFamily
operator|=
literal|null
expr_stmt|;
name|currentQualifier
operator|=
literal|null
expr_stmt|;
name|context
operator|.
name|getCounter
argument_list|(
name|Counters
operator|.
name|ROWS
argument_list|)
operator|.
name|increment
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
operator|new
name|Text
argument_list|(
literal|"Total ROWS"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|values
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|int
name|cellCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|value
range|:
name|values
operator|.
name|listCells
argument_list|()
control|)
block|{
name|cellCount
operator|++
expr_stmt|;
if|if
condition|(
name|currentFamily
operator|==
literal|null
operator|||
operator|!
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|value
argument_list|,
name|currentFamily
argument_list|)
condition|)
block|{
name|currentFamily
operator|=
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|currentFamilyName
operator|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|currentFamily
argument_list|)
expr_stmt|;
name|currentQualifier
operator|=
literal|null
expr_stmt|;
name|context
operator|.
name|getCounter
argument_list|(
literal|"CF"
argument_list|,
name|currentFamilyName
argument_list|)
operator|.
name|increment
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
literal|1
operator|==
name|context
operator|.
name|getCounter
argument_list|(
literal|"CF"
argument_list|,
name|currentFamilyName
argument_list|)
operator|.
name|getValue
argument_list|()
condition|)
block|{
name|context
operator|.
name|write
argument_list|(
operator|new
name|Text
argument_list|(
literal|"Total Families Across all Rows"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
operator|new
name|Text
argument_list|(
name|currentFamily
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|currentQualifier
operator|==
literal|null
operator|||
operator|!
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|value
argument_list|,
name|currentQualifier
argument_list|)
condition|)
block|{
name|currentQualifier
operator|=
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|currentQualifierName
operator|=
name|currentFamilyName
operator|+
name|separator
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|currentQualifier
argument_list|)
expr_stmt|;
name|currentRowQualifierName
operator|=
name|currentRowKey
operator|+
name|separator
operator|+
name|currentQualifierName
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
operator|new
name|Text
argument_list|(
literal|"Total Qualifiers across all Rows"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
operator|new
name|Text
argument_list|(
name|currentQualifierName
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Increment versions
name|context
operator|.
name|write
argument_list|(
operator|new
name|Text
argument_list|(
name|currentRowQualifierName
operator|+
literal|"_Versions"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|getCounter
argument_list|(
name|Counters
operator|.
name|CELLS
argument_list|)
operator|.
name|increment
argument_list|(
name|cellCount
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|static
class|class
name|IntSumReducer
parameter_list|<
name|Key
parameter_list|>
extends|extends
name|Reducer
argument_list|<
name|Key
argument_list|,
name|IntWritable
argument_list|,
name|Key
argument_list|,
name|IntWritable
argument_list|>
block|{
specifier|private
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|public
name|void
name|reduce
parameter_list|(
name|Key
name|key
parameter_list|,
name|Iterable
argument_list|<
name|IntWritable
argument_list|>
name|values
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|int
name|sum
init|=
literal|0
decl_stmt|;
for|for
control|(
name|IntWritable
name|val
range|:
name|values
control|)
block|{
name|sum
operator|+=
name|val
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|sum
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Sets up the actual job.    *    * @param conf The current configuration.    * @param args The command line parameters.    * @return The newly created job.    * @throws IOException When setting up the job fails.    */
specifier|public
specifier|static
name|Job
name|createSubmittableJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|tableName
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|Path
name|outputDir
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|String
name|reportSeparatorString
init|=
operator|(
name|args
operator|.
name|length
operator|>
literal|2
operator|)
condition|?
name|args
index|[
literal|2
index|]
else|:
literal|":"
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"ReportSeparator"
argument_list|,
name|reportSeparatorString
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
name|Job
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|JOB_NAME_CONF_KEY
argument_list|,
name|NAME
operator|+
literal|"_"
operator|+
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|CellCounter
operator|.
name|class
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
name|getConfiguredScanForJob
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableMapperJob
argument_list|(
name|tableName
argument_list|,
name|scan
argument_list|,
name|CellCounterMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|Result
operator|.
name|class
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|IntWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|TextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|IntWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|outputDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|IntSumReducer
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|job
return|;
block|}
specifier|private
specifier|static
name|Scan
name|getConfiguredScanForJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
comment|// create scan with any properties set from TableInputFormat
name|Scan
name|s
init|=
name|TableInputFormat
operator|.
name|createScanFromConfiguration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Set Scan Versions
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_MAXVERSIONS
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// default to all versions unless explicitly set
name|s
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Set RowFilter or Prefix Filter if applicable.
name|Filter
name|rowFilter
init|=
name|getRowFilter
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowFilter
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting Row Filter for counter."
argument_list|)
expr_stmt|;
name|s
operator|.
name|setFilter
argument_list|(
name|rowFilter
argument_list|)
expr_stmt|;
block|}
comment|// Set TimeRange if defined
name|long
name|timeRange
index|[]
init|=
name|getTimeRange
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|timeRange
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting TimeRange for counter."
argument_list|)
expr_stmt|;
name|s
operator|.
name|setTimeRange
argument_list|(
name|timeRange
index|[
literal|0
index|]
argument_list|,
name|timeRange
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|s
return|;
block|}
specifier|private
specifier|static
name|Filter
name|getRowFilter
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|Filter
name|rowFilter
init|=
literal|null
decl_stmt|;
name|String
name|filterCriteria
init|=
operator|(
name|args
operator|.
name|length
operator|>
literal|3
operator|)
condition|?
name|args
index|[
literal|3
index|]
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|filterCriteria
operator|==
literal|null
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|filterCriteria
operator|.
name|startsWith
argument_list|(
literal|"^"
argument_list|)
condition|)
block|{
name|String
name|regexPattern
init|=
name|filterCriteria
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|filterCriteria
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|rowFilter
operator|=
operator|new
name|RowFilter
argument_list|(
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|RegexStringComparator
argument_list|(
name|regexPattern
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rowFilter
operator|=
operator|new
name|PrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|filterCriteria
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|rowFilter
return|;
block|}
specifier|private
specifier|static
name|long
index|[]
name|getTimeRange
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|startTimeArgKey
init|=
literal|"--starttime="
decl_stmt|;
specifier|final
name|String
name|endTimeArgKey
init|=
literal|"--endtime="
decl_stmt|;
name|long
name|startTime
init|=
literal|0L
decl_stmt|;
name|long
name|endTime
init|=
literal|0L
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"i:"
operator|+
name|i
operator|+
literal|"arg[i]"
operator|+
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|startsWith
argument_list|(
name|startTimeArgKey
argument_list|)
condition|)
block|{
name|startTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
name|i
index|]
operator|.
name|substring
argument_list|(
name|startTimeArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|startsWith
argument_list|(
name|endTimeArgKey
argument_list|)
condition|)
block|{
name|endTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
name|i
index|]
operator|.
name|substring
argument_list|(
name|endTimeArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|startTime
operator|==
literal|0
operator|&&
name|endTime
operator|==
literal|0
condition|)
return|return
literal|null
return|;
name|endTime
operator|=
name|endTime
operator|==
literal|0
condition|?
name|HConstants
operator|.
name|LATEST_TIMESTAMP
else|:
name|endTime
expr_stmt|;
return|return
operator|new
name|long
index|[]
block|{
name|startTime
block|,
name|endTime
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR: Wrong number of parameters: "
operator|+
name|args
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: CellCounter "
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"<tablename><outputDir><reportSeparator> [^[regex pattern] or "
operator|+
literal|"[Prefix] for row filter]] --starttime=[starttime] --endtime=[endtime]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  Note: -D properties will be applied to the conf used. "
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  Additionally, all of the SCAN properties from TableInputFormat"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  can be specified to get fine grained control on what is counted.."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_ROW_START
operator|+
literal|"=<rowkey>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_ROW_STOP
operator|+
literal|"=<rowkey>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_COLUMNS
operator|+
literal|"=\"<col1><col2>...\""
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_COLUMN_FAMILY
operator|+
literal|"=<family1>,<family2>, ..."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_TIMESTAMP
operator|+
literal|"=<timestamp>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_TIMERANGE_START
operator|+
literal|"=<timestamp>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_TIMERANGE_END
operator|+
literal|"=<timestamp>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_MAXVERSIONS
operator|+
literal|"=<count>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_CACHEDROWS
operator|+
literal|"=<count>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_BATCHSIZE
operator|+
literal|"=<count>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"<reportSeparator> parameter can be used to override the default report separator "
operator|+
literal|"string : used to separate the rowId/column family name and qualifier name."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" [^[regex pattern] or [Prefix] parameter can be used to limit the cell counter count "
operator|+
literal|"operation to a limited subset of rows from the table based on regex or prefix pattern."
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
name|Job
name|job
init|=
name|createSubmittableJob
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
return|return
operator|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
condition|?
literal|0
else|:
literal|1
operator|)
return|;
block|}
comment|/**    * Main entry point.    * @param args The command line parameters.    * @throws Exception When running the job fails.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|errCode
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
operator|new
name|CellCounter
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|errCode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
