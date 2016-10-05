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
name|fs
operator|.
name|FileAlreadyExistsException
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
name|FileSystem
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
name|mapred
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
name|mapred
operator|.
name|InvalidJobConfException
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
name|mapred
operator|.
name|Reporter
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
name|Progressable
import|;
end_import

begin_comment
comment|/**  * Convert Map/Reduce output and write it to an HBase table  */
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
extends|extends
name|FileOutputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|>
block|{
comment|/** JobConf parameter that specifies the output table */
specifier|public
specifier|static
specifier|final
name|String
name|OUTPUT_TABLE
init|=
literal|"hbase.mapred.outputtable"
decl_stmt|;
comment|/**    * Convert Reduce output (key, value) to (HStoreKey, KeyedDataArrayWritable)    * and write to an HBase table.    */
specifier|protected
specifier|static
class|class
name|TableRecordWriter
implements|implements
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|>
block|{
specifier|private
name|BufferedMutator
name|m_mutator
decl_stmt|;
specifier|private
name|Connection
name|conn
decl_stmt|;
comment|/**      * Instantiate a TableRecordWriter with the HBase HClient for writing.      *      * @deprecated Please use {@code #TableRecordWriter(JobConf)}  This version does not clean up      * connections and will leak connections (removed in 2.0)      */
annotation|@
name|Deprecated
specifier|public
name|TableRecordWriter
parameter_list|(
specifier|final
name|BufferedMutator
name|mutator
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|m_mutator
operator|=
name|mutator
expr_stmt|;
name|this
operator|.
name|conn
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Instantiate a TableRecordWriter with a BufferedMutator for batch writing.      */
specifier|public
name|TableRecordWriter
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// expecting exactly one path
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|job
operator|.
name|get
argument_list|(
name|OUTPUT_TABLE
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|this
operator|.
name|conn
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|this
operator|.
name|m_mutator
operator|=
name|conn
operator|.
name|getBufferedMutator
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|this
operator|.
name|m_mutator
operator|==
literal|null
condition|)
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
name|conn
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|close
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|m_mutator
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|m_mutator
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|conn
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|write
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Put
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|m_mutator
operator|.
name|mutate
argument_list|(
operator|new
name|Put
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Creates a new record writer.    *     * Be aware that the baseline javadoc gives the impression that there is a single    * {@link RecordWriter} per job but in HBase, it is more natural if we give you a new    * RecordWriter per call of this method. You must close the returned RecordWriter when done.    * Failure to do so will drop writes.    *    * @param ignored Ignored filesystem    * @param job Current JobConf    * @param name Name of the job    * @param progress    * @return The newly created writer instance.    * @throws IOException When creating the writer fails.    */
annotation|@
name|Override
specifier|public
name|RecordWriter
name|getRecordWriter
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|String
name|name
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Clear write buffer on fail is true by default so no need to reset it.
return|return
operator|new
name|TableRecordWriter
argument_list|(
name|job
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|FileAlreadyExistsException
throws|,
name|InvalidJobConfException
throws|,
name|IOException
block|{
name|String
name|tableName
init|=
name|job
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
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Must specify table name"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

