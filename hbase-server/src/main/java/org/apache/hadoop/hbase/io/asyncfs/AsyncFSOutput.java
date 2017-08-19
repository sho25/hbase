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
name|io
operator|.
name|asyncfs
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|CompletableFuture
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|CancelableProgressable
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
name|hdfs
operator|.
name|protocol
operator|.
name|DatanodeInfo
import|;
end_import

begin_comment
comment|/**  * Interface for asynchronous filesystem output stream.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|AsyncFSOutput
extends|extends
name|Closeable
block|{
comment|/**    * Just call write(b, 0, b.length).    * @see #write(byte[], int, int)    */
name|void
name|write
parameter_list|(
name|byte
index|[]
name|b
parameter_list|)
function_decl|;
comment|/**    * Copy the data into the buffer. Note that you need to call {@link #flush(boolean)} to flush the    * buffer manually.    */
name|void
name|write
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
function_decl|;
comment|/**    * Write an int to the buffer.    */
name|void
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|)
function_decl|;
comment|/**    * Copy the data in the given {@code bb} into the buffer.    */
name|void
name|write
parameter_list|(
name|ByteBuffer
name|bb
parameter_list|)
function_decl|;
comment|/**    * Return the current size of buffered data.    */
name|int
name|buffered
parameter_list|()
function_decl|;
comment|/**    * Return current pipeline. Empty array if no pipeline.    */
name|DatanodeInfo
index|[]
name|getPipeline
parameter_list|()
function_decl|;
comment|/**    * Flush the buffer out.    * @param sync persistent the data to device    * @return A CompletableFuture that hold the acked length after flushing.    */
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
name|flush
parameter_list|(
name|boolean
name|sync
parameter_list|)
function_decl|;
comment|/**    * The close method when error occurred.    */
name|void
name|recoverAndClose
parameter_list|(
name|CancelableProgressable
name|reporter
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Close the file. You should call {@link #recoverAndClose(CancelableProgressable)} if this method    * throws an exception.    */
annotation|@
name|Override
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

