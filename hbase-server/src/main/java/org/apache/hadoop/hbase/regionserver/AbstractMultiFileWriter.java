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
name|regionserver
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|regionserver
operator|.
name|CellSink
import|;
end_import

begin_comment
comment|/**  * Base class for cell sink that separates the provided cells into multiple files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractMultiFileWriter
implements|implements
name|CellSink
implements|,
name|ShipperListener
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
name|AbstractMultiFileWriter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Factory that is used to produce single StoreFile.Writer-s */
specifier|protected
name|WriterFactory
name|writerFactory
decl_stmt|;
comment|/** Source scanner that is tracking KV count; may be null if source is not StoreScanner */
specifier|protected
name|StoreScanner
name|sourceScanner
decl_stmt|;
specifier|public
interface|interface
name|WriterFactory
block|{
specifier|public
name|StoreFileWriter
name|createWriter
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Initializes multi-writer before usage.    * @param sourceScanner Optional store scanner to obtain the information about read progress.    * @param factory Factory used to produce individual file writers.    */
specifier|public
name|void
name|init
parameter_list|(
name|StoreScanner
name|sourceScanner
parameter_list|,
name|WriterFactory
name|factory
parameter_list|)
block|{
name|this
operator|.
name|writerFactory
operator|=
name|factory
expr_stmt|;
name|this
operator|.
name|sourceScanner
operator|=
name|sourceScanner
expr_stmt|;
block|}
comment|/**    * Commit all writers.    *<p>    * Notice that here we use the same<code>maxSeqId</code> for all output files since we haven't    * find an easy to find enough sequence ids for different output files in some corner cases. See    * comments in HBASE-15400 for more details.    */
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|commitWriters
parameter_list|(
name|long
name|maxSeqId
parameter_list|,
name|boolean
name|majorCompaction
parameter_list|)
throws|throws
name|IOException
block|{
name|preCommitWriters
argument_list|()
expr_stmt|;
name|Collection
argument_list|<
name|StoreFileWriter
argument_list|>
name|writers
init|=
name|this
operator|.
name|writers
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Commit "
operator|+
name|writers
operator|.
name|size
argument_list|()
operator|+
literal|" writers, maxSeqId="
operator|+
name|maxSeqId
operator|+
literal|", majorCompaction="
operator|+
name|majorCompaction
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFileWriter
name|writer
range|:
name|writers
control|)
block|{
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|writer
operator|.
name|appendMetadata
argument_list|(
name|maxSeqId
argument_list|,
name|majorCompaction
argument_list|)
expr_stmt|;
name|preCloseWriter
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|paths
return|;
block|}
comment|/**    * Close all writers without throwing any exceptions. This is used when compaction failed usually.    */
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|abortWriters
parameter_list|()
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFileWriter
name|writer
range|:
name|writers
argument_list|()
control|)
block|{
try|try
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close the writer after an unfinished compaction."
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|paths
return|;
block|}
specifier|protected
specifier|abstract
name|Collection
argument_list|<
name|StoreFileWriter
argument_list|>
name|writers
parameter_list|()
function_decl|;
comment|/**    * Subclasses override this method to be called at the end of a successful sequence of append; all    * appends are processed before this method is called.    */
specifier|protected
name|void
name|preCommitWriters
parameter_list|()
throws|throws
name|IOException
block|{   }
comment|/**    * Subclasses override this method to be called before we close the give writer. Usually you can    * append extra metadata to the writer.    */
specifier|protected
name|void
name|preCloseWriter
parameter_list|(
name|StoreFileWriter
name|writer
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|beforeShipped
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|writers
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|StoreFileWriter
name|writer
range|:
name|writers
argument_list|()
control|)
block|{
name|writer
operator|.
name|beforeShipped
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

