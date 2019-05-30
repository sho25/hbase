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
name|wal
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
name|Arrays
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
name|List
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CompletionService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
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
name|MultipleIOException
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
comment|/**  * Class that manages the output streams from the log splitting process.  * Bounded means the output streams will be no more than the size of threadpool  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BoundedLogWriterCreationOutputSink
extends|extends
name|LogRecoveredEditsOutputSink
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
name|BoundedLogWriterCreationOutputSink
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|regionRecoverStatMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|BoundedLogWriterCreationOutputSink
parameter_list|(
name|WALSplitter
name|walSplitter
parameter_list|,
name|WALSplitter
operator|.
name|PipelineController
name|controller
parameter_list|,
name|EntryBuffers
name|entryBuffers
parameter_list|,
name|int
name|numWriters
parameter_list|)
block|{
name|super
argument_list|(
name|walSplitter
argument_list|,
name|controller
argument_list|,
name|entryBuffers
argument_list|,
name|numWriters
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|finishWritingAndClose
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|isSuccessful
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|result
decl_stmt|;
try|try
block|{
name|isSuccessful
operator|=
name|finishWriting
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|result
operator|=
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isSuccessful
condition|)
block|{
name|splits
operator|=
name|result
expr_stmt|;
block|}
return|return
name|splits
return|;
block|}
annotation|@
name|Override
name|boolean
name|executeCloseTask
parameter_list|(
name|CompletionService
argument_list|<
name|Void
argument_list|>
name|completionService
parameter_list|,
name|List
argument_list|<
name|IOException
argument_list|>
name|thrown
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|paths
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
for|for
control|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|WALSplitter
operator|.
name|RegionEntryBuffer
argument_list|>
name|buffer
range|:
name|entryBuffers
operator|.
name|buffers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Submitting writeThenClose of {}"
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|buffer
operator|.
name|getValue
argument_list|()
operator|.
name|encodedRegionName
argument_list|)
argument_list|)
expr_stmt|;
name|completionService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|dst
init|=
name|writeThenClose
argument_list|(
name|buffer
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|dst
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|boolean
name|progress_failed
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|entryBuffers
operator|.
name|buffers
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|Future
argument_list|<
name|Void
argument_list|>
name|future
init|=
name|completionService
operator|.
name|take
argument_list|()
decl_stmt|;
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|progress_failed
operator|&&
name|reporter
operator|!=
literal|null
operator|&&
operator|!
name|reporter
operator|.
name|progress
argument_list|()
condition|)
block|{
name|progress_failed
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|progress_failed
return|;
block|}
comment|/**    * since the splitting process may create multiple output files, we need a map    * regionRecoverStatMap to track the output count of each region.    * @return a map from encoded region ID to the number of edits written out for that region.    */
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|getOutputCounts
parameter_list|()
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|regionRecoverStatMapResult
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|regionRecoverStatMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|regionRecoverStatMapResult
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|regionRecoverStatMapResult
return|;
block|}
comment|/**    * @return the number of recovered regions    */
annotation|@
name|Override
specifier|public
name|int
name|getNumberOfRecoveredRegions
parameter_list|()
block|{
return|return
name|regionRecoverStatMap
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Append the buffer to a new recovered edits file, then close it after all done    * @param buffer contain all entries of a certain region    * @throws IOException when closeWriter failed    */
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|WALSplitter
operator|.
name|RegionEntryBuffer
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
name|writeThenClose
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Path
name|writeThenClose
parameter_list|(
name|WALSplitter
operator|.
name|RegionEntryBuffer
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
name|WALSplitter
operator|.
name|WriterAndPath
name|wap
init|=
name|appendBuffer
argument_list|(
name|buffer
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|wap
operator|!=
literal|null
condition|)
block|{
name|String
name|encodedRegionName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|buffer
operator|.
name|encodedRegionName
argument_list|)
decl_stmt|;
name|Long
name|value
init|=
name|regionRecoverStatMap
operator|.
name|putIfAbsent
argument_list|(
name|encodedRegionName
argument_list|,
name|wap
operator|.
name|editsWritten
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|Long
name|newValue
init|=
name|regionRecoverStatMap
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
operator|+
name|wap
operator|.
name|editsWritten
decl_stmt|;
name|regionRecoverStatMap
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
name|newValue
argument_list|)
expr_stmt|;
block|}
block|}
name|Path
name|dst
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|IOException
argument_list|>
name|thrown
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|wap
operator|!=
literal|null
condition|)
block|{
name|dst
operator|=
name|closeWriter
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|buffer
operator|.
name|encodedRegionName
argument_list|)
argument_list|,
name|wap
argument_list|,
name|thrown
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|thrown
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
name|MultipleIOException
operator|.
name|createIOException
argument_list|(
name|thrown
argument_list|)
throw|;
block|}
return|return
name|dst
return|;
block|}
block|}
end_class

end_unit
