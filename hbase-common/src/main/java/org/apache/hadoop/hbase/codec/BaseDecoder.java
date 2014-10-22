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
name|Private
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
name|boolean
name|hasNext
init|=
literal|true
decl_stmt|;
specifier|private
name|Cell
name|current
init|=
literal|null
decl_stmt|;
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
name|in
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
if|if
condition|(
operator|!
name|this
operator|.
name|hasNext
condition|)
return|return
name|this
operator|.
name|hasNext
return|;
if|if
condition|(
name|this
operator|.
name|in
operator|.
name|available
argument_list|()
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|hasNext
operator|=
literal|false
expr_stmt|;
return|return
name|this
operator|.
name|hasNext
return|;
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
name|rethrowEofException
argument_list|(
name|ioEx
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|hasNext
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Partial cell read caused by EOF: "
operator|+
name|ioEx
argument_list|)
expr_stmt|;
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
comment|/**    * @return extract a Cell    * @throws IOException    */
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

