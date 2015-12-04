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
name|codec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PushbackInputStream
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nonnull
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
name|HBaseInterfaceAudience
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

begin_comment
comment|/**  * TODO javadoc  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|,
name|HBaseInterfaceAudience
operator|.
name|PHOENIX
block|}
argument_list|)
specifier|public
specifier|abstract
class|class
name|BaseDecoder
implements|implements
name|Codec
operator|.
name|Decoder
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|BaseDecoder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|InputStream
name|in
decl_stmt|;
specifier|private
name|Cell
name|current
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
class|class
name|PBIS
extends|extends
name|PushbackInputStream
block|{
specifier|public
name|PBIS
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|resetBuf
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
operator|new
name|byte
index|[
name|size
index|]
expr_stmt|;
name|this
operator|.
name|pos
operator|=
name|size
expr_stmt|;
block|}
block|}
specifier|public
name|BaseDecoder
parameter_list|(
specifier|final
name|InputStream
name|in
parameter_list|)
block|{
name|this
operator|.
name|in
operator|=
operator|new
name|PBIS
argument_list|(
name|in
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|advance
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|firstByte
init|=
name|in
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstByte
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
operator|(
operator|(
name|PBIS
operator|)
name|in
operator|)
operator|.
name|unread
argument_list|(
name|firstByte
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|this
operator|.
name|current
operator|=
name|parseCell
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioEx
parameter_list|)
block|{
operator|(
operator|(
name|PBIS
operator|)
name|in
operator|)
operator|.
name|resetBuf
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// reset the buffer in case the underlying stream is read from upper layers
name|rethrowEofException
argument_list|(
name|ioEx
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|rethrowEofException
parameter_list|(
name|IOException
name|ioEx
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|isEof
init|=
literal|false
decl_stmt|;
try|try
block|{
name|isEof
operator|=
name|this
operator|.
name|in
operator|.
name|available
argument_list|()
operator|==
literal|0
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Error getting available for error message - ignoring"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isEof
condition|)
throw|throw
name|ioEx
throw|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Partial cell read caused by EOF"
argument_list|,
name|ioEx
argument_list|)
expr_stmt|;
block|}
name|EOFException
name|eofEx
init|=
operator|new
name|EOFException
argument_list|(
literal|"Partial cell read"
argument_list|)
decl_stmt|;
name|eofEx
operator|.
name|initCause
argument_list|(
name|ioEx
argument_list|)
expr_stmt|;
throw|throw
name|eofEx
throw|;
block|}
specifier|protected
name|InputStream
name|getInputStream
parameter_list|()
block|{
return|return
name|in
return|;
block|}
comment|/**    * Extract a Cell.    * @return a parsed Cell or throws an Exception. EOFException or a generic IOException maybe    * thrown if EOF is reached prematurely. Does not return null.    * @throws IOException    */
annotation|@
name|Nonnull
specifier|protected
specifier|abstract
name|Cell
name|parseCell
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|public
name|Cell
name|current
parameter_list|()
block|{
return|return
name|this
operator|.
name|current
return|;
block|}
block|}
end_class

end_unit

