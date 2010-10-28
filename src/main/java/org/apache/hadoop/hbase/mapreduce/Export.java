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
name|SequenceFileOutputFormat
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

begin_comment
comment|/**  * Export an HBase table.  * Writes content to sequence files up in HDFS.  Use {@link Import} to read it  * back in again.  */
end_comment

begin_class
specifier|public
class|class
name|Export
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
name|Export
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"export"
decl_stmt|;
comment|/**    * Mapper.    */
specifier|static
class|class
name|Exporter
extends|extends
name|TableMapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
comment|/**      * @param row  The current table row key.      * @param value  The columns.      * @param context  The current context.      * @throws IOException When something is broken with the data.      * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN,      *   org.apache.hadoop.mapreduce.Mapper.Context)      */
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
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|context
operator|.
name|write
argument_list|(
name|row
argument_list|,
name|value
argument_list|)
expr_stmt|;
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
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
name|NAME
operator|+
literal|"_"
operator|+
name|tableName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJobName
argument_list|(
name|NAME
operator|+
literal|"_"
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|Exporter
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// TODO: Allow passing filter and subset of rows/columns.
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Optional arguments.
name|int
name|versions
init|=
name|args
operator|.
name|length
operator|>
literal|2
condition|?
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|2
index|]
argument_list|)
else|:
literal|1
decl_stmt|;
name|s
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|args
operator|.
name|length
operator|>
literal|3
condition|?
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
literal|3
index|]
argument_list|)
else|:
literal|0L
decl_stmt|;
name|long
name|endTime
init|=
name|args
operator|.
name|length
operator|>
literal|4
condition|?
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
literal|4
index|]
argument_list|)
else|:
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|s
operator|.
name|setTimeRange
argument_list|(
name|startTime
argument_list|,
name|endTime
argument_list|)
expr_stmt|;
name|s
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_COLUMN_FAMILY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|s
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_COLUMN_FAMILY
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"verisons="
operator|+
name|versions
operator|+
literal|", starttime="
operator|+
name|startTime
operator|+
literal|", endtime="
operator|+
name|endTime
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableMapperJob
argument_list|(
name|tableName
argument_list|,
name|s
argument_list|,
name|Exporter
operator|.
name|class
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|job
argument_list|)
expr_stmt|;
comment|// No reducers.  Just write straight to output files.
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|Result
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
return|return
name|job
return|;
block|}
comment|/*    * @param errorMsg Error message.  Can be null.    */
specifier|private
specifier|static
name|void
name|usage
parameter_list|(
specifier|final
name|String
name|errorMsg
parameter_list|)
block|{
if|if
condition|(
name|errorMsg
operator|!=
literal|null
operator|&&
name|errorMsg
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR: "
operator|+
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: Export [-D<property=value>]*<tablename><outputdir> [<versions> "
operator|+
literal|"[<starttime> [<endtime>]]]\n"
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
literal|"  For example: "
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D mapred.output.compress=true"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D mapred.output.compression.codec=org.apache.hadoop.io.compress.GzipCodec"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D mapred.output.compression.type=BLOCK"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  Additionally, the following SCAN properties can be specified"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  to control/limit what is exported.."
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
literal|"=<familyName>"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Main entry point.    *    * @param args  The command line parameters.    * @throws Exception When running the job fails.    */
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
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|String
index|[]
name|otherArgs
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|conf
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
literal|2
condition|)
block|{
name|usage
argument_list|(
literal|"Wrong number of arguments: "
operator|+
name|otherArgs
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|Job
name|job
init|=
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|otherArgs
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
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
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

