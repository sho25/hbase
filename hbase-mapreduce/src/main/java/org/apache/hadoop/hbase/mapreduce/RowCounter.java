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
name|ArrayList
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
name|lang3
operator|.
name|StringUtils
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|FilterBase
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
name|FirstKeyOnlyFilter
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
name|MultiRowRangeFilter
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
name|mapreduce
operator|.
name|Counter
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
name|lib
operator|.
name|output
operator|.
name|NullOutputFormat
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

begin_comment
comment|/**  * A job with a just a map phase to count rows. Map outputs table rows IF the  * input row has columns that have content.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|RowCounter
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RowCounter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Name of this 'program'. */
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"rowcounter"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|JOB_NAME_CONF_KEY
init|=
literal|"mapreduce.job.name"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|EXPECTED_COUNT_KEY
init|=
name|RowCounter
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|".expected_count"
decl_stmt|;
comment|/**    * Mapper that runs the count.    */
specifier|static
class|class
name|RowCounterMapper
extends|extends
name|TableMapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
comment|/** Counter enumeration to count the actual rows. */
specifier|public
specifier|static
enum|enum
name|Counters
block|{
name|ROWS
block|}
comment|/**      * Maps the data.      *      * @param row  The current table row key.      * @param values  The columns.      * @param context  The current context.      * @throws IOException When something is broken with the data.      * @see org.apache.hadoop.mapreduce.Mapper#map(Object, Object, Context)      */
annotation|@
name|Override
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
comment|// Count every row containing data, whether it's in qualifiers or values
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
block|}
block|}
comment|/**    * Sets up the actual job.    *    * @param conf  The current configuration.    * @param args  The command line parameters.    * @return The newly created job.    * @throws IOException When setting up the job fails.    */
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
name|List
argument_list|<
name|MultiRowRangeFilter
operator|.
name|RowRange
argument_list|>
name|rowRangeList
init|=
literal|null
decl_stmt|;
name|long
name|startTime
init|=
literal|0
decl_stmt|;
name|long
name|endTime
init|=
literal|0
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|final
name|String
name|rangeSwitch
init|=
literal|"--range="
decl_stmt|;
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
specifier|final
name|String
name|expectedCountArg
init|=
literal|"--expected-count="
decl_stmt|;
comment|// First argument is table name, starting from second
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
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|startsWith
argument_list|(
name|rangeSwitch
argument_list|)
condition|)
block|{
try|try
block|{
name|rowRangeList
operator|=
name|parseRowRangeParameter
argument_list|(
name|args
index|[
name|i
index|]
argument_list|,
name|rangeSwitch
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
continue|continue;
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
continue|continue;
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
continue|continue;
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
name|expectedCountArg
argument_list|)
condition|)
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|EXPECTED_COUNT_KEY
argument_list|,
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
name|expectedCountArg
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// if no switch, assume column names
name|sb
operator|.
name|append
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|endTime
operator|<
name|startTime
condition|)
block|{
name|printUsage
argument_list|(
literal|"--endtime="
operator|+
name|endTime
operator|+
literal|" needs to be greater than --starttime="
operator|+
name|startTime
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
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
name|RowCounter
operator|.
name|class
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|setScanFilter
argument_list|(
name|scan
argument_list|,
name|rowRangeList
argument_list|)
expr_stmt|;
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|String
name|columnName
range|:
name|sb
operator|.
name|toString
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
control|)
block|{
name|String
name|family
init|=
name|StringUtils
operator|.
name|substringBefore
argument_list|(
name|columnName
argument_list|,
literal|":"
argument_list|)
decl_stmt|;
name|String
name|qualifier
init|=
name|StringUtils
operator|.
name|substringAfter
argument_list|(
name|columnName
argument_list|,
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|qualifier
argument_list|)
condition|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|scan
operator|.
name|setTimeRange
argument_list|(
name|startTime
argument_list|,
name|endTime
operator|==
literal|0
condition|?
name|HConstants
operator|.
name|LATEST_TIMESTAMP
else|:
name|endTime
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|NullOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableMapperJob
argument_list|(
name|tableName
argument_list|,
name|scan
argument_list|,
name|RowCounterMapper
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
literal|0
argument_list|)
expr_stmt|;
return|return
name|job
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|MultiRowRangeFilter
operator|.
name|RowRange
argument_list|>
name|parseRowRangeParameter
parameter_list|(
name|String
name|arg
parameter_list|,
name|String
name|rangeSwitch
parameter_list|)
block|{
specifier|final
name|String
index|[]
name|ranges
init|=
name|arg
operator|.
name|substring
argument_list|(
name|rangeSwitch
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|split
argument_list|(
literal|";"
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|MultiRowRangeFilter
operator|.
name|RowRange
argument_list|>
name|rangeList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|range
range|:
name|ranges
control|)
block|{
name|String
index|[]
name|startEnd
init|=
name|range
operator|.
name|split
argument_list|(
literal|","
argument_list|,
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|startEnd
operator|.
name|length
operator|!=
literal|2
operator|||
name|startEnd
index|[
literal|1
index|]
operator|.
name|contains
argument_list|(
literal|","
argument_list|)
condition|)
block|{
name|printUsage
argument_list|(
literal|"Please specify range in such format as \"--range=a,b\" "
operator|+
literal|"or, with only one boundary, \"--range=,b\" or \"--range=a,\""
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Wrong range specification: "
operator|+
name|range
argument_list|)
throw|;
block|}
name|String
name|startKey
init|=
name|startEnd
index|[
literal|0
index|]
decl_stmt|;
name|String
name|endKey
init|=
name|startEnd
index|[
literal|1
index|]
decl_stmt|;
name|rangeList
operator|.
name|add
argument_list|(
operator|new
name|MultiRowRangeFilter
operator|.
name|RowRange
argument_list|(
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|startKey
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|endKey
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|rangeList
return|;
block|}
comment|/**    * Sets filter {@link FilterBase} to the {@link Scan} instance.    * If provided rowRangeList contains more than one element,    * method sets filter which is instance of {@link MultiRowRangeFilter}.    * Otherwise, method sets filter which is instance of {@link FirstKeyOnlyFilter}.    * If rowRangeList contains exactly one element, startRow and stopRow are set to the scan.    * @param scan    * @param rowRangeList    */
specifier|private
specifier|static
name|void
name|setScanFilter
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|List
argument_list|<
name|MultiRowRangeFilter
operator|.
name|RowRange
argument_list|>
name|rowRangeList
parameter_list|)
block|{
specifier|final
name|int
name|size
init|=
name|rowRangeList
operator|==
literal|null
condition|?
literal|0
else|:
name|rowRangeList
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|<=
literal|1
condition|)
block|{
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|FirstKeyOnlyFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
name|MultiRowRangeFilter
operator|.
name|RowRange
name|range
init|=
name|rowRangeList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setStartRow
argument_list|(
name|range
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
comment|//inclusive
name|scan
operator|.
name|setStopRow
argument_list|(
name|range
operator|.
name|getStopRow
argument_list|()
argument_list|)
expr_stmt|;
comment|//exclusive
block|}
elseif|else
if|if
condition|(
name|size
operator|>
literal|1
condition|)
block|{
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|MultiRowRangeFilter
argument_list|(
name|rowRangeList
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * @param errorMessage Can attach a message when error occurs.    */
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|(
name|String
name|errorMessage
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR: "
operator|+
name|errorMessage
argument_list|)
expr_stmt|;
name|printUsage
argument_list|()
expr_stmt|;
block|}
comment|/**    * Prints usage without error message.    * Note that we don't document --expected-count, because it's intended for test.    */
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: hbase rowcounter [options]<tablename> "
operator|+
literal|"[--starttime=<start> --endtime=<end>] "
operator|+
literal|"[--range=[startKey],[endKey][;[startKey],[endKey]...]] [<column1><column2>...]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For performance consider the following options:\n"
operator|+
literal|"-Dhbase.client.scanner.caching=100\n"
operator|+
literal|"-Dmapreduce.map.speculative=false"
argument_list|)
expr_stmt|;
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
literal|1
condition|)
block|{
name|printUsage
argument_list|(
literal|"Wrong number of parameters: "
operator|+
name|args
operator|.
name|length
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
if|if
condition|(
name|job
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|boolean
name|success
init|=
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|long
name|expectedCount
init|=
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|EXPECTED_COUNT_KEY
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|success
operator|&&
name|expectedCount
operator|!=
operator|-
literal|1
condition|)
block|{
specifier|final
name|Counter
name|counter
init|=
name|job
operator|.
name|getCounters
argument_list|()
operator|.
name|findCounter
argument_list|(
name|RowCounterMapper
operator|.
name|Counters
operator|.
name|ROWS
argument_list|)
decl_stmt|;
name|success
operator|=
name|expectedCount
operator|==
name|counter
operator|.
name|getValue
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failing job because count of '"
operator|+
name|counter
operator|.
name|getValue
argument_list|()
operator|+
literal|"' does not match expected count of '"
operator|+
name|expectedCount
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|success
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
name|RowCounter
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

