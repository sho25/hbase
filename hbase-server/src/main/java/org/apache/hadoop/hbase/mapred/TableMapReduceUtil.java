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
name|catalog
operator|.
name|MetaReader
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
name|MutationSerialization
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
name|ResultSerialization
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
name|security
operator|.
name|User
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
name|mapred
operator|.
name|OutputFormat
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
name|TextInputFormat
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
name|mapred
operator|.
name|jobcontrol
operator|.
name|Job
import|;
end_import

begin_comment
comment|/**  * Utility for {@link TableMap} and {@link TableReduce}  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
class|class
name|TableMapReduceUtil
block|{
comment|/**    * Use this before submitting a TableMap job. It will    * appropriately set up the JobConf.    *    * @param table  The table name to read from.    * @param columns  The columns to scan.    * @param mapper  The mapper class to use.    * @param outputKeyClass  The class of the output key.    * @param outputValueClass  The class of the output value.    * @param job  The current job configuration to adjust.    */
specifier|public
specifier|static
name|void
name|initTableMapJob
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|columns
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableMap
argument_list|>
name|mapper
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|outputKeyClass
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|outputValueClass
parameter_list|,
name|JobConf
name|job
parameter_list|)
block|{
name|initTableMapJob
argument_list|(
name|table
argument_list|,
name|columns
argument_list|,
name|mapper
argument_list|,
name|outputKeyClass
argument_list|,
name|outputValueClass
argument_list|,
name|job
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Use this before submitting a TableMap job. It will    * appropriately set up the JobConf.    *    * @param table  The table name to read from.    * @param columns  The columns to scan.    * @param mapper  The mapper class to use.    * @param outputKeyClass  The class of the output key.    * @param outputValueClass  The class of the output value.    * @param job  The current job configuration to adjust.    * @param addDependencyJars upload HBase jars and jars for any of the configured    *           job classes via the distributed cache (tmpjars).    */
specifier|public
specifier|static
name|void
name|initTableMapJob
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|columns
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableMap
argument_list|>
name|mapper
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|outputKeyClass
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|outputValueClass
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|boolean
name|addDependencyJars
parameter_list|)
block|{
name|job
operator|.
name|setInputFormat
argument_list|(
name|TableInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|outputValueClass
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|outputKeyClass
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
name|job
operator|.
name|setStrings
argument_list|(
literal|"io.serializations"
argument_list|,
name|job
operator|.
name|get
argument_list|(
literal|"io.serializations"
argument_list|)
argument_list|,
name|MutationSerialization
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|ResultSerialization
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|addInputPaths
argument_list|(
name|job
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|TableInputFormat
operator|.
name|COLUMN_LIST
argument_list|,
name|columns
argument_list|)
expr_stmt|;
if|if
condition|(
name|addDependencyJars
condition|)
block|{
try|try
block|{
name|addDependencyJars
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
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
try|try
block|{
name|initCredentials
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// just spit out the stack trace?  really?
name|ioe
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Use this before submitting a TableReduce job. It will    * appropriately set up the JobConf.    *    * @param table  The output table.    * @param reducer  The reducer class to use.    * @param job  The current job configuration to adjust.    * @throws IOException When determining the region count fails.    */
specifier|public
specifier|static
name|void
name|initTableReduceJob
parameter_list|(
name|String
name|table
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableReduce
argument_list|>
name|reducer
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|initTableReduceJob
argument_list|(
name|table
argument_list|,
name|reducer
argument_list|,
name|job
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Use this before submitting a TableReduce job. It will    * appropriately set up the JobConf.    *    * @param table  The output table.    * @param reducer  The reducer class to use.    * @param job  The current job configuration to adjust.    * @param partitioner  Partitioner to use. Pass<code>null</code> to use    * default partitioner.    * @throws IOException When determining the region count fails.    */
specifier|public
specifier|static
name|void
name|initTableReduceJob
parameter_list|(
name|String
name|table
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableReduce
argument_list|>
name|reducer
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Class
name|partitioner
parameter_list|)
throws|throws
name|IOException
block|{
name|initTableReduceJob
argument_list|(
name|table
argument_list|,
name|reducer
argument_list|,
name|job
argument_list|,
name|partitioner
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Use this before submitting a TableReduce job. It will    * appropriately set up the JobConf.    *    * @param table  The output table.    * @param reducer  The reducer class to use.    * @param job  The current job configuration to adjust.    * @param partitioner  Partitioner to use. Pass<code>null</code> to use    * default partitioner.    * @param addDependencyJars upload HBase jars and jars for any of the configured    *           job classes via the distributed cache (tmpjars).    * @throws IOException When determining the region count fails.    */
specifier|public
specifier|static
name|void
name|initTableReduceJob
parameter_list|(
name|String
name|table
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableReduce
argument_list|>
name|reducer
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Class
name|partitioner
parameter_list|,
name|boolean
name|addDependencyJars
parameter_list|)
throws|throws
name|IOException
block|{
name|job
operator|.
name|setOutputFormat
argument_list|(
name|TableOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|reducer
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|TableOutputFormat
operator|.
name|OUTPUT_TABLE
argument_list|,
name|table
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
name|Put
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setStrings
argument_list|(
literal|"io.serializations"
argument_list|,
name|job
operator|.
name|get
argument_list|(
literal|"io.serializations"
argument_list|)
argument_list|,
name|MutationSerialization
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|ResultSerialization
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|partitioner
operator|==
name|HRegionPartitioner
operator|.
name|class
condition|)
block|{
name|job
operator|.
name|setPartitionerClass
argument_list|(
name|HRegionPartitioner
operator|.
name|class
argument_list|)
expr_stmt|;
name|int
name|regions
init|=
name|MetaReader
operator|.
name|getRegionCount
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|job
argument_list|)
argument_list|,
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|job
operator|.
name|getNumReduceTasks
argument_list|()
operator|>
name|regions
condition|)
block|{
name|job
operator|.
name|setNumReduceTasks
argument_list|(
name|regions
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|partitioner
operator|!=
literal|null
condition|)
block|{
name|job
operator|.
name|setPartitionerClass
argument_list|(
name|partitioner
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|addDependencyJars
condition|)
block|{
name|addDependencyJars
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
name|initCredentials
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|initCredentials
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|User
operator|.
name|isHBaseSecurityEnabled
argument_list|(
name|job
argument_list|)
condition|)
block|{
try|try
block|{
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|obtainAuthTokenForJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|ie
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Ensures that the given number of reduce tasks for the given job    * configuration does not exceed the number of regions for the given table.    *    * @param table  The table to get the region count for.    * @param job  The current job configuration to adjust.    * @throws IOException When retrieving the table details fails.    */
specifier|public
specifier|static
name|void
name|limitNumReduceTasks
parameter_list|(
name|String
name|table
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|regions
init|=
name|MetaReader
operator|.
name|getRegionCount
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|job
argument_list|)
argument_list|,
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|job
operator|.
name|getNumReduceTasks
argument_list|()
operator|>
name|regions
condition|)
name|job
operator|.
name|setNumReduceTasks
argument_list|(
name|regions
argument_list|)
expr_stmt|;
block|}
comment|/**    * Ensures that the given number of map tasks for the given job    * configuration does not exceed the number of regions for the given table.    *    * @param table  The table to get the region count for.    * @param job  The current job configuration to adjust.    * @throws IOException When retrieving the table details fails.    */
specifier|public
specifier|static
name|void
name|limitNumMapTasks
parameter_list|(
name|String
name|table
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|regions
init|=
name|MetaReader
operator|.
name|getRegionCount
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|job
argument_list|)
argument_list|,
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|job
operator|.
name|getNumMapTasks
argument_list|()
operator|>
name|regions
condition|)
name|job
operator|.
name|setNumMapTasks
argument_list|(
name|regions
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the number of reduce tasks for the given job configuration to the    * number of regions the given table has.    *    * @param table  The table to get the region count for.    * @param job  The current job configuration to adjust.    * @throws IOException When retrieving the table details fails.    */
specifier|public
specifier|static
name|void
name|setNumReduceTasks
parameter_list|(
name|String
name|table
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|job
operator|.
name|setNumReduceTasks
argument_list|(
name|MetaReader
operator|.
name|getRegionCount
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|job
argument_list|)
argument_list|,
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the number of map tasks for the given job configuration to the    * number of regions the given table has.    *    * @param table  The table to get the region count for.    * @param job  The current job configuration to adjust.    * @throws IOException When retrieving the table details fails.    */
specifier|public
specifier|static
name|void
name|setNumMapTasks
parameter_list|(
name|String
name|table
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|job
operator|.
name|setNumMapTasks
argument_list|(
name|MetaReader
operator|.
name|getRegionCount
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|job
argument_list|)
argument_list|,
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the number of rows to return and cache with each scanner iteration.    * Higher caching values will enable faster mapreduce jobs at the expense of    * requiring more heap to contain the cached rows.    *    * @param job The current job configuration to adjust.    * @param batchSize The number of rows to return in batch with each scanner    * iteration.    */
specifier|public
specifier|static
name|void
name|setScannerCaching
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|batchSize
parameter_list|)
block|{
name|job
operator|.
name|setInt
argument_list|(
literal|"hbase.client.scanner.caching"
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil#addDependencyJars(Job)    */
specifier|public
specifier|static
name|void
name|addDependencyJars
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
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
operator|.
name|addDependencyJars
argument_list|(
name|job
argument_list|,
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
operator|.
name|class
argument_list|,
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
operator|.
name|class
argument_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
operator|.
name|class
argument_list|,
name|job
operator|.
name|getMapOutputKeyClass
argument_list|()
argument_list|,
name|job
operator|.
name|getMapOutputValueClass
argument_list|()
argument_list|,
name|job
operator|.
name|getOutputKeyClass
argument_list|()
argument_list|,
name|job
operator|.
name|getOutputValueClass
argument_list|()
argument_list|,
name|job
operator|.
name|getPartitionerClass
argument_list|()
argument_list|,
name|job
operator|.
name|getClass
argument_list|(
literal|"mapred.input.format.class"
argument_list|,
name|TextInputFormat
operator|.
name|class
argument_list|,
name|InputFormat
operator|.
name|class
argument_list|)
argument_list|,
name|job
operator|.
name|getClass
argument_list|(
literal|"mapred.output.format.class"
argument_list|,
name|TextOutputFormat
operator|.
name|class
argument_list|,
name|OutputFormat
operator|.
name|class
argument_list|)
argument_list|,
name|job
operator|.
name|getCombinerClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

