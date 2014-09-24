begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
operator|.
name|filter
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
name|PrintWriter
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletOutputStream
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponseWrapper
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|GZIPResponseWrapper
extends|extends
name|HttpServletResponseWrapper
block|{
specifier|private
name|HttpServletResponse
name|response
decl_stmt|;
specifier|private
name|ServletOutputStream
name|os
decl_stmt|;
specifier|private
name|PrintWriter
name|writer
decl_stmt|;
specifier|private
name|boolean
name|compress
init|=
literal|true
decl_stmt|;
specifier|public
name|GZIPResponseWrapper
parameter_list|(
name|HttpServletResponse
name|response
parameter_list|)
block|{
name|super
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|this
operator|.
name|response
operator|=
name|response
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStatus
parameter_list|(
name|int
name|status
parameter_list|)
block|{
name|super
operator|.
name|setStatus
argument_list|(
name|status
argument_list|)
expr_stmt|;
if|if
condition|(
name|status
operator|<
literal|200
operator|||
name|status
operator|>=
literal|300
condition|)
block|{
name|compress
operator|=
literal|false
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|addHeader
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
literal|"content-length"
operator|.
name|equalsIgnoreCase
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|super
operator|.
name|addHeader
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setContentLength
parameter_list|(
name|int
name|length
parameter_list|)
block|{
comment|// do nothing
block|}
annotation|@
name|Override
specifier|public
name|void
name|setIntHeader
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
literal|"content-length"
operator|.
name|equalsIgnoreCase
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|super
operator|.
name|setIntHeader
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setHeader
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
literal|"content-length"
operator|.
name|equalsIgnoreCase
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|super
operator|.
name|setHeader
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|flushBuffer
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|os
operator|!=
literal|null
operator|&&
operator|(
name|os
operator|instanceof
name|GZIPResponseStream
operator|)
condition|)
block|{
operator|(
operator|(
name|GZIPResponseStream
operator|)
name|os
operator|)
operator|.
name|finish
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|getResponse
argument_list|()
operator|.
name|flushBuffer
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
if|if
condition|(
name|os
operator|!=
literal|null
operator|&&
operator|(
name|os
operator|instanceof
name|GZIPResponseStream
operator|)
condition|)
block|{
operator|(
operator|(
name|GZIPResponseStream
operator|)
name|os
operator|)
operator|.
name|resetBuffer
argument_list|()
expr_stmt|;
block|}
name|writer
operator|=
literal|null
expr_stmt|;
name|os
operator|=
literal|null
expr_stmt|;
name|compress
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resetBuffer
parameter_list|()
block|{
name|super
operator|.
name|resetBuffer
argument_list|()
expr_stmt|;
if|if
condition|(
name|os
operator|!=
literal|null
operator|&&
operator|(
name|os
operator|instanceof
name|GZIPResponseStream
operator|)
condition|)
block|{
operator|(
operator|(
name|GZIPResponseStream
operator|)
name|os
operator|)
operator|.
name|resetBuffer
argument_list|()
expr_stmt|;
block|}
name|writer
operator|=
literal|null
expr_stmt|;
name|os
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendError
parameter_list|(
name|int
name|status
parameter_list|,
name|String
name|msg
parameter_list|)
throws|throws
name|IOException
block|{
name|resetBuffer
argument_list|()
expr_stmt|;
name|super
operator|.
name|sendError
argument_list|(
name|status
argument_list|,
name|msg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendError
parameter_list|(
name|int
name|status
parameter_list|)
throws|throws
name|IOException
block|{
name|resetBuffer
argument_list|()
expr_stmt|;
name|super
operator|.
name|sendError
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendRedirect
parameter_list|(
name|String
name|location
parameter_list|)
throws|throws
name|IOException
block|{
name|resetBuffer
argument_list|()
expr_stmt|;
name|super
operator|.
name|sendRedirect
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ServletOutputStream
name|getOutputStream
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|os
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|response
operator|.
name|isCommitted
argument_list|()
operator|&&
name|compress
condition|)
block|{
name|os
operator|=
operator|(
name|ServletOutputStream
operator|)
operator|new
name|GZIPResponseStream
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|os
operator|=
name|response
operator|.
name|getOutputStream
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|os
return|;
block|}
annotation|@
name|Override
specifier|public
name|PrintWriter
name|getWriter
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
name|writer
operator|=
operator|new
name|PrintWriter
argument_list|(
name|getOutputStream
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|writer
return|;
block|}
block|}
end_class

end_unit

