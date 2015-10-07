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
name|Configurable
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
name|BufferedMutator
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
name|Delete
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
name|Mutation
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
name|zookeeper
operator|.
name|ZKUtil
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
name|OutputCommitter
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
name|mapreduce
operator|.
name|RecordWriter
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
comment|/**  * Convert Map/Reduce output and write it to an HBase table. The KEY is ignored  * while the output value<u>must</u> be either a {@link Put} or a  * {@link Delete} instance.  */
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
name|TableOutputFormat
parameter_list|<
name|KEY
parameter_list|>
extends|extends
name|OutputFormat
argument_list|<
name|KEY
argument_list|,
name|Mutation
argument_list|>
implements|implements
name|Configurable
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
name|TableOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Job parameter that specifies the output table. */
specifier|public
specifier|static
specifier|final
name|String
name|OUTPUT_TABLE
init|=
literal|"hbase.mapred.outputtable"
decl_stmt|;
comment|/**    * Optional job parameter to specify a peer cluster.    * Used specifying remote cluster when copying between hbase clusters (the    * source is picked up from<code>hbase-site.xml</code>).    * @see TableMapReduceUtil#initTableReducerJob(String, Class, org.apache.hadoop.mapreduce.Job, Class, String, String, String)    */
specifier|public
specifier|static
specifier|final
name|String
name|QUORUM_ADDRESS
init|=
literal|"hbase.mapred.output.quorum"
decl_stmt|;
comment|/** Optional job parameter to specify peer cluster's ZK client port */
specifier|public
specifier|static
specifier|final
name|String
name|QUORUM_PORT
init|=
literal|"hbase.mapred.output.quorum.port"
decl_stmt|;
comment|/** Optional specification of the rs class name of the peer cluster */
specifier|public
specifier|static
specifier|final
name|String
name|REGION_SERVER_CLASS
init|=
literal|"hbase.mapred.output.rs.class"
decl_stmt|;
comment|/** Optional specification of the rs impl name of the peer cluster */
specifier|public
specifier|static
specifier|final
name|String
name|REGION_SERVER_IMPL
init|=
literal|"hbase.mapred.output.rs.impl"
decl_stmt|;
comment|/** The configuration. */
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
comment|/**    * Writes the reducer output to an HBase table.    */
specifier|protected
class|class
name|TableRecordWriter
extends|extends
name|RecordWriter
argument_list|<
name|KEY
argument_list|,
name|Mutation
argument_list|>
block|{
specifier|private
name|Connection
name|connection
decl_stmt|;
specifier|private
name|BufferedMutator
name|mutator
decl_stmt|;
comment|/**      * @throws IOException       *       */
specifier|public
name|TableRecordWriter
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|tableName
init|=
name|conf
operator|.
name|get
argument_list|(
name|OUTPUT_TABLE
argument_list|)
decl_stmt|;
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|mutator
operator|=
name|connection
operator|.
name|getBufferedMutator
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created table instance for "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**      * Closes the writer, in this case flush table commits.      *      * @param context  The context.      * @throws IOException When closing the writer fails.      * @see RecordWriter#close(TaskAttemptContext)      */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|mutator
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**      * Writes a key/value pair into the table.      *      * @param key  The key.      * @param value  The value.      * @throws IOException When writing fails.      * @see RecordWriter#write(Object, Object)      */
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|KEY
name|key
parameter_list|,
name|Mutation
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|value
operator|instanceof
name|Put
operator|)
operator|&&
operator|!
operator|(
name|value
operator|instanceof
name|Delete
operator|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Pass a Delete or a Put"
argument_list|)
throw|;
block|}
name|mutator
operator|.
name|mutate
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Creates a new record writer.    *     * Be aware that the baseline javadoc gives the impression that there is a single    * {@link RecordWriter} per job but in HBase, it is more natural if we give you a new    * RecordWriter per call of this method. You must close the returned RecordWriter when done.    * Failure to do so will drop writes.    *    * @param context  The current task context.    * @return The newly created writer instance.    * @throws IOException When creating the writer fails.    * @throws InterruptedException When the jobs is cancelled.    */
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|KEY
argument_list|,
name|Mutation
argument_list|>
name|getRecordWriter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
operator|new
name|TableRecordWriter
argument_list|()
return|;
block|}
comment|/**    * Checks if the output target exists.    *    * @param context  The current context.    * @throws IOException When the check fails.    * @throws InterruptedException When the job is aborted.    * @see OutputFormat#checkOutputSpecs(JobContext)    */
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// TODO Check if the table exists?
block|}
comment|/**    * Returns the output committer.    *    * @param context  The current context.    * @return The committer.    * @throws IOException When creating the committer fails.    * @throws InterruptedException When the job is aborted.    * @see OutputFormat#getOutputCommitter(TaskAttemptContext)    */
annotation|@
name|Override
specifier|public
name|OutputCommitter
name|getOutputCommitter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
operator|new
name|TableOutputCommitter
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|otherConf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|otherConf
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
name|this
operator|.
name|conf
operator|.
name|get
argument_list|(
name|OUTPUT_TABLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableName
operator|==
literal|null
operator|||
name|tableName
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Must specify table name"
argument_list|)
throw|;
block|}
name|String
name|address
init|=
name|this
operator|.
name|conf
operator|.
name|get
argument_list|(
name|QUORUM_ADDRESS
argument_list|)
decl_stmt|;
name|int
name|zkClientPort
init|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
name|QUORUM_PORT
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|String
name|serverClass
init|=
name|this
operator|.
name|conf
operator|.
name|get
argument_list|(
name|REGION_SERVER_CLASS
argument_list|)
decl_stmt|;
name|String
name|serverImpl
init|=
name|this
operator|.
name|conf
operator|.
name|get
argument_list|(
name|REGION_SERVER_IMPL
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|address
operator|!=
literal|null
condition|)
block|{
name|ZKUtil
operator|.
name|applyClusterKeyToConf
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|address
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serverClass
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|serverImpl
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|zkClientPort
operator|!=
literal|0
condition|)
block|{
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|zkClientPort
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

