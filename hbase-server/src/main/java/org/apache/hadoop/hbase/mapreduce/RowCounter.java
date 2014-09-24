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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|lang
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
name|FirstKeyValueMatchingQualifiersFilter
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
name|GenericOptionsParser
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
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|RowCounter
extends|extends
name|Configured
implements|implements
name|Tool
block|{
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
comment|/**      * Maps the data.      *      * @param row  The current table row key.      * @param values  The columns.      * @param context  The current context.      * @throws IOException When something is broken with the data.      * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN,      *   org.apache.hadoop.mapreduce.Mapper.Context)      */
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
name|String
name|startKey
init|=
literal|null
decl_stmt|;
name|String
name|endKey
init|=
literal|null
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
name|String
index|[]
name|startEnd
init|=
name|args
index|[
name|i
index|]
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
return|return
literal|null
return|;
block|}
name|startKey
operator|=
name|startEnd
index|[
literal|0
index|]
expr_stmt|;
name|endKey
operator|=
name|startEnd
index|[
literal|1
index|]
expr_stmt|;
block|}
else|else
block|{
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
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|qualifiers
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|startKey
operator|!=
literal|null
operator|&&
operator|!
name|startKey
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|endKey
operator|!=
literal|null
operator|&&
operator|!
name|endKey
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|scan
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|endKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|// specified column may or may not be part of first key value for the row.
comment|// Hence do not use FirstKeyOnlyFilter if scan has columns, instead use
comment|// FirstKeyValueMatchingQualifiersFilter.
if|if
condition|(
name|qualifiers
operator|.
name|size
argument_list|()
operator|==
literal|0
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
else|else
block|{
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|FirstKeyValueMatchingQualifiersFilter
argument_list|(
name|qualifiers
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|/*    * Prints usage without error message    */
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
literal|"Usage: RowCounter [options]<tablename> "
operator|+
literal|"[--range=[startKey],[endKey]] [<column1><column2>...]"
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
name|String
index|[]
name|otherArgs
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|args
argument_list|)
operator|.
name|getRemainingArgs
argument_list|()
decl_stmt|;
if|if
condition|(
name|otherArgs
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
name|otherArgs
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

