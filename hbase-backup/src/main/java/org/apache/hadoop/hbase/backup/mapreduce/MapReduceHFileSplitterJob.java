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
name|backup
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
name|TableName
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
name|Connection
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
name|ConnectionFactory
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
name|RegionLocator
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
name|mapreduce
operator|.
name|CellSortReducer
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
name|mapreduce
operator|.
name|HFileInputFormat
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
name|mapreduce
operator|.
name|HFileOutputFormat2
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
name|mapreduce
operator|.
name|TableMapReduceUtil
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
name|EnvironmentEdgeManager
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
name|MapReduceExtendedCell
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
name|NullWritable
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
name|Mapper
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
name|input
operator|.
name|FileInputFormat
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

begin_comment
comment|/**  * A tool to split HFiles into new region boundaries as a MapReduce job. The tool generates HFiles  * for later bulk importing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MapReduceHFileSplitterJob
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
name|MapReduceHFileSplitterJob
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"HFileSplitterJob"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|BULK_OUTPUT_CONF_KEY
init|=
literal|"hfile.bulk.output"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|TABLES_KEY
init|=
literal|"hfile.input.tables"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|TABLE_MAP_KEY
init|=
literal|"hfile.input.tablesmap"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|JOB_NAME_CONF_KEY
init|=
literal|"mapreduce.job.name"
decl_stmt|;
specifier|public
name|MapReduceHFileSplitterJob
parameter_list|()
block|{   }
specifier|protected
name|MapReduceHFileSplitterJob
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
name|super
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
comment|/**    * A mapper that just writes out cells. This one can be used together with    * {@link CellSortReducer}    */
specifier|static
class|class
name|HFileCellMapper
extends|extends
name|Mapper
argument_list|<
name|NullWritable
argument_list|,
name|Cell
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|Cell
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|Cell
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|context
operator|.
name|write
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|,
operator|new
name|MapReduceExtendedCell
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// do nothing
block|}
block|}
comment|/**    * Sets up the actual job.    * @param args The command line parameters.    * @return The newly created job.    * @throws IOException When setting up the job fails.    */
specifier|public
name|Job
name|createSubmittableJob
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|String
name|inputDirs
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|String
name|tabName
init|=
name|args
index|[
literal|1
index|]
decl_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|TABLES_KEY
argument_list|,
name|tabName
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|FileInputFormat
operator|.
name|INPUT_DIR
argument_list|,
name|inputDirs
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
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|MapReduceHFileSplitterJob
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|HFileInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|String
name|hfileOutPath
init|=
name|conf
operator|.
name|get
argument_list|(
name|BULK_OUTPUT_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|hfileOutPath
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"add incremental job :"
operator|+
name|hfileOutPath
operator|+
literal|" from "
operator|+
name|inputDirs
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tabName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|HFileCellMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|CellSortReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|Path
name|outputDir
init|=
operator|new
name|Path
argument_list|(
name|hfileOutPath
argument_list|)
decl_stmt|;
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
name|setMapOutputValueClass
argument_list|(
name|MapReduceExtendedCell
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|;           Table table = conn.getTable(tableName)
empty_stmt|;
name|RegionLocator
name|regionLocator
init|=
name|conn
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|HFileOutputFormat2
operator|.
name|configureIncrementalLoad
argument_list|(
name|job
argument_list|,
name|table
operator|.
name|getDescriptor
argument_list|()
argument_list|,
name|regionLocator
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"success configuring load incremental job"
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
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
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No bulk output directory specified"
argument_list|)
throw|;
block|}
return|return
name|job
return|;
block|}
comment|/**    * Print usage    * @param errorMsg Error message. Can be null.    */
specifier|private
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
literal|"Usage: "
operator|+
name|NAME
operator|+
literal|" [options]<HFile inputdir(s)><table>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Read all HFile's for<table> and split them to<table> region boundaries."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"<table>  table to load.\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"To generate HFiles for a bulk data load, pass the option:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -D"
operator|+
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"=/path/for/output"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Other options:"
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
name|JOB_NAME_CONF_KEY
operator|+
literal|"=jobName - use the specified mapreduce job name for the HFile splitter"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For performance also consider the following options:\n"
operator|+
literal|"  -Dmapreduce.map.speculative=false\n"
operator|+
literal|"  -Dmapreduce.reduce.speculative=false"
argument_list|)
expr_stmt|;
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
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|MapReduceHFileSplitterJob
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
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
literal|2
condition|)
block|{
name|usage
argument_list|(
literal|"Wrong number of arguments: "
operator|+
name|args
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
name|args
argument_list|)
decl_stmt|;
name|int
name|result
init|=
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
decl_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

