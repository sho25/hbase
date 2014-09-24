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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|client
operator|.
name|Durability
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
comment|/**  *<p>  * Hadoop output format that writes to one or more HBase tables. The key is  * taken to be the table name while the output value<em>must</em> be either a  * {@link Put} or a {@link Delete} instance. All tables must already exist, and  * all Puts and Deletes must reference only valid column families.  *</p>  *  *<p>  * Write-ahead logging (HLog) for Puts can be disabled by setting  * {@link #WAL_PROPERTY} to {@link #WAL_OFF}. Default value is {@link #WAL_ON}.  * Note that disabling write-ahead logging is only appropriate for jobs where  * loss of data due to region server failure can be tolerated (for example,  * because it is easy to rerun a bulk import).  *</p>  */
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
name|MultiTableOutputFormat
extends|extends
name|OutputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Mutation
argument_list|>
block|{
comment|/** Set this to {@link #WAL_OFF} to turn off write-ahead logging (HLog) */
specifier|public
specifier|static
specifier|final
name|String
name|WAL_PROPERTY
init|=
literal|"hbase.mapreduce.multitableoutputformat.wal"
decl_stmt|;
comment|/** Property value to use write-ahead logging */
specifier|public
specifier|static
specifier|final
name|boolean
name|WAL_ON
init|=
literal|true
decl_stmt|;
comment|/** Property value to disable write-ahead logging */
specifier|public
specifier|static
specifier|final
name|boolean
name|WAL_OFF
init|=
literal|false
decl_stmt|;
comment|/**    * Record writer for outputting to multiple HTables.    */
specifier|protected
specifier|static
class|class
name|MultiTableRecordWriter
extends|extends
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Mutation
argument_list|>
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
name|MultiTableRecordWriter
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|HTable
argument_list|>
name|tables
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
name|boolean
name|useWriteAheadLogging
decl_stmt|;
comment|/**      * @param conf      *          HBaseConfiguration to used      * @param useWriteAheadLogging      *          whether to use write ahead logging. This can be turned off (      *<tt>false</tt>) to improve performance when bulk loading data.      */
specifier|public
name|MultiTableRecordWriter
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|useWriteAheadLogging
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created new MultiTableRecordReader with WAL "
operator|+
operator|(
name|useWriteAheadLogging
condition|?
literal|"on"
else|:
literal|"off"
operator|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|tables
operator|=
operator|new
name|HashMap
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|HTable
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|useWriteAheadLogging
operator|=
name|useWriteAheadLogging
expr_stmt|;
block|}
comment|/**      * @param tableName      *          the name of the table, as a string      * @return the named table      * @throws IOException      *           if there is a problem opening a table      */
name|HTable
name|getTable
parameter_list|(
name|ImmutableBytesWritable
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|tables
operator|.
name|containsKey
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Opening HTable \""
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
operator|.
name|get
argument_list|()
argument_list|)
operator|+
literal|"\" for writing"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|tables
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
return|return
name|tables
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
return|;
block|}
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
for|for
control|(
name|HTable
name|table
range|:
name|tables
operator|.
name|values
argument_list|()
control|)
block|{
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Writes an action (Put or Delete) to the specified table.      *      * @param tableName      *          the table being updated.      * @param action      *          the update, either a put or a delete.      * @throws IllegalArgumentException      *          if the action is not a put or a delete.      */
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ImmutableBytesWritable
name|tableName
parameter_list|,
name|Mutation
name|action
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// The actions are not immutable, so we defensively copy them
if|if
condition|(
name|action
operator|instanceof
name|Put
condition|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
operator|(
name|Put
operator|)
name|action
argument_list|)
decl_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|useWriteAheadLogging
condition|?
name|Durability
operator|.
name|SYNC_WAL
else|:
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|action
operator|instanceof
name|Delete
condition|)
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
operator|(
name|Delete
operator|)
name|action
argument_list|)
decl_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
else|else
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"action must be either Delete or Put"
argument_list|)
throw|;
block|}
block|}
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
comment|// we can't know ahead of time if it's going to blow up when the user
comment|// passes a table name that doesn't exist, so nothing useful here.
block|}
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
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
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
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
return|return
operator|new
name|MultiTableRecordWriter
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|WAL_PROPERTY
argument_list|,
name|WAL_ON
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

