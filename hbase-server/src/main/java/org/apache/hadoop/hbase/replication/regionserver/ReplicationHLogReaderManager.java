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
name|replication
operator|.
name|regionserver
package|;
end_package

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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|wal
operator|.
name|HLogFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Wrapper class around HLog to help manage the implementation details  * such as compression.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationHLogReaderManager
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
name|ReplicationHLogReaderManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|long
name|position
init|=
literal|0
decl_stmt|;
specifier|private
name|HLog
operator|.
name|Reader
name|reader
decl_stmt|;
specifier|private
name|Path
name|lastPath
decl_stmt|;
comment|/**    * Creates the helper but doesn't open any file    * Use setInitialPosition after using the constructor if some content needs to be skipped    * @param fs    * @param conf    */
specifier|public
name|ReplicationHLogReaderManager
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * Opens the file at the current position    * @param path    * @return an HLog reader.    * @throws IOException    */
specifier|public
name|HLog
operator|.
name|Reader
name|openReader
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Detect if this is a new file, if so get a new reader else
comment|// reset the current reader so that we see the new data
if|if
condition|(
name|this
operator|.
name|reader
operator|==
literal|null
operator|||
operator|!
name|this
operator|.
name|lastPath
operator|.
name|equals
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|this
operator|.
name|closeReader
argument_list|()
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|HLogFactory
operator|.
name|createReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|path
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastPath
operator|=
name|path
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|this
operator|.
name|reader
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|npe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"NPE resetting reader, likely HDFS-4380"
argument_list|,
name|npe
argument_list|)
throw|;
block|}
block|}
return|return
name|this
operator|.
name|reader
return|;
block|}
comment|/**    * Get the next entry, returned and also added in the array    * @return a new entry or null    * @throws IOException    */
specifier|public
name|HLog
operator|.
name|Entry
name|readNextAndSetPosition
parameter_list|()
throws|throws
name|IOException
block|{
name|HLog
operator|.
name|Entry
name|entry
init|=
name|this
operator|.
name|reader
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// Store the position so that in the future the reader can start
comment|// reading from here. If the above call to next() throws an
comment|// exception, the position won't be changed and retry will happen
comment|// from the last known good position
name|this
operator|.
name|position
operator|=
name|this
operator|.
name|reader
operator|.
name|getPosition
argument_list|()
expr_stmt|;
comment|// We need to set the CC to null else it will be compressed when sent to the sink
if|if
condition|(
name|entry
operator|!=
literal|null
condition|)
block|{
name|entry
operator|.
name|setCompressionContext
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|entry
return|;
block|}
comment|/**    * Advance the reader to the current position    * @throws IOException    */
specifier|public
name|void
name|seek
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|position
operator|!=
literal|0
condition|)
block|{
name|this
operator|.
name|reader
operator|.
name|seek
argument_list|(
name|this
operator|.
name|position
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the position that we stopped reading at    * @return current position, cannot be negative    */
specifier|public
name|long
name|getPosition
parameter_list|()
block|{
return|return
name|this
operator|.
name|position
return|;
block|}
specifier|public
name|void
name|setPosition
parameter_list|(
name|long
name|pos
parameter_list|)
block|{
name|this
operator|.
name|position
operator|=
name|pos
expr_stmt|;
block|}
comment|/**    * Close the current reader    * @throws IOException    */
specifier|public
name|void
name|closeReader
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|reader
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|reader
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Tell the helper to reset internal state    */
name|void
name|finishCurrentFile
parameter_list|()
block|{
name|this
operator|.
name|position
operator|=
literal|0
expr_stmt|;
try|try
block|{
name|this
operator|.
name|closeReader
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to close reader"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

