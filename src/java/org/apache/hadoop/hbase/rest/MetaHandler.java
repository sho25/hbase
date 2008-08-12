begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ServletException
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
name|HttpServletRequest
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
name|HBaseAdmin
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
name|HTableDescriptor
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
name|znerd
operator|.
name|xmlenc
operator|.
name|XMLOutputter
import|;
end_import

begin_comment
comment|/**  * MetaHandler fields all requests for metadata at the instance level. At the  * moment this is only GET requests to /.  */
end_comment

begin_class
specifier|public
class|class
name|MetaHandler
extends|extends
name|GenericHandler
block|{
specifier|public
name|MetaHandler
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|HBaseAdmin
name|admin
parameter_list|)
throws|throws
name|ServletException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|,
name|String
index|[]
name|pathSegments
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
name|getTables
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doPost
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|,
name|String
index|[]
name|pathSegments
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
name|doMethodNotAllowed
argument_list|(
name|response
argument_list|,
literal|"POST not allowed at /"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doPut
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|,
name|String
index|[]
name|pathSegments
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
name|doMethodNotAllowed
argument_list|(
name|response
argument_list|,
literal|"PUT not allowed at /"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doDelete
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|,
name|String
index|[]
name|pathSegments
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
name|doMethodNotAllowed
argument_list|(
name|response
argument_list|,
literal|"DELETE not allowed at /"
argument_list|)
expr_stmt|;
block|}
comment|/*    * Return list of tables.     * @param request    * @param response    */
specifier|private
name|void
name|getTables
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|,
specifier|final
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
index|[]
name|tables
init|=
name|this
operator|.
name|admin
operator|.
name|listTables
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|ContentType
operator|.
name|getContentType
argument_list|(
name|request
operator|.
name|getHeader
argument_list|(
name|ACCEPT
argument_list|)
argument_list|)
condition|)
block|{
case|case
name|XML
case|:
name|setResponseHeader
argument_list|(
name|response
argument_list|,
name|tables
operator|.
name|length
operator|>
literal|0
condition|?
literal|200
else|:
literal|204
argument_list|,
name|ContentType
operator|.
name|XML
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|XMLOutputter
name|outputter
init|=
name|getXMLOutputter
argument_list|(
name|response
operator|.
name|getWriter
argument_list|()
argument_list|)
decl_stmt|;
name|outputter
operator|.
name|startTag
argument_list|(
literal|"tables"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tables
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|doElement
argument_list|(
name|outputter
argument_list|,
literal|"table"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tables
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|outputter
operator|.
name|endTag
argument_list|()
expr_stmt|;
name|outputter
operator|.
name|endDocument
argument_list|()
expr_stmt|;
name|outputter
operator|.
name|getWriter
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
case|case
name|PLAIN
case|:
name|setResponseHeader
argument_list|(
name|response
argument_list|,
name|tables
operator|.
name|length
operator|>
literal|0
condition|?
literal|200
else|:
literal|204
argument_list|,
name|ContentType
operator|.
name|PLAIN
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|PrintWriter
name|out
init|=
name|response
operator|.
name|getWriter
argument_list|()
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
name|tables
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tables
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
default|default:
name|doNotAcceptable
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

