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
name|io
operator|.
name|OutputStream
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
name|ArrayList
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
name|util
operator|.
name|ByteBufferUtils
import|;
end_import

begin_comment
comment|/**  * An OutputStream which writes data into ByteBuffers. It will try to get ByteBuffer, as and when  * needed, from the passed pool. When pool is not giving a ByteBuffer it will create one on heap.  * Make sure to call {@link #releaseResources()} method once the Stream usage is over and  * data is transferred to the wanted destination.  * Not thread safe!  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ByteBufferListOutputStream
extends|extends
name|ByteBufferOutputStream
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
name|ByteBufferListOutputStream
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ByteBufferPool
name|pool
decl_stmt|;
comment|// Keep track of the BBs where bytes written to. We will first try to get a BB from the pool. If
comment|// it is not available will make a new one our own and keep writing to that. We keep track of all
comment|// the BBs that we got from pool, separately so that on closeAndPutbackBuffers, we can make sure
comment|// to return back all of them to pool
specifier|protected
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|allBufs
init|=
operator|new
name|ArrayList
argument_list|<
name|ByteBuffer
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|bufsFromPool
init|=
operator|new
name|ArrayList
argument_list|<
name|ByteBuffer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|lastBufFlipped
init|=
literal|false
decl_stmt|;
comment|// Indicate whether the curBuf/lastBuf is flipped already
specifier|public
name|ByteBufferListOutputStream
parameter_list|(
name|ByteBufferPool
name|pool
parameter_list|)
block|{
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|allocateNewBuffer
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|allocateNewBuffer
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|curBuf
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|curBuf
operator|.
name|flip
argument_list|()
expr_stmt|;
comment|// On the current buf set limit = pos and pos = 0.
block|}
comment|// Get an initial BB to work with from the pool
name|this
operator|.
name|curBuf
operator|=
name|this
operator|.
name|pool
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|curBuf
operator|==
literal|null
condition|)
block|{
comment|// No free BB at this moment. Make a new one. The pool returns off heap BBs. Don't make off
comment|// heap BB on demand. It is difficult to account for all such and so proper sizing of Max
comment|// direct heap size. See HBASE-15525 also for more details.
comment|// Make BB with same size of pool's buffer size.
name|this
operator|.
name|curBuf
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|this
operator|.
name|pool
operator|.
name|getBufferSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|bufsFromPool
operator|.
name|add
argument_list|(
name|this
operator|.
name|curBuf
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|allBufs
operator|.
name|add
argument_list|(
name|this
operator|.
name|curBuf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
name|int
name|s
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|allBufs
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|s
operator|+=
name|this
operator|.
name|allBufs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|remaining
argument_list|()
expr_stmt|;
block|}
comment|// On the last BB, it might not be flipped yet if getByteBuffers is not yet called
if|if
condition|(
name|this
operator|.
name|lastBufFlipped
condition|)
block|{
name|s
operator|+=
name|this
operator|.
name|curBuf
operator|.
name|remaining
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|s
operator|+=
name|this
operator|.
name|curBuf
operator|.
name|position
argument_list|()
expr_stmt|;
block|}
return|return
name|s
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getByteBuffer
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This stream is not backed by a single ByteBuffer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|checkSizeAndGrow
parameter_list|(
name|int
name|extra
parameter_list|)
block|{
name|long
name|capacityNeeded
init|=
name|curBuf
operator|.
name|position
argument_list|()
operator|+
operator|(
name|long
operator|)
name|extra
decl_stmt|;
if|if
condition|(
name|capacityNeeded
operator|>
name|curBuf
operator|.
name|limit
argument_list|()
condition|)
block|{
name|allocateNewBuffer
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// No usage of this API in code. Just making it as an Unsupported operation as of now
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Release the resources it uses (The ByteBuffers) which are obtained from pool. Call this only    * when all the data is fully used. And it must be called at the end of usage else we will leak    * ByteBuffers from pool.    */
specifier|public
name|void
name|releaseResources
parameter_list|()
block|{
try|try
block|{
name|close
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
name|debug
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
comment|// Return back all the BBs to pool
if|if
condition|(
name|this
operator|.
name|bufsFromPool
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|bufsFromPool
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|pool
operator|.
name|putbackBuffer
argument_list|(
name|this
operator|.
name|bufsFromPool
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|bufsFromPool
operator|=
literal|null
expr_stmt|;
block|}
name|this
operator|.
name|allBufs
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|curBuf
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|(
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
comment|// No usage of this API in code. Just making it as an Unsupported operation as of now
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * We can be assured that the buffers returned by this method are all flipped    * @return list of bytebuffers    */
specifier|public
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|getByteBuffers
parameter_list|()
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|lastBufFlipped
condition|)
block|{
name|this
operator|.
name|lastBufFlipped
operator|=
literal|true
expr_stmt|;
comment|// All the other BBs are already flipped while moving to the new BB.
name|curBuf
operator|.
name|flip
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|allBufs
return|;
block|}
annotation|@
name|Override
specifier|public
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
throws|throws
name|IOException
block|{
name|int
name|toWrite
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|toWrite
operator|=
name|Math
operator|.
name|min
argument_list|(
name|len
argument_list|,
name|this
operator|.
name|curBuf
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|this
operator|.
name|curBuf
argument_list|,
name|b
argument_list|,
name|off
argument_list|,
name|toWrite
argument_list|)
expr_stmt|;
name|off
operator|+=
name|toWrite
expr_stmt|;
name|len
operator|-=
name|toWrite
expr_stmt|;
if|if
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|allocateNewBuffer
argument_list|()
expr_stmt|;
comment|// The curBuf is over. Let us move to the next one
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuffer
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|toWrite
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|toWrite
operator|=
name|Math
operator|.
name|min
argument_list|(
name|len
argument_list|,
name|this
operator|.
name|curBuf
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBufferToBuffer
argument_list|(
name|b
argument_list|,
name|this
operator|.
name|curBuf
argument_list|,
name|off
argument_list|,
name|toWrite
argument_list|)
expr_stmt|;
name|off
operator|+=
name|toWrite
expr_stmt|;
name|len
operator|-=
name|toWrite
expr_stmt|;
if|if
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|allocateNewBuffer
argument_list|()
expr_stmt|;
comment|// The curBuf is over. Let us move to the next one
block|}
block|}
block|}
block|}
end_class

end_unit

